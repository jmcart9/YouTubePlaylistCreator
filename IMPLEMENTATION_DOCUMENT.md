# YouTube Playlist Creator — Implementation Document
### YouTube Data API v3 & Gmail API: Use in the Learning Process of App Development

**Project:** YouTube Playlist Creator  
**Language / Build Tool:** Java 15 · Gradle  
**Author:** Project Owner  
**Document Date:** June 3, 2026  

---

## 1. Project Overview

YouTube Playlist Creator is a personal-use Java desktop application developed as a hands-on learning project for Google API integration. The core motivation is to automate a real-world workflow:

> YouTube sends email notifications to a Gmail inbox every time a subscribed channel uploads a video. Over time, thousands of unread notifications accumulate. This application reads those notifications via the **Gmail API**, extracts the video URLs, groups them by uploader channel, and organises them into **YouTube playlists** using the **YouTube Data API v3**. Additional features — developed incrementally as new API skills were learned — allow copying entire playlists and entire subscription lists between two YouTube accounts.

The project is entirely self-directed and educational in nature: no commercial product is sold or distributed, and all API calls are made under the developer's own OAuth 2.0 credentials against their own Google accounts.

---

## 2. Technologies & Dependencies

| Library | Version | Purpose |
|---|---|---|
| `google-api-services-youtube` | v3-rev222-1.25.0 | YouTube Data API v3 client |
| `google-api-services-gmail` | v1-rev83-1.23.0 | Gmail API client |
| `google-api-client` | 1.31.2 | Core Google API Java client |
| `google-oauth-client-jetty` | 1.23.0 | OAuth 2.0 local server redirect |
| `google-api-client-jackson2` | 1.25.0 | JSON serialisation |
| `guava` | 29.0-jre | Utility (list construction) |
| JUnit 4 / JUnit Jupiter 5 | 4.13 / 5.1.0 | Unit testing |

All dependencies are resolved via **Maven Central** and declared in `build.gradle`.

---

## 3. How the YouTube API Is Used — Step-by-Step

### 3.1 OAuth 2.0 Authentication (`AuthYouTube.java`)

**Learning goal:** Understand how to authenticate a Java desktop application against Google's OAuth 2.0 server and cache tokens for reuse.

```
User runs the app
    └─► AuthYouTube.authorize() loads client_secret.json
        └─► GoogleAuthorizationCodeFlow builds an authorisation URL
            └─► LocalServerReceiver opens a local HTTP server (port 8080 or 8081)
                └─► Browser window opens; user signs into Google
                    └─► Google redirects with an authorisation code
                        └─► Token is stored in a FileDataStoreFactory (tokens_source/ or tokens_dest/)
                            └─► Subsequent runs reuse the stored token without a browser prompt
```

**Key API classes learned:**
- `GoogleAuthorizationCodeFlow.Builder` — constructs the OAuth flow
- `LocalServerReceiver` — catches the redirect on localhost
- `AuthorizationCodeInstalledApp` — ties the flow and receiver together
- `FileDataStoreFactory` — persists the credential to disk

**Multi-account support (learned skill):** By accepting a `userLabel`, `tokensDir`, and `port` parameter, the same auth method authenticates two different Google accounts in the same JVM session — a skill learned through iterative debugging when the copy-playlist feature was added.

---

### 3.2 Reading Gmail Notifications (`GmailMethods.java`)

**Learning goal:** Learn the Gmail API's message-listing and message-fetching pattern, and practice Base64 MIME decoding.

**API calls made:**

| Method | Gmail API Endpoint | What was learned |
|---|---|---|
| `setEmailMessageList()` | `users.messages.list` with query `from:noreply@youtube.com` | Pagination via `nextPageToken`; `list` returns only IDs, not full bodies |
| `getMessage()` | `users.messages.get` | Fetching the full message object by ID |
| `messageBodyToString()` | — (client-side) | MIME multipart traversal; URL-safe Base64 decoding |
| `getVideoUrl()` | — (client-side) | String scanning for `youtube.com/watch?` links |
| `getVideoIDFromUrl()` | — (client-side) | Regex extraction of the `v=` query parameter |

---

### 3.3 Fetching Video Metadata (`YouTubeMethods.getVideoChannel()`)

**Learning goal:** Query the YouTube `videos` resource to retrieve snippet metadata for a given video ID.

```java
// YouTube Data API v3 call:
YouTube.Videos.List request = service.videos().list("snippet");
VideoListResponse response = request.setId(videoID).execute();
String channelTitle = response.getItems().get(0).getSnippet().getChannelTitle();
```

**Quota cost:** 1 unit per call (read operation).  
**Skill learned:** Understanding the `snippet` part selector and how to navigate the response model.

---

### 3.4 Listing All Playlists (`YouTubeMethods` constructor)

**Learning goal:** Retrieve all playlists owned by the authenticated account, including handling pagination.

```java
YouTube.Playlists.List request = service.playlists().list("snippet");
request.setMaxResults(50L).setMine(true);
// Paginate until nextPageToken is null
```

