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
    	System.out.println("2) Copy an existing YouTube playlist to your account");
    	System.out.print("Choose an option (1 or 2): ");
    	String choice = scanner.nextLine().trim();

    	// YouTube auth is needed for both options
    	final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    	YouTube yService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY, AuthYouTube.authorize(httpTransport))
                .setApplicationName("YouTubePlaylistCreator")
                .build();
    	YouTubeMethods youtubeMethods = new YouTubeMethods(yService);

    	switch (choice) {
    		case "1":
    			runGmailFlow(youtubeMethods);
    			break;
    		case "2":
    			runPlaylistCopy(youtubeMethods, scanner);
    			break;
    		default:
    			System.out.println("Invalid option. Exiting.");
    	}

    	scanner.close();
    }

    /**
     * Original flow: read Gmail notifications and create per-uploader playlists.
     */
    private static void runGmailFlow(YouTubeMethods youtubeMethods) throws IOException, GeneralSecurityException {
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
     * New flow: copy all videos from a source playlist into a new/existing
     * playlist on the authorized account.
     *
     * The source playlist can belong to any account (as long as it is
     * public or unlisted). This uses the YouTube Data API v3 to list items
     * from the source playlist and insert them into the destination.
     */
    private static void runPlaylistCopy(YouTubeMethods youtubeMethods, Scanner scanner) {
    	System.out.println();
    	System.out.println("=== Copy / Move a YouTube Playlist ===");
    	System.out.print("Enter the source playlist ID (e.g. PLxxxxxxxxxxxxxxxx): ");
    	String sourcePlaylistId = scanner.nextLine().trim();

    	if (sourcePlaylistId.isEmpty()) {
    		System.out.println("No playlist ID entered. Exiting.");
    		return;
    	}

    	// Try to fetch the source playlist title to use as the default name
    	String sourceTitle = youtubeMethods.getPlaylistTitle(sourcePlaylistId);
    	String defaultName = (sourceTitle != null) ? sourceTitle : "Copied Playlist";

    	System.out.print("Enter a name for the destination playlist (press Enter to use '"
    			+ defaultName + "'): ");
    	String destTitle = scanner.nextLine().trim();
    	if (destTitle.isEmpty()) {
    		destTitle = defaultName;
    	}

    	System.out.println("Copying playlist '" + sourcePlaylistId + "' → '" + destTitle + "' ...");
    	youtubeMethods.copyPlaylist(sourcePlaylistId, destTitle);
    }
}