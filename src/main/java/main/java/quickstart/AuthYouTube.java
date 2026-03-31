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
     * Uses the default token directory and port 8080.
     */
    public static Credential authorize(NetHttpTransport httpTransport) throws IOException, GeneralSecurityException {
        return authorize(httpTransport, "user", TOKENS_DIRECTORY_PATH, 8080);
    }

    /**
     * Authorizes a specific account. Each userLabel / tokensDir combination
     * stores its own OAuth tokens so multiple Google accounts can coexist.
     *
     * @param httpTransport the HTTP transport
     * @param userLabel     label stored in the credential datastore (e.g. "source", "destination")
     * @param tokensDir     directory to cache this account's OAuth tokens
     * @param port          local port for the OAuth redirect (use different ports for each account)
     */
    public static Credential authorize(NetHttpTransport httpTransport, String userLabel,
                                       String tokensDir, int port) throws IOException, GeneralSecurityException {

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

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        		httpTransport, JSON_FACTORY, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDir)))
                .build();

        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(port).build();

        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize(userLabel);
    }
}
