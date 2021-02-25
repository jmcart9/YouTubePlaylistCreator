package main.java.quickstart;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Shared class used by every sample. Contains methods for authorizing a user and caching credentials.
 */
public class AuthYouTube {

    /**
     * Define a global instance of the HTTP transport.
     */
	public static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	

    /**
     * Define a global instance of the JSON factory.
     */
	public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * This is the directory that will be used where OAuth tokens will be stored.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens2";
    
    static List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube", "https://www.googleapis.com/auth/youtube.readonly", "");
    
    static String credentialDatastore = "StoredCredentialYoutube";
    
	private static final String CREDENTIALS_FILE_PATH = "client_secret.json";

    
    /**
     * Authorizes the installed application to access user's protected data.
     * @param httpTransport 
     *
     * @param scopes              list of scopes needed to run youtube upload.
     * @param credentialDatastore name of the credential datastore to cache OAuth tokens
     * @throws GeneralSecurityException 
     */
    public static Credential authorize(NetHttpTransport httpTransport) throws IOException, GeneralSecurityException {

        // Load client secrets.
    	InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Checks that the defaults have been replaced (Default = "Enter X here").
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential "
                            + "into src/main/resources/client_secrets.json");
            System.exit(1);
        }

        //old
        // This creates the credentials datastore at TOKENS_DIRECTORY_PATH
//        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));
//        DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes)
//        		.setCredentialDataStore(datastore)
//                .build();
        
        //from gmail
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        		httpTransport, JSON_FACTORY, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .build();

        // Build the local server and bind it to port 8080
        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

        // Authorize.
        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
    }
}
