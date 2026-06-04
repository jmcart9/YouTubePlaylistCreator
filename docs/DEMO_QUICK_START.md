# YouTube Playlist Creator — Quick Start Demo Guide
### How to Set Up and Run Each Feature for Demonstration

**Document Date:** June 4, 2026

---

## Prerequisites for Running Demos

### System Requirements
- Java 15 or later installed
- Windows PowerShell (or command terminal)
- Internet connection
- Google account (at least one; two recommended for full demo)
- Modern web browser (Chrome, Firefox, Safari, Edge)

### Setup Steps (One-Time)

#### Step 1: Obtain OAuth Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create new project: "YouTubePlaylistCreator"
3. Enable APIs:
   - YouTube Data API v3
   - Gmail API
4. Create OAuth 2.0 credentials:
   - Type: Desktop Application
   - Download JSON → Save as `client_secret.json` in project root
5. Ensure client ID is not empty (verify the JSON file)

#### Step 2: Clone/Open Project

```powershell
cd C:\Users\jorey\IdeaProjects\YouTubePlaylistCreator
```

#### Step 3: Build the Project

```powershell
.\gradlew build
```

If successful:
```
BUILD SUCCESSFUL in 2s
```

---

## Running the Application

### Start the App

```powershell
.\gradlew run
```

You'll see:
```
=== YouTube Playlist Creator ===
1) Create playlists from Gmail notifications
2) Copy a playlist from one YouTube account to another
3) Copy ALL playlists from one YouTube account to another
4) List channels your account is subscribed to
5) Copy ALL subscriptions from one YouTube account to another
Choose an option (1-5):
```

---

## Demo Scenario 1: Quick Feature Demo (10 minutes)

**Best for:** Demonstrating core API concepts  
**Required:** 1 Google account  
**Steps:**

### Part A: List Subscriptions (Feature 4)

```
Choose an option (1-5): 4

=== List Your Subscriptions ===
Sign in to the YouTube account...
Press Enter to continue...
```

**Observer Notes:**
- ✓ Browser automatically opens
- ✓ User signs in with Google
- ✓ Shows OAuth token caching (no re-prompt if run again)
- ✓ YouTube API `subscriptions().list()` returns all channels

**Expected Output:**
```
Subscriptions (47 total):
  1) TED-Ed                    [UCsooa4yRKGN_zVYZS-5s8Ja]
  2) YouTube Creators          [UCkR3AzVN5UH501U5Xjdu8vA]
  [...more...]
```

**What It Demonstrates:**
- OAuth 2.0 authentication
- API pagination (50 items at a time)
- Parsing JSON response (channel name + ID)

---

## Demo Scenario 2: Playlist Copy (20 minutes)

**Best for:** Showing multi-account OAuth and write operations  
**Required:** 2 Google accounts with different playlists  
**Preparation:**
1. Have Account A signed in to YouTube in a browser
2. Create a test playlist with 3-5 videos
3. Note the playlist title

### Part A: Copy Single Playlist (Feature 2)

```
Choose an option (1-5): 2

=== Copy a Playlist Between YouTube Accounts ===
Step 1: Sign in to the SOURCE account...
Press Enter to continue...
```

**At Port 8080 Browser Window:**
- Sign in as Account A
- Observe: Token stored to `tokens_source/`

```
Source account authenticated. Found X playlist(s).

Your source playlists:
  1) Your Test Playlist     [PLxxxxx]
  2) Another Playlist       [PLyyyyy]
  
Enter the source playlist ID (or number from list above): 1
```

**Observer Notes:**
- ✓ `playlists().list()` API call enumerated all playlists
- ✓ User can pick by name or ID
- ✓ Quota cost: 1 unit shown in logs

```
Step 2: Sign in to the DESTINATION account...
Press Enter to continue...
```

**At Port 8081 Browser Window:**
- Sign in as Account B (different account)
- Observe: Token stored to `tokens_dest/`

**Console Output:**

