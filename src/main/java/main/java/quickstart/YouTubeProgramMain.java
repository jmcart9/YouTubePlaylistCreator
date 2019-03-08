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
    	
    	GmailMethods gmailMethods = new GmailMethods();
    	
    	Credential credential = AuthGmail.authorize();
    	
    	// Build a new authorized API client service.
    	
    	Gmail service = new Gmail.Builder(AuthGmail.HTTP_TRANSPORT, AuthGmail.JSON_FACTORY, AuthGmail.authorize())
            .setApplicationName("YouTube Playlist Creator")
            .build();
    	
    	//you use this to filter out invalid messages
    	String query = "from:noreply@youtube.com \"just uploaded a video\"";
    	
        //String query = "from:noreply@youtube.com in:music";
    	//String query = "from:noreply@youtube.com";
    	//String query = "from:noreply@youtube.com \"Emory University\" OR \"Big Think\"";
        
    	Map<String,List<String>> uploadersAndVideos = new HashMap<String,List<String>>();
    	Set<String> uploaders = new HashSet<String>();
    	
    	int count = 0;
        gmailMethods.setEmailMessageList(service, "me", query);
        for(Message x : gmailMethods.getEmailMessageList()) {
        	Message m = gmailMethods.getMessage(service, "me", x.getId());
        	
        	long date = m.getInternalDate();
        	Date d = new Date(date);
        	DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        	format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        	String formatted = format.format(date);
        	
            String uploader = gmailMethods.getVideoUploader(gmailMethods.messageBodyToString(m));
            String videoURL = gmailMethods.getVideoUrl(gmailMethods.messageBodyToString(m));
            
            if(!uploadersAndVideos.containsKey(uploader)) {
            	List<String> videoIDs = new LinkedList<String>();
            	videoIDs.add(gmailMethods.getVideoID(videoURL));
            	uploadersAndVideos.put(uploader, videoIDs);
            }
            else {
            	uploadersAndVideos.get(uploader).add(gmailMethods.getVideoID(videoURL));
            }
        	
            
            System.out.println(formatted);
        	System.out.println(videoURL);
        	System.out.println(uploader);
        	System.out.println("---");
        	
        	//to make testing faster testing
        	count++;
        	if (count > 10) break;
        }
        
        uploadersAndVideos.forEach((k,v) -> Collections.reverse(v));
        
        /*
        System.out.println(uploadersAndVideos.keySet());
        System.out.println(uploadersAndVideos.get("Emory University"));
        System.out.println(uploadersAndVideos.get("Big Think"));
       
        System.out.println(uploadersAndVideos.get("Emory University"));
        System.out.println(uploadersAndVideos.get("Big Think"));
        */
        //System.out.println(uploadersAndVideos.get("Sheet Music Boss").toString());  
              
        /*
         *
         * */
         
        YouTubeMethods youtubeMethods = new YouTubeMethods();
        
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

        
        try {
            // Authorize the request.
            Credential credentialY = AuthYouTube.authorize(scopes, "playlistupdates");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(AuthYouTube.HTTP_TRANSPORT, AuthYouTube.JSON_FACTORY, credentialY)
                    .setApplicationName("youtube-cmdline-playlistupdates-sample")
                    .build();

            // Create a new, private playlist in the authorized user's channel.
            String playlistId = YouTubeMethods.insertPlaylist(youtube, "testing playlist woo!");

            // If a valid playlist was created, add a video to that playlist.
            //YouTubeMethods.insertPlaylistItem(playlistId, VIDEO_ID, youtube);
            for(String s : list) {
            	YouTubeMethods.insertPlaylistItem(playlistId, s, youtube);
            }

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
        
        
    }

}