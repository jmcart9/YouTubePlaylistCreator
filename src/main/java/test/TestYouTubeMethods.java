package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;

import main.java.quickstart.AuthYouTube;
import main.java.quickstart.YouTubeMethods;



public class TestYouTubeMethods {
	
	static YouTubeMethods youtubeMethods;
	static YouTube service;
	
	@BeforeClass
	public static void setUp() {
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");
		try {
			Credential credential = AuthYouTube.authorize(scopes, "StoredCredentialYoutube");
			service = new YouTube.Builder(AuthYouTube.HTTP_TRANSPORT, AuthYouTube.JSON_FACTORY, credential)
	                .setApplicationName("YouTube Playlist Creator")
	                .build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		youtubeMethods = new YouTubeMethods(service);
	}
	
	//@Test
	public void testInsertPlaylist() {
		try {
			String x = youtubeMethods.insertPlaylist("testy6 ");
			System.out.println(x);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetVideoChannel() {
		String channel = youtubeMethods.getVideoChannel("Ks-_Mh1QhMc");
		assertNotNull(channel);
		assertEquals("TED", channel);
	}
}
