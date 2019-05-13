package main.java.quickstart;

import java.io.IOException;
import java.util.Calendar;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.VideoListResponse;

public class YouTubeMethods {
    
	YouTube service;
    String userID = "me";
 
    public YouTubeMethods (YouTube service) {
    	this.service = service;
    }
	
    /**
     * Create a playlist and add it to the authorized account.
     */
    public Playlist createPlaylist(String title) {

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
		} 
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
    public PlaylistItem insertPlaylistItem(String playlistId, String videoId, YouTube youtube, String itemTitle) {

        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle(itemTitle);
        playlistItemSnippet.setPlaylistId(playlistId);
        playlistItemSnippet.setResourceId(resourceId);

        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        YouTube.PlaylistItems.Insert playlistItemsInsertCommand;
        PlaylistItem returnedPlaylistItem = null;
		try {
			playlistItemsInsertCommand = youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
			returnedPlaylistItem = playlistItemsInsertCommand.execute();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
        //return returnedPlaylistItem.getId();
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
