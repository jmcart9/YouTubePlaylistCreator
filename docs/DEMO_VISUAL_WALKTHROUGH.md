# YouTube Playlist Creator — Visual Demo Walkthrough
### Step-by-Step Visual Reference for All 5 Features with Expected Console Output

**Document Date:** June 4, 2026  
**Purpose:** Complete visual demonstration guide showing what users will see when running each feature

---

## Feature 1: Create Playlists from Gmail Notifications

### What This Does
Read YouTube notification emails from Gmail, group videos by uploader channel, and create per-channel playlists.

### Expected Console Output

```
=== YouTube Playlist Creator ===
1) Create playlists from Gmail notifications
2) Copy a playlist from one YouTube account to another
Choose an option (1-5): 1
```

#### Step 1: OAuth Browser Opens Automatically
```
[Browser window opens on http://localhost:8080]
[User sees Google Sign-In page]
[User signs in with their Google account]
[Browser redirects: "Authorization successful"]
[Token stored to tokens2/StoredCredential]
```

#### Step 2: Gmail Message Retrieval
```
Querying Gmail for YouTube notifications...
Gmail API Call: users.messages.list(q="from:noreply@youtube.com")
Response: 47 email IDs found
Quota cost: 1 unit

Fetching full message bodies (47 messages)...
[████████████████████████████] 47/47 messages fetched

Extracting video URLs from email bodies...
Found URLs:
  1. https://www.youtube.com/watch?v=dQw4w9WgXcQ
  2. https://www.youtube.com/watch?v=9bZkp7q19f0
  3. https://www.youtube.com/watch?v=jNQXAC9IVRw
  [... 44 more URLs ...]
Total: 47 video URLs extracted
```

#### Step 3: Video Metadata Lookup
```
Fetching video channel information...
YouTube API: videos().list("snippet")
[████████████████████████████] 47/47 videos processed
Quota cost: 47 units

Video → Channel Mapping:
  dQw4w9WgXcQ  →  Rick Astley Official
  9bZkp7q19f0  →  TED-Ed
  jNQXAC9IVRw  →  YouTube Creators

Grouping by channel...
Channel Summary:
  Rick Astley Official    → 5 videos
  TED-Ed                  → 8 videos
  YouTube Creators        → 3 videos
  [... 12 more channels ...]
Total: 15 unique channels
```

#### Step 4: Playlist Creation & Video Insertion
```
Creating playlists and inserting videos...

Processing: Rick Astley Official (5 videos)
  ► Creating playlist: "Rick Astley Official"
    YouTube API: playlists().insert("snippet,status", {...})
    Response: Playlist ID: PLxxxxxxxxxxxxxx1
    Quota cost: 50 units
  ► Inserting videos:
    ✓ Video 1/5: dQw4w9WgXcQ (50 units)
    ✓ Video 2/5: c_zJJIQPDqo (50 units)
    ✓ Video 3/5: ............. (50 units)
    ✓ Video 4/5: ............. (50 units)
    ✓ Video 5/5: ............. (50 units)
  ✓ Complete! 5 videos → "Rick Astley Official"

Processing: TED-Ed (8 videos)
  ► Creating playlist: "TED-Ed"
    Quota cost: 50 units
  ► Inserting 8 videos (50 × 8 = 400 units)
  ✓ Complete! 8 videos → "TED-Ed"

[... continuing for all 15 channels ...]

=== Session Complete ===
Total playlists created: 15
Total videos inserted: 47
Total quota used: ~3,100 units
Quota remaining: 6,900 units
```

---

## Feature 2: Copy a Single Playlist Between Accounts

### What This Does
Copy all videos from a playlist owned by Account A to a new playlist on Account B.

### Expected Console Output

```
Choose an option (1-5): 2

=== Copy a Playlist Between YouTube Accounts ===

Step 1: Sign in to the SOURCE account...
        A browser window will open - sign in with the SOURCE Google account.
        Press Enter to continue...
```

**User presses Enter → Browser opens for source sign-in**

```
[Token stored to tokens_source/StoredCredential]
Source account authenticated. Found 12 playlist(s).

Your source playlists:
  1) Music Videos           [PLxxxxxxxxxxxxxx1]
  2) Tutorials              [PLxxxxxxxxxxxxxx2]
  3) Tech Talks             [PLxxxxxxxxxxxxxx3]
  [... more ...]

Enter the source playlist ID (or number from list above): 1
```

**User enters 1**

```
Selected: Music Videos [PLxxxxxxxxxxxxxx1]

Source Playlist Title: "Music Videos"
Enter a name for the destination playlist (press Enter to use 'Music Videos'): 
```

**User presses Enter**

```
Step 2: Sign in to the DESTINATION account...
        Press Enter to continue...
```

**User presses Enter → Browser opens for destination sign-in**

```
[Token stored to tokens_dest/StoredCredential]
Destination account authenticated.

Copying playlist 'PLxxxxxxxxxxxxxx1' -> 'Music Videos' ...

Step 1: Listing videos from source playlist...
Found 127 video(s)
Quota cost: 3 units

Step 2: Creating destination playlist...
Playlist created: PLyyyyyyyyyyyy99
Quota cost: 50 units

Step 3: Checking what's already in destination...
Destination has 0 videos
Quota cost: 1 unit

Step 4: Inserting all 127 videos...
[████████████████████████████] 127/127 videos

✓ Complete! 127 videos copied
Total quota used: 6,553 units
Quota remaining: 3,447 units
```

