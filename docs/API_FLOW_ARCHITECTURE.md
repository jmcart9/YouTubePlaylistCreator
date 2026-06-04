# YouTube Playlist Creator — API Architecture & Flow Diagrams
### Visual Reference for How YouTube & Gmail APIs Are Used

**Document Date:** June 4, 2026

---

## Overall Application Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    YouTubeProgramMain (CLI)                     │
│        Menu-driven entry point for all 5 features               │
└────────────────────────┬────────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   ┌─────────┐    ┌────────────┐    ┌──────────┐
   │  Gmail  │    │ YouTube    │    │  OAuth   │
   │ Methods │    │  Methods   │    │  Config  │
   └─────────┘    └────────────┘    └──────────┘
        │                │                │
        ▼                ▼                ▼
   ┌──────────────────────────────────────────────┐
   │   Google Client Libraries (Java SDK)         │
   │   - google-api-services-gmail                │
   │   - google-api-services-youtube              │
   │   - google-oauth-client                      │
   └──────────────────────────────────────────────┘
        │                │
        ▼                ▼
   ┌──────────────────────────────────────────┐
   │      REST API Calls (HTTPS)              │
   │      gmail.googleapis.com:443             │
   │      youtube.googleapis.com:443           │
   └──────────────────────────────────────────┘
