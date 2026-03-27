package main.java.quickstart;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

public class GmailMethods {
    
	//list of video urls
    private List<String> videoUrls;
    
    //list of emails
    private List<Message> listOfEmailMessages;
	
    Gmail service;
    String userID = "me";
    
    public GmailMethods(Gmail service){
    	this.service = service;
    	this.listOfEmailMessages = new ArrayList<>();
    	this.videoUrls = new ArrayList<>();
    }
    
    //fill a list containing email messages from the user's inbox which match the query
    public void setEmailMessageList(Gmail service, String userId, String query) {
    	List<Message> messages = new ArrayList<>();
    	try {
    		//for some reason, the messages returned below contain only the message id and thread id, not the payload or anything else
    		ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();
      	    while (response.getMessages() != null) {
      	    	messages.addAll(response.getMessages());
      	    	if (response.getNextPageToken() != null) {
      	    		String pageToken = response.getNextPageToken();
      	    		response = service.users().messages().list(userId).setQ(query).setPageToken(pageToken).execute();
      	    	} 
      	    	else {
      	    		break;
      	    	}
      	    }
      	    //get the whole message, not just the IDs
      	    messages.replaceAll(x -> getMessage(x.getId())); 	    
      	    listOfEmailMessages.addAll(messages);
    	}
    	catch(IOException e){
    		System.out.println("unable to get messages!");
    	}	    
  	  }
  
    //getter for list of emails
    public List<Message> getEmailMessageList() {
    	return this.listOfEmailMessages;
    }
    
    //return a message given its ID
    public Message getMessage(String messageID) {
    	try {
    		return service.users().messages().get(userID, messageID).execute();
    	}
    	catch(IOException e){
    		return null;
    	}
    }
    
    //messages are encoded as base64 strings, so they need to be made less ugly
    public String messageBodyToString(Message m) {
    	try {
    		String data = extractBodyData(m.getPayload());
    		if (data == null) return "";
    		byte[] decoded = Base64.getUrlDecoder().decode(data);
    		return new String(decoded, StandardCharsets.UTF_8);
    	} catch (Exception e) {
    		System.err.println("Failed to parse message body: " + e.getMessage());
    		return "";
    	}
    }

    // Recursively find the first text body data in a MIME message
    private String extractBodyData(MessagePart part) {
    	if (part == null) return null;
    	// If this part has direct body data, use it
    	if (part.getBody() != null && part.getBody().getData() != null) {
    		return part.getBody().getData();
    	}
    	// Otherwise search sub-parts (prefer text/plain)
    	if (part.getParts() != null) {
    		for (MessagePart sub : part.getParts()) {
    			if ("text/plain".equals(sub.getMimeType())
    					&& sub.getBody() != null && sub.getBody().getData() != null) {
    				return sub.getBody().getData();
    			}
    		}
    		// Fall back to first sub-part with data
    		for (MessagePart sub : part.getParts()) {
    			String data = extractBodyData(sub);
    			if (data != null) return data;
    		}
    	}
    	return null;
    }
  
    //return the video url of a YouTube email message
	public String getVideoUrl(String m) {
		if (m == null || m.isEmpty()) return "";
		// Check for both https and http
		String[] prefixes = {
			"https://www.youtube.com/watch?",
			"http://www.youtube.com/watch?"
		};
		for (String prefix : prefixes) {
			if (m.contains(prefix)) {
				int i = m.indexOf(prefix);
				int end = m.indexOf('&', i);
				if (end == -1) end = m.length();
				return m.substring(i, end);
			}
		}
		return "";
	}
  
	//create list of video urls, skipping messages that don't contain valid URLs
	public void createVideoList() {
		for(Message x : listOfEmailMessages) {
			String url = getVideoUrl(messageBodyToString(x));
			if (url != null && !url.isEmpty()) {
				videoUrls.add(url);
			}
		}
	}
	
	//return list of video urls
	public List<String> getVideoUrls(){
		return videoUrls;
	}
	
	//Robust method for extracting video id from URL.
	public String getVideoIDFromUrl(String url) {
		String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
		Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
       }
        else return null;
	}
	
}
