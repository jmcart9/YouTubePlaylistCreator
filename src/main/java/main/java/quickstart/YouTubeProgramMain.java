package main.java.quickstart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        
    	Map<String,List<String>> uploadersAndVideos = new HashMap<String,List<String>>();
    	Set<String> uploaders = new HashSet<String>();
    	
        String query = "from:noreply@youtube.com";
        gmailMethods.setEmailMessageList(service, "me", query);
        
        for(Message x : gmailMethods.getEmailMessageList()) {
        	//Message m = gmailMethods.getMessage(service, "me", x.getId());
 
            String uploader = gmailMethods.getVideoUploader(gmailMethods.messageBodyToString(x));
            String videoURL = gmailMethods.getVideoUrl(gmailMethods.messageBodyToString(x));
            
            if(!uploadersAndVideos.containsKey(uploader)) {
            	List<String> videoIDs = new LinkedList<String>();
            	videoIDs.add(videoURL);
            	uploadersAndVideos.put(uploader, videoIDs);
            }
            else {
            	uploadersAndVideos.get(uploader).add(videoURL);
            }
        	
        	//System.out.println(gmailMethods.getVideoUrl(m));
        	//System.out.println(gmailMethods.getVideoUploader(m));
        }
        
        System.out.println(uploadersAndVideos.keySet());      
              
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

        /*
        try {
            // Authorize the request.
            Credential credential = AuthYouTube.authorize(scopes, "playlistupdates");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(AuthYouTube.HTTP_TRANSPORT, AuthYouTube.JSON_FACTORY, credential)
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
        */
        
    }

}