package main.java.quickstart;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class GmailMethods {
	
	static final String APPLICATION_NAME = "YouTube Playlist Creator";
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = YouTubeProgramMain.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
	
    //return a list of messages from the user's inbox matching the query
    public static List<Message> listMessagesMatchingQuery(Gmail service, String userId,
  	      String query) throws IOException {
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
  	    	//System.out.println(message.toPrettyString());
  	    }

  	    return messages;
  	  }
  
  //return a message given its ID
  public static Message getMessage(Gmail service, String userId, String messageId)
  	      throws IOException {
  	    Message message = service.users().messages().get(userId, messageId).execute();

  	    //System.out.println("Message snippet: " + message.getSnippet());

  	    return message;
  	  }
  
  //return the video url of a YouTube email message
  public static String getVideoUrl(Message m) {
	  String messageBody64 = m.getPayload().getParts().get(0).get("body").toString();
	  messageBody64 = messageBody64.substring(9, messageBody64.indexOf("\"", 10));
	  Base64.Decoder decoder = Base64.getUrlDecoder();
      byte[] decoded = decoder.decode(messageBody64);
      String messageBodyS = new String(decoded);
      int i = messageBodyS.indexOf("http://www.youtube.com/watch?");
      return messageBodyS.substring(i, messageBodyS.indexOf("&", i));
  }
	
}
