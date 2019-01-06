package main.java.quickstart;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class YouTubeProgramMain {

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
    	
    	GmailMethods gmailMethods = new GmailMethods();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, gmailMethods.JSON_FACTORY, gmailMethods.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(gmailMethods.APPLICATION_NAME)
                .build();
        
        //String user = "mountedczarina@gmail.com";
        String query = "from:joreyjorey@live.com";
        List<Message> list = GmailMethods.listMessagesMatchingQuery(service, "me", query);
        System.out.println( GmailMethods.getMessage(service, "me", list.get(0).getId()).toPrettyString()  );
        
    }
}