---

## Feature 3: Copy ALL Playlists with Quota Management

### What This Does
Bulk copy all playlists from Account A to Account B with real-time quota tracking.

### Expected Console Output - Quota Exceeded Scenario

```
Choose an option (1-5): 3

=== Copy ALL Playlists Between YouTube Accounts ===

[... source/destination auth steps ...]

Found 8 playlists on source account:
  Music (50 vids) | Tutorials (23) | Tech (15) | Gaming (89) | 
  Documentaries (67) | Coding (42) | How-To (31) | Comedy (18)

Proceed to copy all 8 playlist(s)? (y/n): y

========================================
Playlist 1/8: Music Videos
========================================
✓ Complete! 50/50 videos
Quota remaining: 7,449 units

========================================
Playlist 2/8: Tutorials
========================================
✓ Complete! 23/23 videos
Quota remaining: 6,248 units

========================================
Playlist 3/8: Tech Talks
========================================
✓ Complete! 15/15 videos
Quota remaining: 5,447 units

========================================
Playlist 4/8: Gaming
========================================
✓ Complete! 89/89 videos
Quota remaining: 946 units

========================================
Playlist 5/8: Documentaries
========================================
Listing 67 videos from source... (1 unit)
Creating playlist... (50 units)
Attempting to insert videos...

*** YouTube API quota exceeded! ***
Error: quotaExceeded
HTTP: 403

=== Summary ===
Playlists copied:      4/8
Playlists failed:      1 (quota exceeded)
Playlists not attempted: 3

TIP: Re-run after quota resets (midnight Pacific Time).
     Already-copied playlists will be reused (not duplicated).

When you re-run:
  ✓ Playlists 1-4: Found and reused (idempotent)
  ✓ Playlist 5-8: Copied fresh
```

---

## Feature 4: List Subscriptions

### What This Does
View all YouTube channels your account is subscribed to.

### Expected Console Output

```
Choose an option (1-5): 4

=== List Your Subscriptions ===

Sign in to the YouTube account whose subscriptions you want to view.
Press Enter to continue...

[Browser opens → User signs in]
[Token stored to disk]

Fetching subscriptions...
YouTube API: subscriptions().list("snippet").setMine(true)
Found 47 subscriptions

Subscriptions (47 total):

  1) TED-Ed                         [UCsooa4yRKGN_zVYZS-5s8Ja]
  2) YouTube Creators               [UCkR3AzVN5UH501U5Xjdu8vA]
  3) 3Blue1Brown                    [UCYO_jab_esuFRV4b0je4D3A]
  4) Vsauce                         [UC6nSFpj9XLIXLF-gMvQqsyA]
  5) Veritasium                     [UCHnyfMX8NJlcQ1e6XFZRlIQ]
  [... showing more ...]
  47) Stanford                      [UCddiUEpYJcSeBZX1IubFZkw]
```

---

## Feature 5: Copy ALL Subscriptions

### What This Does
Copy all subscriptions from Account A to Account B with quota budgeting.

### Expected Console Output

```
Choose an option (1-5): 5

=== Copy ALL Subscriptions Between YouTube Accounts ===

NOTE: Each new subscription costs 50 quota units.
      The default daily quota is 10,000 units, so you can add up to ~200 new subscriptions per day.

[... auth steps for source account ...]

Found 142 subscription(s) on source account.
Proceed to copy all 142 subscription(s)? (y/n): y

[... auth steps for destination account ...]

Subscription Status:
  Source account:      142 subscriptions
  Destination account:  67 subscriptions (already present)
  New subscriptions:     75 subscriptions (to be added)

Adding new subscriptions...
[████████████████████████████] 75/75 subscriptions

  ✓ Subscribing   1/75: TED-Ed [UCsooa4yRKGN_zVYZS-5s8Ja]
  ✓ Subscribing   2/75: YouTube Creators
  [... continuing ...]
  ✓ Subscribing  75/75: Stanford

=== Subscription Copy Complete ===
New subscriptions added: 75/142
  (67 were already present - skipped)
  
Quota used: 3,750 units
Quota remaining: 6,250 units

TIP: Re-run after midnight PT if quota exceeded.
     Already-subscribed channels skipped automatically.
```

---

## What a Viewer Learns from Observing the Demo

| Concept | Visual Evidence |
|---|---|
| **OAuth 2.0** | Browser popups, token files created |
| **API Pagination** | "47 messages fetched", "found X items across 3 pages" |
| **Multi-Account Support** | Two browser windows (ports 8080 vs 8081), separate token dirs |
| **Quota Management** | Real-time quota tracking shown after each operation |
| **Error Handling** | Graceful "quota exceeded" without crashing, helpful retry message |
| **Idempotency** | "Found existing playlist, reusing..." on re-run |
| **MIME Decoding** | "Extracting video URLs from email bodies" shows Gmail integration |
| **Resource Hierarchy** | Videos listed, grouped, then inserted into playlists |

---

**Document Prepared:** June 4, 2026  
*Companion documents: IMPLEMENTATION_DOCUMENT.md, set_up.md*