```
Copying playlist 'PLxxxxx' -> 'Your Test Playlist' ...

Step 1: Listing videos from source playlist...
Found 5 video(s)
Quota cost: 1 unit

Step 2: Creating destination playlist...
New playlist created: PLyyyyyyy
Quota cost: 50 units

Step 3: Inserting all 5 videos...
✓ Video 1/5: dQw4w9WgXcQ
✓ Video 2/5: c_zJJIQPDqo
✓ Video 3/5: .............
✓ Video 4/5: .............
✓ Video 5/5: .............

=== Copy Complete ===
Videos copied: 5/5
Total quota used: 302 units
```

**What It Demonstrates:**
- Multi-account OAuth (two separate ports, two token directories)
- API calls on source account (list) vs destination account (create + insert)
- Quota cost model (read = 1, write = 50)
- Real-time progress feedback

**Verification Step:**
- Open YouTube in second browser window
- Sign in as Account B
- Check: New playlist "Your Test Playlist" appears in their library
- Verify: All 5 videos are present in the playlist

---

## Demo Scenario 3: Quota Management (15 minutes)

**Best for:** Showing error handling and idempotency  
**Required:** Source account with 3+ small playlists (5-10 videos each)  
**Challenge:** Manually trigger quota exceeded (optional)

### Part A: Bulk Copy (Feature 3)

```
Choose an option (1-5): 3

=== Copy ALL Playlists Between YouTube Accounts ===
[... auth for source account ...]

Found 3 playlists on source account:
  1) Test Playlist 1  [PLxxxxx1] (5 videos)
  2) Test Playlist 2  [PLxxxxx2] (7 videos)
  3) Test Playlist 3  [PLxxxxx3] (6 videos)

Proceed to copy all 3 playlist(s)? (y/n): y

[... auth for destination account ...]

========================================
Playlist 1/3: Test Playlist 1
========================================
✓ Complete! 5/5 videos
Quota remaining: 9,550 units

========================================
Playlist 2/3: Test Playlist 2
========================================
✓ Complete! 7/7 videos
Quota remaining: 8,800 units

========================================
Playlist 3/3: Test Playlist 3
========================================
✓ Complete! 6/6 videos
Quota remaining: 8,050 units

=== Summary ===
Playlists copied: 3/3
Total quota used: 1,950 units
```

**Observer Notes:**
- ✓ Quota tracking shown in real-time
- ✓ Progress updates per playlist
- ✓ Summary statistics at end

### Part B: Test Idempotency (Re-run same demo)

```
Run app again, choose Feature 3

[... same auth steps ...]
[... same playlist selection ...]

========================================
Playlist 1/3: Test Playlist 1
========================================
Checking what's already in destination...
Found 5 videos already present
Skipping 5/5 videos (already copied)
✓ Complete! 0 new videos
Quota remaining: 10,000 units

========================================
Playlist 2/3: Test Playlist 2
========================================
Found 7 videos already present
Skipping 7/7 videos
✓ Complete! 0 new videos

========================================
Playlist 3/3: Test Playlist 3
========================================
Found 6 videos already present
Skipping 6/6 videos
✓ Complete! 0 new videos

=== Summary ===
Playlists copied: 3/3
Videos skipped: 18 (already present in destination)
Quota used: ~10 units (read only, no writes)
```

**What It Demonstrates:**
- **Idempotency:** Re-running doesn't duplicate content
- **Quota awareness:** Skip checking before insert saves quota (1 unit vs 50 units)
- **Error recovery:** Re-runs are safe and efficient

---

## Demo Scenario 4: Gmail Integration (25 minutes)

**Best for:** Complete end-to-end demonstration  
**Required:** 1 Google account with YouTube notification emails in Gmail inbox  
**Preparation:**
1. Account should have received 5+ YouTube notification emails
   - If not, manually subscribe to 1-2 channels and wait for notifications
   - Or ask for accounts with existing notifications

### Run Feature 1 (Create Playlists from Gmail)

