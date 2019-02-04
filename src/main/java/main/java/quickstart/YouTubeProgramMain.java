package main.java.quickstart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;

public class YouTubeProgramMain {

    public static void main(String... args) throws IOException, GeneralSecurityException {
        
    	// Build a new authorized API client service.
    	GmailMethods gmailMethods = new GmailMethods();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, gmailMethods.JSON_FACTORY, gmailMethods.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(gmailMethods.APPLICATION_NAME)
                .build();
        
        //String user = "mountedczarina@gmail.com";
        String query = "The Infographics Show";
        gmailMethods.setEmailMessageList(service, "me", query);
        
        for(Message x : gmailMethods.getEmailMessageList()) {
        	Message m = gmailMethods.getMessage(service, "me", x.getId());
        	System.out.println(gmailMethods.getVideoUrl(m));
        }
        
        /*
         *
         * */
         
        
        YouTubeMethods youtubeMethods = new YouTubeMethods();
        YouTube youtube = youtubeMethods.getYouTubeService();
        try {
            YouTube.Channels.List channelsListByUsernameRequest = youtube.channels().list("snippet,contentDetails,statistics");
            channelsListByUsernameRequest.setForUsername("GoogleDevelopers");

            ChannelListResponse response = channelsListByUsernameRequest.execute();
            Channel channel = response.getItems().get(0);
            System.out.printf(
                "This channel's ID is %s. Its title is '%s', and it has %s views.\n",
                channel.getId(),
                channel.getSnippet().getTitle(),
                channel.getStatistics().getViewCount());
        }
        catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " +
                e.getDetails().getCode() + " : " + e.getDetails().getMessage());
        } 
        catch (Throwable t) {
            t.printStackTrace();
        }
            
    }
}