**Quota cost:** 1 unit per page (50 playlists per page).  
**Skill learned:** The YouTube API's maximum page size is 50; larger collections require looping on `nextPageToken`.

---

### 3.5 Creating a Playlist (`YouTubeMethods.createPlaylist()`)

**Learning goal:** Make a write request to the YouTube API; understand `PlaylistSnippet`, `PlaylistStatus`, and the `insert` method.

```java
PlaylistSnippet snippet = new PlaylistSnippet();
snippet.setTitle(title);
snippet.setDescription("playlist for " + title);

PlaylistStatus status = new PlaylistStatus();
status.setPrivacyStatus("private");   // learned: new playlists default to private for safety

Playlist playlist = new Playlist();
playlist.setSnippet(snippet).setStatus(status);

service.playlists().insert("snippet,status", playlist).execute();
```

**Quota cost:** 50 units per insert (write operation).  
**Skill learned:** Write operations are 50× more expensive than read operations; this drove the idempotency design (check before creating).

---

### 3.6 Inserting a Video into a Playlist (`YouTubeMethods.insertPlaylistItem()`)

**Learning goal:** Understand the `PlaylistItems` resource and the relationship between `ResourceId`, `PlaylistItemSnippet`, and `PlaylistItem`.

```java
ResourceId resourceId = new ResourceId();
resourceId.setKind("youtube#video");
resourceId.setVideoId(videoId);

PlaylistItemSnippet itemSnippet = new PlaylistItemSnippet();
itemSnippet.setPlaylistId(playlistId);
itemSnippet.setResourceId(resourceId);

PlaylistItem item = new PlaylistItem();
item.setSnippet(itemSnippet);

service.playlistItems().insert("snippet,contentDetails", item).execute();
```

**Quota cost:** 50 units per insert.  
**Skill learned:** Each video insertion is expensive; batching decisions and deduplication (see §3.7) are critical.

---

### 3.7 Copying a Playlist Between Accounts (`YouTubeMethods.copyPlaylistFrom()`)

**Learning goal:** Coordinate two authenticated `YouTube` service instances; practice quota-aware design.

**Flow:**

```
SOURCE account                          DESTINATION account
─────────────────────────────────────   ──────────────────────────────────────────
listPlaylistVideoIds(sourcePlaylistId)  getOrCreatePlaylistId(destinationTitle)
    │                                       │
    │  [list of video IDs]                  │  listPlaylistVideoIds(destPlaylistId)
    │                                       │  [filter: skip already-present videos]
    └───────────────────────────────────────►
                                            for each new videoId:
                                                insertPlaylistItem(destPlaylistId, videoId)
```

**Key learning outcomes:**
- **Idempotency:** Videos already present in the destination are skipped. This is critical because the daily quota (10,000 units default) can be exhausted before all playlists are copied; re-running the next day resumes without duplicating content.
- **Quota budget example:** Copying 1,000 videos costs ~50,000 quota units in inserts alone — more than five days' quota. This forced learning about practical quota management.
- **Separation of concerns:** The `source` parameter lets the destination `YouTubeMethods` instance delegate reads to the source instance, keeping authentication contexts clean.

---

### 3.8 Listing & Copying Subscriptions (`YouTubeMethods.listSubscriptions()` / `copySubscriptionsFrom()`)

**Learning goal:** Learn the `subscriptions` resource; understand the 50-unit cost of each new subscription.

```java
// List subscriptions
service.subscriptions().list("snippet")
    .setMine(true)
    .setMaxResults(50L)
    .execute();

// Subscribe to a channel
ResourceId rid = new ResourceId();
rid.setKind("youtube#channel");
rid.setChannelId(channelId);

SubscriptionSnippet snippet = new SubscriptionSnippet();
snippet.setResourceId(rid);

Subscription sub = new Subscription();
sub.setSnippet(snippet);

service.subscriptions().insert("snippet", sub).execute();
```

**Quota cost:** 1 unit to list; **50 units** per new subscription.  
**Key learning:** A 10,000-unit daily quota allows adding at most ~200 new subscriptions per day. The application informs the user of this limit before proceeding and handles `HTTP 409 Already Subscribed` gracefully.

---

## 4. Application Feature Map (All Five Menu Options)

| Option | Feature | Gmail API | YouTube Data API v3 |
|---|---|---|---|
| 1 | Create playlists from Gmail notifications | ✅ `messages.list`, `messages.get` | ✅ `videos.list`, `playlists.insert`, `playlistItems.insert` |
| 2 | Copy a single playlist between accounts | — | ✅ `playlists.list`, `playlists.insert`, `playlistItems.list`, `playlistItems.insert` |
| 3 | Copy ALL playlists between accounts | — | ✅ Same as option 2, repeated for every playlist |
| 4 | List subscriptions | — | ✅ `subscriptions.list` |
| 5 | Copy ALL subscriptions between accounts | — | ✅ `subscriptions.list`, `subscriptions.insert` |

