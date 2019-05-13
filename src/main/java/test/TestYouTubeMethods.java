package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
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
	public void testCreatePlaylist() {
		String playlistTitle = "the test playlist";
		String privacy = "private";
		
		Playlist x = youtubeMethods.createPlaylist(playlistTitle);
		assertEquals(x.getSnippet().getTitle(), playlistTitle);
		assertEquals(x.getStatus().getPrivacyStatus(), privacy);
		assertEquals(x.getSnippet().getDescription(), "playlist for " + playlistTitle);
		System.out.println("playlist ID: " + x.getId());
	}
	
	//@Test
	public void testInsertPlaylistItem() {
		String videoId = "Ks-_Mh1QhMc";
		String title = "Your body language may shape who you are | Amy Cuddy";
		String playlistId = "PLwMubaXkpmaS7M8CryPgt8XkFa2_basIN";
		
		PlaylistItem x = youtubeMethods.insertPlaylistItem(playlistId, videoId, service, title);
		
		assertEquals(x.getSnippet().getTitle(), title);
		assertEquals(x.getSnippet().getResourceId().getVideoId(), videoId);
	}
	
	//@Test
	public void testGetVideoChannel() {
		String channel = youtubeMethods.getVideoChannel("Ks-_Mh1QhMc");
		assertNotNull(channel);
		assertEquals("TED", channel);
	}
}
