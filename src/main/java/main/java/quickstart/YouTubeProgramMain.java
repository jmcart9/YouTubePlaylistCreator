package main.java.quickstart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

public class YouTubeProgramMain {

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
    	
    	//GmailMethods gmailMethods = new GmailMethods();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, GmailMethods.JSON_FACTORY, GmailMethods.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(GmailMethods.APPLICATION_NAME)
                .build();
        
        //String user = "mountedczarina@gmail.com";
        String query = "The Infographics Show";
        List<Message> list = GmailMethods.listMessagesMatchingQuery(service, "me", query);
        System.out.println(list.size());
        Message m = GmailMethods.getMessage(service, "me", list.get(0).getId());
        //System.out.println( m.toPrettyString()  );
        String x = m.getPayload().getParts().get(0).get("body").toString();
        System.out.println(x);
        String xCut = x.substring(9, x.indexOf("\"", 10));
        System.out.println(xCut);
        //System.out.println(Base64.decodeBase64(xCut));
        
        Base64.Decoder decoder = Base64.getUrlDecoder();
        byte[] decoded = decoder.decode(xCut);
        System.out.println("Decoded: " + new String(decoded));
        
    }
}