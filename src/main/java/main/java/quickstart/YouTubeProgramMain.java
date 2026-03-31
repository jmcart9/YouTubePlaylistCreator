package main.java.quickstart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.youtube.YouTube;

public class YouTubeProgramMain {

    public static void main(String... args) throws IOException, GeneralSecurityException {

    	Scanner scanner = new Scanner(System.in);

    	System.out.println("=== YouTube Playlist Creator ===");
    	System.out.println("1) Create playlists from Gmail notifications");
    	System.out.println("2) Copy a playlist from one YouTube account to another");
    	System.out.print("Choose an option (1 or 2): ");
    	String choice = scanner.nextLine().trim();

    	switch (choice) {
    		case "1":
    			runGmailFlow(scanner);
    			break;
    		case "2":
    			runPlaylistCopy(scanner);
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
     *   1. SOURCE account (port 8080, tokens in tokens_source/) — reads the playlist
     *   2. DESTINATION account (port 8081, tokens in tokens_dest/) — creates & writes the playlist
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
    	System.out.println("        A browser window will open — sign in with the SOURCE Google account.");
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
    	System.out.println("        A browser window will open — sign in with the DESTINATION Google account.");
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
    	System.out.println("Copying playlist '" + sourcePlaylistId + "' → '" + destTitle + "' ...");
    	destMethods.copyPlaylistFrom(sourceMethods, sourcePlaylistId, destTitle);
    }
}