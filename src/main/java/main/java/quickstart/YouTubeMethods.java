package main.java.quickstart;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.VideoListResponse;

public class YouTubeMethods {
    
	YouTube service;
    String userID = "me";
    
    // Maps playlist title -> playlist ID for all existing playlists
    public Map<String, String> extantPlaylists = new HashMap<>();
 
    public Map<String, String> getExtantPlaylists() {
		return extantPlaylists;
	}

	public YouTubeMethods (YouTube service) {
    	this.service = service;
    	
		try {
			YouTube.Playlists.List request = service.playlists().list("snippet");
			// YouTube API caps maxResults at 50
			request.setMaxResults(50L).setMine(true);
			PlaylistListResponse response = request.execute();

			while (response != null && response.getItems() != null) {
				for (Playlist p : response.getItems()) {
					this.extantPlaylists.put(p.getSnippet().getTitle(), p.getId());
				}
				String nextPage = response.getNextPageToken();
				if (nextPage == null) break;
				response = request.setPageToken(nextPage).execute();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
    }
	
    /**
     * Returns the playlist ID for an existing playlist, or null if not found.
     */
    public String getExistingPlaylistId(String title) {
        return extantPlaylists.get(title);
    }

    /**
     * Returns the playlist ID for the given title, creating the playlist if it
     * does not already exist. Returns null only on API failure.
     */
    public String getOrCreatePlaylistId(String title) {
        String existingId = getExistingPlaylistId(title);
        if (existingId != null) {
            System.out.println("Reusing existing playlist: " + title);
            return existingId;
        }
        Playlist created = createPlaylist(title);
        return created != null ? created.getId() : null;
    }

    /**
     * Create a playlist and add it to the authorized account.
     */
    public Playlist createPlaylist(String title) {

    	if (extantPlaylists.containsKey(title)) {
    		System.out.println("Playlist already exists: " + title);
    		return null;
    	}
    	
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle(title);
        playlistSnippet.setDescription("playlist for " + title);
        PlaylistStatus playlistStatus = new PlaylistStatus();
        playlistStatus.setPrivacyStatus("private");

        Playlist youTubePlaylist = new Playlist();
        youTubePlaylist.setSnippet(playlistSnippet);
        youTubePlaylist.setStatus(playlistStatus);

        YouTube.Playlists.Insert playlistInsertCommand;
        Playlist playlistInserted = null;
		
        try {
			playlistInsertCommand = service.playlists().insert("snippet,status", youTubePlaylist);
			playlistInserted = playlistInsertCommand.execute();
			extantPlaylists.put(title, playlistInserted.getId());
		} 
		catch (IOException e) {
			System.err.println("Failed to create playlist '" + title + "': " + e.getMessage());
			e.printStackTrace();
		}
        
        return playlistInserted;

    }
    
    /**
     * Create a playlist item with the specified video ID and add it to the
     * specified playlist.
     *
     * @param playlistId assign to newly created playlistitem
     * @param videoId    YouTube video id to add to playlistitem
     */
    public PlaylistItem insertPlaylistItem(String playlistId, String videoId, String playlistTitle) {

        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle(playlistTitle);
        playlistItemSnippet.setPlaylistId(playlistId);
        playlistItemSnippet.setResourceId(resourceId);

        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        YouTube.PlaylistItems.Insert playlistItemsInsertCommand;
        PlaylistItem returnedPlaylistItem = null;
		try {
			playlistItemsInsertCommand = service.playlistItems().insert("snippet,contentDetails", playlistItem);
			returnedPlaylistItem = playlistItemsInsertCommand.execute();
		} 
		catch (IOException e) {
			System.err.println("Failed to insert video " + videoId + " into playlist " + playlistId + ": " + e.getMessage());
			e.printStackTrace();
		}
        return returnedPlaylistItem;
    }
    
    public String getVideoChannel(String videoID) {
    	if (videoID == null || videoID.isEmpty()) return null;
    	try {
			YouTube.Videos.List request = service.videos().list("snippet");
			VideoListResponse response = request.setId(videoID).execute();
			if (response.getItems() == null || response.getItems().isEmpty()) {
				System.err.println("No video found for ID: " + videoID);
				return null;
			}
		    return response.getItems().get(0).getSnippet().getChannelTitle();
		} 
    	catch (IOException e) {
    		System.err.println("Failed to get channel for video " + videoID + ": " + e.getMessage());
			e.printStackTrace();
			return null;
		}
    }

    /**
     * Retrieve the title of a playlist by its ID.
     * Returns null on failure.
     */
    public String getPlaylistTitle(String playlistId) {
        try {
            PlaylistListResponse response = service.playlists()
                    .list("snippet")
                    .setId(playlistId)
                    .execute();
            if (response.getItems() == null || response.getItems().isEmpty()) {
                System.err.println("No playlist found for ID: " + playlistId);
                return null;
            }
            return response.getItems().get(0).getSnippet().getTitle();
        } catch (IOException e) {
            System.err.println("Failed to get playlist title for " + playlistId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * List all video IDs in a playlist (handles pagination).
     * The source playlist can belong to any account as long as it is public/unlisted.
     *
     * @param playlistId the source playlist ID
     * @return ordered list of video IDs in the playlist
     */
    public java.util.List<String> listPlaylistVideoIds(String playlistId) {
        java.util.List<String> videoIds = new java.util.ArrayList<>();
        try {
            YouTube.PlaylistItems.List request = service.playlistItems()
                    .list("contentDetails")
                    .setPlaylistId(playlistId)
                    .setMaxResults(50L);

            PlaylistItemListResponse response = request.execute();

            while (response != null && response.getItems() != null) {
                for (PlaylistItem item : response.getItems()) {
                    videoIds.add(item.getContentDetails().getVideoId());
                }
                String nextPage = response.getNextPageToken();
                if (nextPage == null) break;
                response = request.setPageToken(nextPage).execute();
            }
            System.out.println("Found " + videoIds.size() + " video(s) in source playlist " + playlistId);
        } catch (IOException e) {
            System.err.println("Failed to list items for playlist " + playlistId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return videoIds;
    }

    /**
     * Copy an entire playlist: reads every video from the source playlist and
     * inserts them (in order) into a new or existing playlist on the
     * authorized account.
     *
     * @param sourcePlaylistId the ID of the playlist to copy from (can be any public/unlisted playlist)
     * @param destinationTitle the title for the destination playlist (created if it doesn't exist)
     */
    public void copyPlaylist(String sourcePlaylistId, String destinationTitle) {
        // 1. List videos from the source playlist
        java.util.List<String> videoIds = listPlaylistVideoIds(sourcePlaylistId);
        if (videoIds.isEmpty()) {
            System.out.println("Source playlist is empty or could not be read. Nothing to copy.");
            return;
        }

        // 2. Get or create the destination playlist
        String destPlaylistId = getOrCreatePlaylistId(destinationTitle);
        if (destPlaylistId == null) {
            System.err.println("Could not get/create destination playlist '" + destinationTitle + "'. Aborting.");
            return;
        }

        // 3. Insert each video into the destination playlist
        int success = 0;
        for (int i = 0; i < videoIds.size(); i++) {
            String videoId = videoIds.get(i);
            System.out.println("  Inserting video " + (i + 1) + "/" + videoIds.size() + ": " + videoId);
            PlaylistItem result = insertPlaylistItem(destPlaylistId, videoId, destinationTitle);
            if (result != null) success++;
        }
        System.out.println("Done! Copied " + success + "/" + videoIds.size()
                + " videos into playlist '" + destinationTitle + "' (" + destPlaylistId + ")");
    }
    
}
