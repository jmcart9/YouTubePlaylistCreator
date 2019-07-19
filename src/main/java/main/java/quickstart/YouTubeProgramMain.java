package main.java.quickstart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.youtube.YouTube;



public class YouTubeProgramMain {

    public static void main(String... args) throws IOException {
    	
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
        
    	//keys: uploader. value: video list
    	Map<String, LinkedList<String>> uploadersAndVideos = new HashMap<>();
    	
    	//
    	Set<String> uploaders = new HashSet<>();
    	
        gmailMethods.setEmailMessageList(gService, "me", query);
        gmailMethods.createVideoList();
        
        //test this
        
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
        
        System.out.println(uploadersAndVideos.keySet());
        for (Map.Entry<String, LinkedList<String>> i : uploadersAndVideos.entrySet()) {
        	System.out.println(i.toString());
        }
        
        //create playlist by uploader
        
        //uploadersAndVideos.forEach((k,v) -> Collections.reverse(v));  
              
       
        
    }

}