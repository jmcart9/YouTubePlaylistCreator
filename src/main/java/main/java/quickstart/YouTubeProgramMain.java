package main.java.quickstart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;



public class YouTubeProgramMain {

    public static void main(String... args) throws IOException, GeneralSecurityException {
    	
    	Gmail gService = new Gmail.Builder(AuthGmail.HTTP_TRANSPORT, AuthGmail.JSON_FACTORY, AuthGmail.authorize())
    			.setApplicationName("YouTube Playlist Creator")
    			.build();
    	GmailMethods gmailMethods = new GmailMethods(gService);
    	
    	YouTube yService = new YouTube.Builder(AuthYouTube.HTTP_TRANSPORT, AuthYouTube.JSON_FACTORY, AuthYouTube.authorize())
                .setApplicationName("YouTube Playlist Creator")
                .build();
    	YouTubeMethods youtubeMethods = new YouTubeMethods(yService);
    	
    	//you use this to filter out invalid messages
    	//String query = "from:noreply@youtube.com \"just uploaded a video\"";
        //String query = "from:noreply@youtube.com in:music";
    	//String query = "from:noreply@youtube.com \"Emory University\" OR \"Big Think\"";
    	String query = "from:noreply@youtube.com";
        
    	Map<String, List<String>> uploadersAndVideos = new HashMap<String, List<String>>();
    	Set<String> uploaders = new HashSet<String>();
    	
        gmailMethods.setEmailMessageList(gService, "me", query);
        gmailMethods.createVideoList();
        
        for(String ) {
        	
        }
        
        
        uploadersAndVideos.forEach((k,v) -> Collections.reverse(v));  
              
        /*
         *
         * */
        
        // This OAuth 2.0 access scope allows for full read/write access to the
        // authenticated user's account.
        
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");
    	YouTube youtube;
    	
        String VIDEO_ID1 = "WM8bTdBs-cw";
        String VIDEO_ID2 = "PrDzd4ufypE";
        String VIDEO_ID3 = "fI1UKP6u4mo";
        ArrayList<String> list = new ArrayList<String>();
        list.add(VIDEO_ID1);
        list.add(VIDEO_ID2);
        list.add(VIDEO_ID3);

        //list of playlists from user's account
        List<Integer> playlists;
        
        //map of playlist ids and titles
        Map<String, String> titlesAndIDs = new HashMap<String, String>();
        
        try {

            // Create a new, private playlist in the authorized user's channel.
            String playlistId = youtubeMethods.createPlaylist("testing playlist woo!").getId();

            // If a valid playlist was created, add a video to that playlist.
            //YouTubeMethods.insertPlaylistItem(playlistId, VIDEO_ID, youtube);
            for(String s : list) {
            	//YouTubeMethods.insertPlaylistItem(playlistId, s, youtube);
            }

        } 
        catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
        
        
    }

}