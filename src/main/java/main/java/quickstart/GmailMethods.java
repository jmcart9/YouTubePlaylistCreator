package main.java.quickstart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class GmailMethods {
	
	List youtubeVideos = new ArrayList();
	
	//gets youtube message IDs from user's inbox
	//and adds them to a list
	List<Message> getAllMessages(Gmail service, String userId, String query) throws IOException {
		
		ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();
		
		List<Message> messages = new ArrayList<Message>();
	    while (response.getMessages() != null) {
	      messages.addAll(response.getMessages());
	      if (response.getNextPageToken() != null) {
	        String pageToken = response.getNextPageToken();
	        response = service.users().messages().list(userId).setQ(query)
	            .setPageToken(pageToken).execute();
	      } else {
	        break;
	      }
	    }

	    for (Message message : messages) {
	      System.out.println(message.toPrettyString());
	    }

		
	    return messages;
		
	}
}