---

## 5. Quota Awareness — A Key Learning Outcome

Managing the YouTube Data API v3 quota (default: **10,000 units/day**) was one of the most important lessons of this project. The table below summarises the quota cost of each operation:

| Operation | Quota Units |
|---|---|
| `playlists.list` (read, per page of 50) | 1 |
| `videos.list` (read, per call) | 1 |
| `playlistItems.list` (read, per page of 50) | 1 |
| `subscriptions.list` (read, per page of 50) | 1 |
| `playlists.insert` (write) | 50 |
| `playlistItems.insert` (write) | 50 |
| `subscriptions.insert` (write) | 50 |

**Design decisions driven by quota awareness:**
1. Playlists are never duplicated — `getOrCreatePlaylistId()` checks `extantPlaylists` cache first.
2. On re-runs, videos already in the destination playlist are fetched (cheap: 1 unit) and skipped before attempting any inserts (expensive: 50 units each).
3. `GoogleJsonResponseException` is caught at the copy-all level; the summary reports exactly how many playlists were copied before quota ran out.
4. The UI warns the user before the subscription copy begins: *"Each new subscription costs 50 quota units. You can add ~200 per day."*

---

## 6. Project Structure

```
src/main/java/
├── main/java/quickstart/
│   ├── AuthYouTube.java        — OAuth 2.0 flow for YouTube (multi-account)
│   ├── AuthGmail.java          — OAuth 2.0 flow for Gmail
│   ├── YouTubeMethods.java     — All YouTube Data API v3 operations
│   ├── GmailMethods.java       — All Gmail API operations
│   ├── DatabaseMethods.java    — Local persistence helpers
│   └── YouTubeProgramMain.java — Entry point; menu-driven CLI
├── laboratory/
│   ├── Experimenting.java      — Sandbox for trying API calls in isolation
│   ├── Experimenting2.java
│   └── Debugging.java
└── test/
    ├── TestYouTubeMethods.java — Unit tests for YouTube methods
    └── TestGmailMethods.java   — Unit tests for Gmail methods
```

The `laboratory/` package illustrates the learning process directly: new API calls were first explored in `Experimenting.java` before being promoted to the production `YouTubeMethods.java` class.

---

## 7. How API Services Support the Learning Process

The following table maps each phase of app development to the specific API skill practised:

| Phase | What Was Built | API Skill Learned |
|---|---|---|
| Phase 1 | Gmail notification reader | REST pagination, MIME Base64 decoding, query filtering |
| Phase 2 | Playlist creator from Gmail | YouTube write operations, quota cost model, idempotency |
| Phase 3 | Playlist copier (single) | Multi-account OAuth, reading vs. writing across sessions |
| Phase 4 | Bulk playlist copier | Error handling at scale, quota budget management, re-run safety |
| Phase 5 | Subscription lister | New resource type (`subscriptions`), read-only scopes |
| Phase 6 | Subscription copier | Conflict handling (HTTP 409), bulk write strategy |

Each phase was a deliberate learning step: the developer read the official [YouTube Data API v3 Reference](https://developers.google.com/youtube/v3/docs) and [Gmail API Reference](https://developers.google.com/gmail/api/reference/rest), implemented the feature, encountered real errors (quota exhaustion, token expiry, pagination edge cases), and iterated to a working solution.

---

## 8. Launch Timeline

> **Note:** This application is a personal learning project. "Launch" means the version is functional, tested, and documented for personal use (and optionally shared on GitHub). It is not a commercial product.

| Milestone | Target Date | Description |
|---|---|---|
| **Phase 1–2 Complete** | *(Already complete)* | Gmail reader + playlist creator from notifications |
| **Phase 3–4 Complete** | *(Already complete)* | Single and bulk playlist copy between accounts |
| **Phase 5–6 Complete** | *(Already complete)* | Subscription list + bulk subscription copy |
| **v1.0 Internal Release** | **June 10, 2026** | All five menu features stable; README updated with full setup guide; all known bugs resolved |
| **v1.0 Public GitHub Release** | **June 17, 2026** | Source code published on GitHub with setup instructions, credential guide, and this implementation document |
| **v1.1 — Error Hardening** | **July 1, 2026** | Improved error messages; retry logic on transient HTTP 5xx errors; better quota reporting |
| **v1.2 — GUI Prototype (Optional)** | **August 1, 2026** | Evaluate feasibility of a simple JavaFX or web front-end to replace the CLI |

---

## 9. Summary

This project demonstrates practical, hands-on learning of two Google API services:

- **Gmail API** — reading and parsing email notifications at scale
- **YouTube Data API v3** — creating and managing playlists, playlist items, and subscriptions across multiple authenticated accounts

Every feature in the application maps directly to a real API concept (OAuth flows, resource CRUD operations, pagination, quota management), making the codebase an end-to-end record of the developer's API learning journey.

---

*Document prepared in response to a review request for API use-case investigation purposes.*  
*Date: June 3, 2026*