```

---

## Feature 1: Create Playlists from Gmail Notifications

### Complete API Call Sequence

```
┌─────────────────────────────────────────────────────────────────┐
│ User selects Feature 1                                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
    ┌────────────────────────────────────────────┐
    │ Step 1: OAuth 2.0 Authentication           │
    │ (User signs in via browser)                │
    └────────────────┬───────────────────────────┘
                     │
    ┌────────┐       │       ┌──────────┐
    │ Google │◄──────┼──────►│ Browser  │
    │ OAuth  │       │       │ Port 8080│
    │ Server │      │       │          │
    └────────┘       │       └──────────┘
                     │
                 (Token stored to tokens2/)
                     │
    ┌────────────────────────────────────────────┐
    │ Step 2: List Gmail Notifications           │
    │ gmail.users.messages.list()                │
    │ Query: from:noreply@youtube.com            │
    └────────────────┬───────────────────────────┘
                     │
                     ▼
    ┌──────────────────────────────────────┐
    │ Gmail API Response:                   │
    │ {                                     │
    │   "messages": [                       │
    │     {id: msg1}, {id: msg2}, ...      │
    │   ],                                  │
    │   "resultSizeEstimate": 47            │
    │ }                                     │
    │ Quota cost: 1 unit                    │
    └────────────────┬──────────────────────┘
                     │
    ┌────────────────────────────────────────────┐
    │ Step 3: Fetch Full Message Bodies (x47)    │
    │ gmail.users.messages.get(messageId)        │
    │ For each: Extract MIME payload             │
    │         Decode Base64                      │
    │         Extract body content               │
    └────────────────┬───────────────────────────┘
                     │
                     ▼
    ┌──────────────────────────────────────┐
    │ Message Response:                     │
    │ {                                     │
    │   payload: {                          │
    │     mimeType: "text/html",            │
    │     body: { data: "BASE64ENCODED..." }│
    │   }                                   │
    │ }                                     │
    │ Quota cost: Minimal (batched)         │
    └────────────────┬──────────────────────┘
                     │
    ┌────────────────────────────────────────────┐
    │ Step 4: Extract Video URLs via Regex       │
    │ Pattern: (?<=watch\?v=)[^#\&\?\n]*         │
    │ Example: dQw4w9WgXcQ                       │
    └────────────────┬───────────────────────────┘
                     │
    ┌────────────────────────────────────────────┐
    │ Step 5: Fetch Video Metadata (x47)         │
    │ youtube.videos.list("snippet")             │
    │ Extract: snippet.channelTitle               │
    └────────────────┬───────────────────────────┘
                     │
                     ▼
    ┌──────────────────────────────────────┐
    │ YouTube API Response:                 │
    │ {                                     │
    │   items: [{                           │
    │     snippet: {                        │
    │       title: "Video Title",           │
    │       channelTitle: "Rick Astley..." │
    │       channelId: "UCuAXFkgs..."      │
    │     }                                 │
    │   }]                                  │
    │ }                                     │
    │ Quota cost: 1 unit × 47 calls         │
    └────────────────┬──────────────────────┘
                     │
    ┌────────────────────────────────────────────┐
    │ Step 6: Group Videos by Channel            │
    │ Create Map<String, List<String>>           │
    │ Key: channelTitle                          │
    │ Value: [videoId1, videoId2, ...]           │
    └────────────────┬───────────────────────────┘
                     │
    ┌────────────────────────────────────────────┐
    │ Step 7: Create Playlist (x15 channels)     │
    │ youtube.playlists.insert(...)              │
    │ Set snippet.title = channelTitle            │
    │ Set status.privacyStatus = "private"        │
    └────────────────┬───────────────────────────┘
                     │
                     ▼
    ┌──────────────────────────────────────┐
    │ YouTube API Response:                 │
    │ {                                     │
    │   id: "PLxxxxxxxxxxxxxx1",             │
    │   snippet: {title: "Rick Astley..."}  │
    │ }                                     │
    │ Quota cost: 50 units × 15 playlists   │
    └────────────────┬──────────────────────┘
                     │
    ┌────────────────────────────────────────────┐
    │ Step 8: Insert Videos into Playlists (x47) │
    │ youtube.playlistItems.insert(...)          │
    │ For each: playlistId + videoId             │
    └────────────────┬───────────────────────────┘
                     │
                     ▼
    ┌──────────────────────────────────────┐
    │ YouTube API Response:                 │
    │ {                                     │
    │   id: "playlistItemId",               │
    │   snippet: {                          │
    │     playlistId: "PLxxxxx1",           │
    │     resourceId: {videoId: "xxx"}      │
    │   }                                   │
    │ }                                     │
    │ Quota cost: 50 units × 47 videos      │
    └────────────────┬──────────────────────┘
                     │
                     ▼
    ┌────────────────────────────────────┐
    │ Summary Statistics Displayed:       │
    │ - 15 playlists created             │
    │ - 47 videos inserted               │
    │ - ~3,100 quota units used          │
    └────────────────────────────────────┘
```

### API Call Cost Breakdown

```
Operation                    Count   Units Each   Total Units
─────────────────────────────────────────────────────────────
users.messages.list()            1         1            1
users.messages.get()            47    <1 batched     1
videos.list()                   47         1           47
playlists.insert()              15        50          750
playlistItems.insert()          47        50        2,350
─────────────────────────────────────────────────────────────
TOTAL QUOTA COST                                   ~3,100 units
```

---

## Feature 2: Copy Single Playlist Between Accounts

### Dual-Account API Flow

```
                     ACCOUNT A (Source)              ACCOUNT B (Destination)
                     ─────────────────────           ─────────────────────
                            │                                 │
User Input: Which Playlist?  │                                 │
                     ├───────────────────────────┐              │
                     ▼                           │              │
        ┌───────────────────────────┐            │              │
        │ playlists.list()          │◄───────────┘              │
        │ setMine(true)             │                           │
        │ maxResults: 50            │                           │
        └────────────┬──────────────┘                           │
                     │                                          │
            Response: All playlists                            │
            on Account A                                       │
                     │                                          │
User selects playlist, confirms name                           │
                     │                                          │
                     ├──────────────────►OAuth Port 8081        │
                     │                         │                │
                     │                         ▼                │
                     │              ┌──────────────────┐        │
                     │              │ Browser sign-in  │        │
                     │              │ (Account B user) │        │
                     │              └────────┬─────────┘        │
                     │                        │                │
                     │            token → tokens_dest/          │
                     │                        │                │
                     │◄───────────────────────┼────────────────┤
                     │                        │                │
        ┌────────────────────────┐    ┌──────────────────┐    │
        │playlistItems.list(id)  │    │playlists.insert()│    │
        │(from source playlist)  │    │(on dest account) │    │
        │                        │    │                  │    │
        │Response: [vid1,vid2..]│    │Response: New     │    │
        │         (127 videos)   │    │  Playlist ID     │    │
        └────────────┬───────────┘    └────────┬─────────┘    │
                     │                         │                │
                 (cached)                   (cached)            │
                     │                         │                │
                     └──────────────┬──────────┘                │
                                    │                           │
                    ┌───────────────────────────────┐           │
                    │ For each of 127 videos:       │           │
                    │ playlistItems.insert(         │           │
                    │   playlist_id_B,              │           │
                    │   video_id_A                  │           │
                    │ )                             │           │
                    │ (Quota: 50 units × 127)       │           │
                    └───────────────────────────────┘           │
                                    │                           │
                                    ▼                           │
                    ┌──────────────────────────────┐            │
                    │ Copy Complete!               │            │
                    │ 127 videos now on Account B  │            │
                    └──────────────────────────────┘            │
```

### Key Learning: Quota-Aware Deduplication

```
IF (re-running same copy):

Account A                          Account B
─────────────────────────────      ──────────────────────────
playlistItems.list(A_playlist)     
  Response: [vid1..vid127]         
       (127 videos) ◄──────┐       
                           └──────► playlistItems.list(B_playlist)
                                      Response: [vid1..vid127]
                                      (already present!)
                           ┌─────────┐
                           │ Compare │ (1 unit quota cost)
                           │ Sets    │ (vs 50 units × 127 for inserts)
                           └────┬────┘
                                │
                    ┌───────────┴────────────┐
                    │                        │
                    ▼                        ▼
            Videos to insert:       Skip inserting:
            [0 videos]              [127 videos]
            (none - all present)
                    │
              Quota used: ~2 units (instead of 6,554!)
```

---

## Feature 3: Bulk Copy with Quota Limit Handling

### Quota-Aware Loop Architecture

```
Daily Quota: 10,000 units
─────────────────────────────────────────────────────────────

For Playlist in [8 source playlists]:
  │
  ├─► playlists.insert() ────────────────── -50 units
  │
  ├─► playlistItems.list(dest) ──────────── -1 unit
  │
  ├─► playlistItems.insert() × N videos ─── -50N units
  │
  └─► total per playlist: 51 + (50 × N) units

Example execution:

Playlist 1 (50 vids):  51 + 2,500 = 2,551 units   Remaining: 7,449
Playlist 2 (23 vids):  51 + 1,150 = 1,201 units   Remaining: 6,248
Playlist 3 (15 vids):  51 +   750 =   801 units   Remaining: 5,447
Playlist 4 (89 vids):  51 + 4,450 = 4,501 units   Remaining:   946
Playlist 5 (67 vids):  51 + 3,350 = ?             QUOTA EXCEEDED!
                       └──► HttpResponse 403: quotaExceeded

┌─────────────────────────────────────────────────┐
│ Catch GoogleJsonResponseException               │
│ Message: "quotaExceeded"                        │
│ HTTP Status: 403                                │
│ Action: Break loop, show summary:               │
│   ✓ 4 playlists copied                          │
│   ✗ 1 playlist failed (quota)                   │
│   - 3 playlists not attempted                   │
│ Message: "Re-run after midnight PT"             │
└─────────────────────────────────────────────────┘

NEXT DAY: Quota resets to 10,000 units

Re-run same operation:
  ├─ Playlist 1: playlists.list() finds "already exists"
  │             Skip creation (idempotent)
  ├─ Playlist 2: Same
  ├─ Playlist 3: Same
  ├─ Playlist 4: Same
  ├─ Playlist 5: Actually insert 67 videos ──────── ~3,400 units
  └─ Playlists 6-8: Create and fill ─────────────── ~4,000 units

SUCCESS: All playlists copied, quota preserved!
```

---

## Feature 4: List Subscriptions

### Simple Read-Only Flow

```
┌────────────────────────────────┐
│ youtube.subscriptions.list()    │
│ setMine(true)                  │
│ maxResults: 50L                │
└────────────┬───────────────────┘
             │
             ▼
┌────────────────────────────────┐
│ Response (page 1):              │
│ {                              │
│   items: [                     │
│     {snippet: {                │
│       title: "TED-Ed",         │  ◄─── Channel name
│       resourceId: {            │
│         channelId: "xxxxx"     │  ◄─── Channel ID
│       }                        │
│     }},                        │
│     ... (up to 50 items) ...  │
│   ],                           │
│   nextPageToken: "NEXT_PAGE"   │
│ }                              │
│ Quota cost: 1 unit             │
└────────────┬───────────────────┘
             │
             ├─► Check nextPageToken
             │   If exists, fetch again
             │   (repeat until null)
             │
             ▼
┌────────────────────────────────┐
│ Display all subscriptions       │
│ (paginated, sorted)            │
│ Quota total: ~1 unit per 50    │
└────────────────────────────────┘
```

---

## Feature 5: Copy Subscriptions with Conflict Handling

### Multi-Stage Subscription Copy

```
Stage 1: Fetch Source Subscriptions
┌──────────────────────────────────┐
│ youtube.subscriptions.list()      │ Account A
│ (setMine(true))                  │
│ Response: [sub1, sub2, ..sub142] │
│ Quota: 3 units (pagination)      │
└────────────────┬─────────────────┘
                 │
Stage 2: Fetch Destination Subscriptions
                 │
         ┌───────────────────────────────┐
         │ youtube.subscriptions.list()  │ Account B
         │ (setMine(true))               │
         │ Response: [67 existing subs]  │
         │ Quota: 2 units                │
         └───────────────┬───────────────┘
                         │
Stage 3: Delta Calculation
                         │
         ┌───────────────────────────────┐
         │ Compare Sets:                 │
         │ 142 source - 67 existing      │
         │ = 75 new subscriptions        │
         │ (no API call)                 │
         └───────────────┬───────────────┘
                         │
Stage 4: Subscribe to 75 Channels
                         │
         ┌────────────────────────────────────┐
         │ For each of 75 channels:            │
         │                                    │
         │ youtube.subscriptions.insert()     │
         │ resourceId.channelId = channel_id  │
         │                                    │
         │ Handler:                           │
         │   HTTP 200 ──► success count++     │
         │   HTTP 409 ──► already sub, ok++   │
         │   HTTP 403 ──► quota exceeded,     │
         │                break              │
         │                                    │
         │ Quota: 50 units × 75 = 3,750 units│
         └────────────────┬───────────────────┘
                          │
Stage 5: Summary
                          │
         ┌─────────────────────────────────┐
         │ Results:                         │
         │ ✓ 75 new subscriptions added    │
         │ (67 already present)            │
         │ Quota used: 3,755 units         │
         │ Quota remaining: 6,245 units    │
         └─────────────────────────────────┘
```

### HTTP 409 Conflict Handling

```
java.code:
─────────────────────────────────────────────────

for (String channelId : to_subscribe) {
  try {
    service.subscriptions().insert("snippet", sub)
           .execute();
    ✓ count++;
  } catch (GoogleJsonResponseException e) {
    if (e.getStatusCode() == 409) {
      // Already subscribed
      // Treat as success, continue
      ✓ count++;
    } else {
      // Other errors (quota, etc.)
      throw e;  // Stop here
    }
  }
}

Effect: Re-runs are idempotent
─────────────────────────────────────────────────
  Day 1: Add 75 channels (successfully)
  Day 2: Run again with same 142 channels
         - 75 already subscribed (409s)
         - All treated as success
         - No HTTP failures
         - Quota saved: 75 × 50 units
```

---

## API Scope Requirements

### OAuth Scopes Requested

```
Feature              Scopes Needed            Why
──────────────────────────────────────────────────────
Feature 1            youtube                  Create playlists
                     youtube.readonly         Read videos
Gmail Integration    gmail                    Read messages
                     (implied)                

Feature 2-3          youtube                  Read & create
                                             playlists

Feature 4-5          youtube                  Manage subscriptions
                     (write scope needed
                      even for read because
                      of subsequent inserts)
```

**Code Reference:**
```java
// AuthYouTube.java
static List<String> scopes = Lists.newArrayList(
    "https://www.googleapis.com/auth/youtube",
    "https://www.googleapis.com/auth/youtube.readonly",
    ""  // Empty string for compatibility
);
```

---

## Quota Budget Planner

### Daily Quota: 10,000 units

```
Scenario A: Feature 1 Only
  Estimated cost:    3,100 units
  Remaining:         6,900 units
  ✓ Safe margin

Scenario B: Feature 1 + Feature 4
  Feature 1:         3,100 units
  Feature 4:            1 unit
  Total:             3,101 units
  Remaining:         6,899 units
  ✓ Safe margin

Scenario C: Feature 3 (Copy 8 playlists, ~250 videos)
  Estimated cost:    ~9,000 units
  Remaining:         1,000 units
  ⚠ Very tight - may exceed!

Scenario D: Feature 5 (Copy 100 subscriptions)
  Estimated cost:    5,000 units
  Remaining:         5,000 units
  ✓ Safe, but no room for error
```

---

## Key Learning Outcomes Shown by Diagrams

1. **OAuth 2.0 Flow**
   - Browser-based authentication
   - Token caching for future sessions
   - Multi-account support via separate tokens/ports

2. **API Resource Hierarchy**
   - Playlists contain PlaylistItems
   - Videos are referenced via ResourceId
   - Subscriptions contain resourceId (channelId)

3. **Quota Model**
   - Read operations: 1 unit
   - Write operations: 50 units
   - Pagination: counted per page, not result

4. **Error Handling**
   - HTTP 403 quotaExceeded (stop gracefully)
   - HTTP 409 already exists (retry logic)
   - Re-runs with idempotency

5. **Practical Design Patterns**
   - Check before write (deduplication)
   - Pagination loops
   - Exception mapping
   - Summary statistics

---

**Document Prepared:** June 4, 2026  
*See also: IMPLEMENTATION_DOCUMENT.md, DEMO_VISUAL_WALKTHROUGH.md*

