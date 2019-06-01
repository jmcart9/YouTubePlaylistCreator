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
        
    	//keys: uploader. value: video list
    	Map<String, LinkedList<String>> uploadersAndVideos = new HashMap<String, LinkedList<String>>();
    	
    	//
    	Set<String> uploaders = new HashSet<String>();
    	
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
        for (String i : uploadersAndVideos.keySet()) {
        	System.out.println(i + ": " + uploadersAndVideos.get(i).toString());
        }
        
        //create playlist by uploader
        
        //uploadersAndVideos.forEach((k,v) -> Collections.reverse(v));  
              
       
        
    }

}