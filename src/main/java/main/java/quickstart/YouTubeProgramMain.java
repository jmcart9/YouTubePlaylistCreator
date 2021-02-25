package main.java.quickstart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;

public class YouTubeProgramMain {

    public static void main(String... args) throws IOException, GeneralSecurityException {
    	
    	System.out.println("xxx");
    	
    	Gmail gService = new Gmail.Builder(AuthGmail.HTTP_TRANSPORT, AuthGmail.JSON_FACTORY, AuthGmail.authorize())
    			.setApplicationName("YouTubePlaylistCreator")
    			.build();
    	GmailMethods gmailMethods = new GmailMethods(gService);
    	
    	final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    	YouTube yService = new YouTube.Builder(httpTransport, AuthYouTube.JSON_FACTORY, AuthYouTube.authorize(httpTransport))
                .setApplicationName("YouTubePlaylistCreator")
                .build();
    	YouTubeMethods youtubeMethods = new YouTubeMethods(yService);
    	
    	//you use this to filter out invalid messages
    	//String query = "from:noreply@youtube.com \"just uploaded a video\"";
        //String query = "from:noreply@youtube.com in:music";
    	//String query = "from:noreply@youtube.com \"Emory University\" OR \"Big Think\"";
    	String query = "from:noreply@youtube.com";
        
    	//key: uploader. value: video list from uploader
    	Map<String, LinkedList<String>> uploadersAndVideos = new HashMap<>();
    	
    	Set<String> uploaders = new HashSet<>();
    	
        gmailMethods.setEmailMessageList(gService, "me", query);
        gmailMethods.createVideoList();
        
        for(String url : gmailMethods.getVideoUrls()) {
        	String id = gmailMethods.getVideoIDFromUrl(url);
        	String uploader = youtubeMethods.getVideoChannel(id);
        	if (!uploadersAndVideos.containsKey(uploader)) {
        		uploadersAndVideos.put(uploader, new LinkedList());
        		uploadersAndVideos.get(uploader).add(id);
        	}
        	else {
        		uploadersAndVideos.get(uploader).add(id);
        	}
        }
        
        uploadersAndVideos.forEach((k,v) -> Collections.reverse(v));  
        
        //create playlist by uploader
        
        System.out.println(uploadersAndVideos.keySet());
        for (Map.Entry<String, LinkedList<String>> i : uploadersAndVideos.entrySet()) {
        	System.out.println(i.toString());
        	
        	 String title = i.getKey();
        	 Playlist playlist =  youtubeMethods.createPlaylist(title);
        	 String playlistID = playlist.getId();
        	 
        	 for(String s : i.getValue()) {
        		 youtubeMethods.insertPlaylistItem(playlistID, s, title);
        	 }	         	 
        }
    }
}