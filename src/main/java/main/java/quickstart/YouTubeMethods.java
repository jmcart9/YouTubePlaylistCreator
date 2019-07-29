package main.java.quickstart;

import java.io.IOException;
import java.util.HashSet;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.VideoListResponse;

public class YouTubeMethods {
    
	YouTube service;
    String userID = "me";
    
    public HashSet<String> extantPlaylists = new HashSet<>();
 
    public HashSet<String> getExtantPlaylists() {
		return extantPlaylists;
	}

	public YouTubeMethods (YouTube service) {
    	this.service = service;
    	
    	long maxResultSize = 50L;
    	
		try {
			YouTube.Playlists.List request = service.playlists().list("snippet");
			PlaylistListResponse response = request.setMaxResults(maxResultSize).setMine(true).execute();
			
			long pages = 0;
			
			if(response.getPageInfo().getTotalResults() <= maxResultSize) {
				pages++;
			}
			else if(response.getPageInfo().getTotalResults() % maxResultSize == 0) {
				pages = response.getPageInfo().getTotalResults() / maxResultSize;
			}
			else {
				pages = response.getPageInfo().getTotalResults() / maxResultSize;
				pages++;
			}
				
			for (; pages > 0; pages--) {
				for(Playlist p : response.getItems()) {
					this.extantPlaylists.add(p.getSnippet().getTitle());
		    	}
				String nextPage = response.getNextPageToken();
				response = request.setMaxResults(2L).setMine(true).setPageToken(nextPage).execute();
				
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
    }
	
    /**
     * Create a playlist and add it to the authorized account.
     */
    public Playlist createPlaylist(String title) {

    	if (extantPlaylists.contains(title)) {
    		System.out.println("playlist already added");
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
			extantPlaylists.add(title);
		} 
        //what if the playlist cannot be created???
		catch (IOException e) {
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
			e.printStackTrace();
		}
        return returnedPlaylistItem;
    }
    
    public String getVideoChannel(String videoID) {
    	try {
			YouTube.Videos.List request = service.videos().list("snippet");
			VideoListResponse response = request.setId(videoID).execute();
			//video IDs should be unique, so the items list should contain only one object
		    return response.getItems().get(0).getSnippet().getChannelTitle();
		} 
    	catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }
    
}
