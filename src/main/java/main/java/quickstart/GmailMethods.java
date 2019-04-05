package main.java.quickstart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class GmailMethods {
    
	//list of video urls
    private List<String> videoUrls;
    
    //list of emails
    private List<Message> listOfEmailMessages;
	
    //fill a list containing email messages from the user's inbox which match the query
    public void setEmailMessageList(Gmail service, String userId, String query) {
    	List<Message> messages = new ArrayList<Message>();
    	
    	try {
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
      	  this.listOfEmailMessages = messages;
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
    public Message getMessage(Gmail service, String userID, String messageID) {
    	try {
    		return service.users().messages().get(userID, messageID).execute();
    	}
    	catch(IOException e){
    		return null;
    	}
    }
    
    //messages are encoded as base64 strings, so they need to be made less ugly
    public String messageBodyToString(Message m) {
    	String messageBody64 = m.getPayload().getParts().get(0).get("body").toString();
		messageBody64 = messageBody64.substring(9, messageBody64.indexOf("\"", 10));
		Base64.Decoder decoder = Base64.getUrlDecoder();
		byte[] decoded = decoder.decode(messageBody64);
		String messageBodyS = new String(decoded);
		return messageBodyS;
    }
  
    //return the video url of a YouTube email message
    //there should be a better way to write this.
	public String getVideoUrl(String m) {
		if(m.contains("http://www.youtube.com/watch?")) {
			int i = m.indexOf("http://www.youtube.com/watch?");
			return m.substring(i, m.indexOf("&", i));
		}
		else return ":";
		
	}
	
	//return the uploader of a video
	//there should be a better way to write this
	public String getVideoUploader(String m) {
			//if(m.contains("just uploaded a video")) {
			int i = m.indexOf("just uploaded a video");
			return m.substring(0, i-1);
			//}
			//channel names cannot have non-alphanumric characters, so...
			//else return ":";
			
	}
  
	//create list of video urls
	public void createVideoList() {
		for(Message x : listOfEmailMessages) {
			videoUrls.add(getVideoUrl(messageBodyToString(x)));
		}
	}
	
	//return list of video urls
	public List<String> getVideoUrls(){
		return videoUrls;
	}
	
	//Robust method for extracting video id from URL.
	public String getVideoID(String url) {
		String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
		Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
       }
        else return null;
	}
	
}
