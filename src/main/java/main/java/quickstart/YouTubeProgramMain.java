package main.java.quickstart;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.youtube.YouTube;

public class YouTubeProgramMain {

    public static void main(String... args) throws IOException, GeneralSecurityException {

    	// Use UTF-8 output so non-ASCII playlist titles render correctly on Windows
    	System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
    	System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

    	Scanner scanner = new Scanner(System.in);

    	System.out.println("=== YouTube Playlist Creator ===");
    	System.out.println("1) Create playlists from Gmail notifications");
    	System.out.println("2) Copy a playlist from one YouTube account to another");
    	System.out.println("3) Copy ALL playlists from one YouTube account to another");
    	System.out.println("4) List channels your account is subscribed to");
    	System.out.println("5) Copy ALL subscriptions from one YouTube account to another");
    	System.out.print("Choose an option (1-5): ");
    	String choice = scanner.nextLine().trim();

    	switch (choice) {
    		case "1":
    			runGmailFlow(scanner);
    			break;
    		case "2":
    			runPlaylistCopy(scanner);
    			break;
    		case "3":
    			runAllPlaylistsCopy(scanner);
    			break;
    		case "4":
    			runListSubscriptions(scanner);
    			break;
    		case "5":
    			runCopySubscriptions(scanner);
    			break;
    		default:
    			System.out.println("Invalid option. Exiting.");
    	}

    	scanner.close();
    }

    /**
     * Original flow: read Gmail notifications and create per-uploader playlists.
     */
    private static void runGmailFlow(Scanner scanner) throws IOException, GeneralSecurityException {
    	final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    	YouTube yService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY, AuthYouTube.authorize(httpTransport))
                .setApplicationName("YouTubePlaylistCreator")
                .build();
    	YouTubeMethods youtubeMethods = new YouTubeMethods(yService);

    	Gmail gService = new Gmail.Builder(AuthGmail.HTTP_TRANSPORT, AuthGmail.JSON_FACTORY, AuthGmail.authorize())
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	GmailMethods gmailMethods = new GmailMethods(gService);

    	String query = "from:noreply@youtube.com";

    	//key: uploader. value: video list from uploader
    	Map<String, LinkedList<String>> uploadersAndVideos = new HashMap<>();

        gmailMethods.setEmailMessageList(gService, "me", query);
        gmailMethods.createVideoList();

        for(String url : gmailMethods.getVideoUrls()) {
        	String id = gmailMethods.getVideoIDFromUrl(url);
        	if (id == null || id.isEmpty()) {
        		System.err.println("Skipping invalid URL: " + url);
        		continue;
        	}
        	String uploader = youtubeMethods.getVideoChannel(id);
        	if (uploader == null || uploader.isEmpty()) {
        		System.err.println("Skipping video with unknown uploader: " + id);
        		continue;
        	}
        	uploadersAndVideos.computeIfAbsent(uploader, k -> new LinkedList<>()).add(id);
        }

        uploadersAndVideos.forEach((k,v) -> Collections.reverse(v));

        //create playlist by uploader
        System.out.println("Uploaders found: " + uploadersAndVideos.keySet());