```
Choose an option (1-5): 1

[... OAuth sign-in for YouTube ...]

Querying Gmail for YouTube notifications...
Gmail API: users.messages.list(q="from:noreply@youtube.com")
Found 8 email(s) with YouTube notifications

Extracting video URLs...
Found URLs:
  1. https://www.youtube.com/watch?v=xxx1
  2. https://www.youtube.com/watch?v=xxx2
  [...more...]
Total: 8 video URLs extracted

Fetching video channel information...
[████████████████████████████] 8/8 videos processed

Video → Channel Mapping:
  xxx1 → TED-Ed
  xxx2 → Vsauce
  xxx3 → Kurzgesagt
  xxx4 → TED-Ed (duplicate)
  [...more...]

Grouping by channel...
Channel Summary:
  TED-Ed         → 2 videos
  Vsauce         → 1 video
  Kurzgesamt     → 1 video
  [...more...]
Total: 4 unique channels

Creating playlists...

Processing: TED-Ed (2 videos)
  ► Creating playlist: "TED-Ed"
    Quota: 50 units
  ► Inserting 2 videos
    Quota: 100 units
  ✓ Complete!

[... continuing for other channels ...]

=== Session Complete ===
Playlists created: 4
Videos inserted: 8
Total quota used: ~650 units
```

**Observer Notes:**
- ✓ Gmail API reads notification emails
- ✓ Base64 MIME decoding extracts URLs
- ✓ Regex extracts video IDs
- ✓ YouTube API fetches video metadata
- ✓ Grouping logic creates per-channel playlists

**Verification Step:**
- Open YouTube web interface
- Check newly created playlists in library
- Verify videos are present and in correct playlists

**What It Demonstrates:**
- Multi-API integration (Gmail + YouTube)
- MIME message parsing
- Regex URL extraction
- Batch operations
- Resource grouping logic

---

## Troubleshooting During Demo

### Problem: "OAuth sign-in browser doesn't open"
**Solution:** Manually open browser to `http://localhost:8080` (or 8081)

### Problem: "Token file not found"
**Solution:** Delete token files, re-run. First auth attempt always opens browser.

### Problem: "Quota exceeded" happens too quickly
**Solution:** Check if running same feature twice in succession; quota persists for 24 hours.

### Problem: "No playlists found on source account"
**Solution:** Source account needs at least one playlist. Create one before running Feature 2/3.

### Problem: "Gmail API error: Bad credentials"
**Solution:** Ensure `client_secret.json` has valid credentials from Google Cloud Console.

---

## Key Points to Highlight During Demo

1. **OAuth 2.0 Security**
   - Passwords never stored
   - Browser handles authentication
   - Tokens automatically cached
   - Multi-account support via separate tokens

2. **API Quota Awareness**
   - Real-time quota tracking
   - User educated upfront (Feature 5: 50 units per subscription)
   - Graceful handling when quota exceeded
   - Reusability prevents waste (check before insert)

3. **Error Resilience**
   - HTTP 409 (already subscribed) caught and ignored
   - HTTP 403 (quota exceeded) reported cleanly
   - Re-runs don't duplicate content
   - Informative messages guide user ("retry after midnight PT")

4. **API Learning Progression**
   - Feature 1: Gmail reading + playlist creation
   - Feature 2: Two-account operations
   - Feature 3: Bulk operations with quota handling
   - Feature 4: Reading new resource type
   - Feature 5: Writing to new resource type

---

## Demo Checklist

Before demonstrating:
- [ ] Java 15+ installed
- [ ] `client_secret.json` in project root
- [ ] Google accounts prepared
- [ ] Test playlists created (if demoing Feature 2-3)
- [ ] Project built successfully (`gradlew build`)

During demo:
- [ ] Show each feature's console output
- [ ] Verify results in YouTube web interface
- [ ] Explain quota costs shown in console
- [ ] Highlight OAuth token files created
- [ ] Discuss API calls happening behind the scenes

---

*End of Quick Start Demo Guide*  
**See also:** IMPLEMENTATION_DOCUMENT.md, DEMO_VISUAL_WALKTHROUGH.md

