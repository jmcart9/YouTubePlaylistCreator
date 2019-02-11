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
    
    private List<String> videos;
    private List<Message> emailMessageList;
	
    //fill a list of email messages from the user's inbox matching the query
    public void setEmailMessageList(Gmail service, String userId,
    		String query) throws IOException {
    	ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

  	    List<Message> messages = new ArrayList<Message>();
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
  	    
  	    this.emailMessageList = messages;
  	  }
  
    public List<Message> getEmailMessageList() {
    	return this.emailMessageList;
    }
    
    //return a message given its ID
    public Message getMessage(Gmail service, String userId, String messageId)
	  	      throws IOException {
    	Message message = service.users().messages().get(userId, messageId).execute();
	
	  	//System.out.println("Message snippet: " + message.getSnippet());
	
	  	return message;
    }
  
    //return the video url of a YouTube email message
	public String getVideoUrl(Message m) {
		String messageBody64 = m.getPayload().getParts().get(0).get("body").toString();
		messageBody64 = messageBody64.substring(9, messageBody64.indexOf("\"", 10));
		Base64.Decoder decoder = Base64.getUrlDecoder();
		byte[] decoded = decoder.decode(messageBody64);
		String messageBodyS = new String(decoded);
		int i = messageBodyS.indexOf("http://www.youtube.com/watch?");
		return messageBodyS.substring(i, messageBodyS.indexOf("&", i));
	}
  
	public void createVideoList() {
		for(Message x : emailMessageList) {
			videos.add(getVideoUrl(x));
		}
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