        for (Map.Entry<String, LinkedList<String>> entry : uploadersAndVideos.entrySet()) {
        	String title = entry.getKey();
        	System.out.println("Processing: " + title + " (" + entry.getValue().size() + " videos)");

        	String playlistID = youtubeMethods.getOrCreatePlaylistId(title);
        	if (playlistID == null) {
        		System.err.println("Failed to get/create playlist for: " + title + ", skipping.");
        		continue;
        	}

        	for(String videoId : entry.getValue()) {
        		youtubeMethods.insertPlaylistItem(playlistID, videoId, title);
        	}
        }
    }

    /**
     * Copy a playlist from one YouTube account to another.
     *
     * Authenticates twice:
     *   1. SOURCE account (port 8080, tokens in tokens_source/) -- reads the playlist
     *   2. DESTINATION account (port 8081, tokens in tokens_dest/) -- creates & writes the playlist
     *
     * The user signs in with a different Google account each time.
     */
    private static void runPlaylistCopy(Scanner scanner) throws IOException, GeneralSecurityException {
    	System.out.println();
    	System.out.println("=== Copy a Playlist Between YouTube Accounts ===");

    	final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    	// --- Authenticate SOURCE account ---
    	System.out.println();
    	System.out.println("Step 1: Sign in to the SOURCE account (the account that owns the playlist).");
    	System.out.println("        A browser window will open - sign in with the SOURCE Google account.");
    	System.out.println("        Press Enter to continue...");
    	scanner.nextLine();

    	YouTube sourceService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY,
    			AuthYouTube.authorize(httpTransport, "source", "tokens_source", 8080))
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	YouTubeMethods sourceMethods = new YouTubeMethods(sourceService);
    	System.out.println("Source account authenticated. Found " + sourceMethods.getExtantPlaylists().size() + " playlist(s).");

    	// --- Show source playlists and let user pick ---
    	System.out.println();
    	System.out.println("Your source playlists:");
    	int idx = 1;
    	java.util.List<Map.Entry<String, String>> playlistEntries = new java.util.ArrayList<>(sourceMethods.getExtantPlaylists().entrySet());
    	for (Map.Entry<String, String> entry : playlistEntries) {
    		System.out.println("  " + idx + ") " + entry.getKey() + "  [" + entry.getValue() + "]");
    		idx++;
    	}
    	System.out.println();
    	System.out.print("Enter the source playlist ID (or number from list above): ");
    	String input = scanner.nextLine().trim();

    	String sourcePlaylistId;
    	try {
    		int num = Integer.parseInt(input);
    		if (num >= 1 && num <= playlistEntries.size()) {
    			sourcePlaylistId = playlistEntries.get(num - 1).getValue();
    		} else {
    			sourcePlaylistId = input;
    		}
    	} catch (NumberFormatException e) {
    		sourcePlaylistId = input;
    	}

    	if (sourcePlaylistId.isEmpty()) {
    		System.out.println("No playlist ID entered. Exiting.");
    		return;
    	}

    	// Fetch the source playlist title to suggest a default name
    	String sourceTitle = sourceMethods.getPlaylistTitle(sourcePlaylistId);
    	String defaultName = (sourceTitle != null) ? sourceTitle : "Copied Playlist";

    	System.out.print("Enter a name for the destination playlist (press Enter to use '"
    			+ defaultName + "'): ");
    	String destTitle = scanner.nextLine().trim();
    	if (destTitle.isEmpty()) {
    		destTitle = defaultName;
    	}

    	// --- Authenticate DESTINATION account ---
    	System.out.println();
    	System.out.println("Step 2: Sign in to the DESTINATION account (the account to copy INTO).");
    	System.out.println("        A browser window will open - sign in with the DESTINATION Google account.");
    	System.out.println("        Press Enter to continue...");
    	scanner.nextLine();

    	YouTube destService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY,
    			AuthYouTube.authorize(httpTransport, "destination", "tokens_dest", 8081))
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	YouTubeMethods destMethods = new YouTubeMethods(destService);
    	System.out.println("Destination account authenticated.");

    	// --- Copy ---
    	System.out.println();
    	System.out.println("Copying playlist '" + sourcePlaylistId + "' -> '" + destTitle + "' ...");
    	destMethods.copyPlaylistFrom(sourceMethods, sourcePlaylistId, destTitle);
    }

    /**
     * Copy ALL playlists from one YouTube account to another.
     *
     * Authenticates twice:
     *   1. SOURCE account (port 8080, tokens in tokens_source/) -- reads all playlists
     *   2. DESTINATION account (port 8081, tokens in tokens_dest/) -- creates & writes all playlists
     */
    private static void runAllPlaylistsCopy(Scanner scanner) throws IOException, GeneralSecurityException {
    	System.out.println();
    	System.out.println("=== Copy ALL Playlists Between YouTube Accounts ===");

    	final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    	// --- Authenticate SOURCE account ---
    	System.out.println();
    	System.out.println("Step 1: Sign in to the SOURCE account (the account that owns the playlists).");
    	System.out.println("        A browser window will open - sign in with the SOURCE Google account.");
    	System.out.println("        Press Enter to continue...");
    	scanner.nextLine();

    	YouTube sourceService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY,
    			AuthYouTube.authorize(httpTransport, "source", "tokens_source", 8080))
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	YouTubeMethods sourceMethods = new YouTubeMethods(sourceService);

    	java.util.List<Map.Entry<String, String>> playlistEntries =
    			new java.util.ArrayList<>(sourceMethods.getExtantPlaylists().entrySet());

    	System.out.println("Source account authenticated. Found " + playlistEntries.size() + " playlist(s):");
    	System.out.println();
    	int idx = 1;
    	for (Map.Entry<String, String> entry : playlistEntries) {
    		System.out.println("  " + idx + ") " + entry.getKey() + "  [" + entry.getValue() + "]");
    		idx++;
    	}

    	if (playlistEntries.isEmpty()) {
    		System.out.println("No playlists found on the source account. Nothing to copy.");
    		return;
    	}

    	System.out.println();
    	System.out.print("Proceed to copy all " + playlistEntries.size() + " playlist(s)? (y/n): ");
    	String confirm = scanner.nextLine().trim();
    	if (!confirm.equalsIgnoreCase("y") && !confirm.equalsIgnoreCase("yes")) {
    		System.out.println("Cancelled.");
    		return;
    	}

    	// --- Authenticate DESTINATION account ---
    	System.out.println();
    	System.out.println("Step 2: Sign in to the DESTINATION account (the account to copy INTO).");
    	System.out.println("        A browser window will open - sign in with the DESTINATION Google account.");
    	System.out.println("        Press Enter to continue...");
    	scanner.nextLine();

    	YouTube destService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY,
    			AuthYouTube.authorize(httpTransport, "destination", "tokens_dest", 8081))
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	YouTubeMethods destMethods = new YouTubeMethods(destService);
    	System.out.println("Destination account authenticated.");

    	// --- Copy all playlists ---
    	System.out.println();
    	int total = playlistEntries.size();
    	int playlistNum = 0;
    	int totalSuccess = 0;
    	int totalSkipped = 0;
    	int totalFailed = 0;
    	boolean quotaHit = false;

    	for (Map.Entry<String, String> entry : playlistEntries) {
    		playlistNum++;
    		String title = entry.getKey();
    		String sourcePlaylistId = entry.getValue();

    		System.out.println();
    		System.out.println("========================================");
    		System.out.println("Playlist " + playlistNum + "/" + total + ": " + title + "  [" + sourcePlaylistId + "]");
    		System.out.println("========================================");

    		try {
    			int copied = destMethods.copyPlaylistFrom(sourceMethods, sourcePlaylistId, title);
    			if (copied > 0) {
    				totalSuccess++;
    			} else {
    				totalSkipped++;  // empty or unreadable playlist
    			}
    		} catch (GoogleJsonResponseException e) {
    			totalFailed++;
    			int remaining = total - playlistNum;
    			System.err.println();
    			System.err.println("*** YouTube API quota exceeded! ***");
    			System.err.println("Successfully copied " + totalSuccess + " playlist(s) before hitting the limit.");
    			if (remaining > 0) {
    				System.err.println("Skipping the remaining " + remaining + " playlist(s).");
    				System.err.println("Quota resets at midnight Pacific Time. Re-run to continue where you left off.");
    			}
    			quotaHit = true;
    			break;
    		} catch (Exception e) {
    			System.err.println("Failed to copy playlist '" + title + "': " + e.getMessage());
    			e.printStackTrace();
    			totalFailed++;
    		}
    	}

    	System.out.println();
    	System.out.println("=== Summary ===");
    	System.out.println("Playlists copied:  " + totalSuccess + "/" + total);
    	if (totalSkipped > 0) {
    		System.out.println("Playlists skipped (empty): " + totalSkipped);
    	}
    	if (totalFailed > 0) {
    		System.out.println("Playlists failed:  " + totalFailed);
    	}
    	if (quotaHit) {
    		System.out.println("Playlists not attempted: " + (total - playlistNum));
    		System.out.println();
    		System.out.println("TIP: Re-run after quota resets (midnight PT) to copy the remaining playlists.");
    		System.out.println("     Already-copied playlists will be reused (not duplicated).");
    	}
    }

    /**
     * List all channels the authenticated account is subscribed to.
     */
    private static void runListSubscriptions(Scanner scanner) throws IOException, GeneralSecurityException {
    	System.out.println();
    	System.out.println("=== List Your Subscriptions ===");

    	final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    	System.out.println();
    	System.out.println("Sign in to the YouTube account whose subscriptions you want to view.");
    	System.out.println("A browser window will open. Press Enter to continue...");
    	scanner.nextLine();

    	YouTube service = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY,
    			AuthYouTube.authorize(httpTransport, "source", "tokens_source", 8080))
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	YouTubeMethods methods = new YouTubeMethods(service);

    	System.out.println();
    	System.out.println("Fetching subscriptions...");

    	try {
    		java.util.Map<String, String> subs = methods.listSubscriptions();
    		if (subs.isEmpty()) {
    			System.out.println("No subscriptions found on this account.");
    			return;
    		}
    		System.out.println();
    		System.out.println("Subscriptions (" + subs.size() + " total):");
    		int idx = 1;
    		for (java.util.Map.Entry<String, String> entry : subs.entrySet()) {
    			System.out.println("  " + idx + ") " + entry.getKey() + "  [" + entry.getValue() + "]");
    			idx++;
    		}
    	} catch (GoogleJsonResponseException e) {
    		System.err.println("API error: " + e.getMessage());
    	}
    }

    /**
     * Copy ALL subscriptions from one YouTube account to another.
     *
     * Authenticates twice:
     *   1. SOURCE account (port 8080, tokens in tokens_source/) -- reads subscriptions
     *   2. DESTINATION account (port 8081, tokens in tokens_dest/) -- creates subscriptions
     */
    private static void runCopySubscriptions(Scanner scanner) throws IOException, GeneralSecurityException {
    	System.out.println();
    	System.out.println("=== Copy ALL Subscriptions Between YouTube Accounts ===");
    	System.out.println();
    	System.out.println("NOTE: Each new subscription costs 50 quota units.");
    	System.out.println("      The default daily quota is 10,000 units, so you can add up to ~200 new subscriptions per day.");

    	final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    	// --- Authenticate SOURCE account ---
    	System.out.println();
    	System.out.println("Step 1: Sign in to the SOURCE account (the account whose subscriptions you want to copy).");
    	System.out.println("        A browser window will open - sign in with the SOURCE Google account.");
    	System.out.println("        Press Enter to continue...");
    	scanner.nextLine();

    	YouTube sourceService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY,
    			AuthYouTube.authorize(httpTransport, "source", "tokens_source", 8080))
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	YouTubeMethods sourceMethods = new YouTubeMethods(sourceService);

    	System.out.println();
    	System.out.println("Fetching source subscriptions...");
    	java.util.Map<String, String> sourceSubs;
    	try {
    		sourceSubs = sourceMethods.listSubscriptions();
    	} catch (GoogleJsonResponseException e) {
    		System.err.println("Could not fetch source subscriptions: " + e.getMessage());
    		return;
    	}

    	if (sourceSubs.isEmpty()) {
    		System.out.println("Source account has no subscriptions. Nothing to copy.");
    		return;
    	}

    	System.out.println("Found " + sourceSubs.size() + " subscription(s) on source account.");

    	System.out.println();
    	System.out.print("Proceed to copy all " + sourceSubs.size() + " subscription(s)? (y/n): ");
    	String confirm = scanner.nextLine().trim();
    	if (!confirm.equalsIgnoreCase("y") && !confirm.equalsIgnoreCase("yes")) {
    		System.out.println("Cancelled.");
    		return;
    	}

    	// --- Authenticate DESTINATION account ---
    	System.out.println();
    	System.out.println("Step 2: Sign in to the DESTINATION account (the account to subscribe FROM).");
    	System.out.println("        A browser window will open - sign in with the DESTINATION Google account.");
    	System.out.println("        Press Enter to continue...");
    	scanner.nextLine();

    	YouTube destService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY,
    			AuthYouTube.authorize(httpTransport, "destination", "tokens_dest", 8081))
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	YouTubeMethods destMethods = new YouTubeMethods(destService);
    	System.out.println("Destination account authenticated.");

    	// --- Copy all subscriptions ---
    	System.out.println();
    	try {
    		int copied = destMethods.copySubscriptionsFrom(sourceMethods);
    		System.out.println();
    		System.out.println("=== Subscription Copy Complete ===");
    		System.out.println("New subscriptions added: " + copied + "/" + sourceSubs.size());
    		if (copied < sourceSubs.size()) {
    			System.out.println("TIP: If quota was exceeded, re-run after midnight PT to continue.");
    			System.out.println("     Already-subscribed channels will be skipped automatically.");
    		}
    	} catch (GoogleJsonResponseException e) {
    		System.err.println();
    		System.err.println("*** YouTube API quota exceeded during subscription copy! ***");
    		System.err.println("Re-run after quota resets (midnight Pacific Time) to continue.");
    		System.err.println("Already-subscribed channels will be skipped automatically.");
    	}
    }
}