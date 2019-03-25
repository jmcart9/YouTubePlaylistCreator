package test;

import org.junit.Test;

import com.google.api.services.gmail.Gmail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import main.java.quickstart.AuthGmail;
import main.java.quickstart.GmailMethods;

public class TestGmailMethods {
	
	GmailMethods gmailMethods = new GmailMethods();
	Gmail service = new Gmail.Builder(AuthGmail.HTTP_TRANSPORT, AuthGmail.JSON_FACTORY, AuthGmail.authorize())
            .setApplicationName("YouTube Playlist Creator")
            .build();

	@Test
	public void testGetVideoID() {
		String url1 = "https://www.youtube.com/watch?v=oBIQbja-BPY";
		String url2 = "http://youtu.be/dQw4w9WgXcQ";
		String url3 = "http://www.youtube.com/watch?feature=player_embedded&v=xxx";
		String url4 = "https://www.youtube.com/watch?v=dm66kyU5vWA&feature=em-uploademail";
		String url5 = "https://www.youtube.com/watch?v=EOBqpDcfVW0";
		
		assertEquals("oBIQbja-BPY", gmailMethods.getVideoID(url1));
		assertEquals("dQw4w9WgXcQ", gmailMethods.getVideoID(url2));
		assertEquals("xxx", gmailMethods.getVideoID(url3));
		assertEquals("dm66kyU5vWA", gmailMethods.getVideoID(url4));
		assertEquals("EOBqpDcfVW0", gmailMethods.getVideoID(url5));
	}
	
	@Test
	public void testGmailService() {
		assertNotEquals(service, null);
		service.
	}
	
	@Test
	public void TestGetEmailMessageList() {
		assertTrue(gmailMethods.getEmailMessageList().isEmpty());
		gmailMethods.setEmailMessageList();
		assertFalse(gmailMethods.getEmailMessageList().isEmpty());
		
	}


	
}
