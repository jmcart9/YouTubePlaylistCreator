package test;

import org.junit.Test;

import com.google.api.services.gmail.Gmail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;

import main.java.quickstart.AuthGmail;
import main.java.quickstart.GmailMethods;

public class TestGmailMethods {
	
	GmailMethods gmailMethods;
	Gmail service;
	
	@SuppressWarnings("unused")
	@BeforeClass
	public static void setUp() {
		GmailMethods gmailMethods = new GmailMethods();
		Gmail service = new Gmail.Builder(AuthGmail.HTTP_TRANSPORT, AuthGmail.JSON_FACTORY, AuthGmail.authorize())
	            .setApplicationName("YouTube Playlist Creator")
	            .build();
	}
	
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
		assertEquals(service.getApplicationName(), "YouTube Playlist Creator");
		assertNotNull(service.users());
		System.out.println("service: " + service.toString());
	}
	
	@Test
	public void TestSetEmailMessageList() {
		String query = "from:noreply@youtube.com \"just uploaded a video\"";
		assertTrue(gmailMethods.getEmailMessageList().isEmpty());
		gmailMethods.setEmailMessageList(service, "me", query);
		assertFalse(gmailMethods.getEmailMessageList().isEmpty());
		System.out.println("EmailMessageList set?: " + gmailMethods.getEmailMessageList().isEmpty());
	}
	
	@Test
	public void TestGetEmailMessageList() {
		assertFalse(gmailMethods.getEmailMessageList().isEmpty());
		System.out.println("EmailMessageList: " + gmailMethods.getEmailMessageList().toString());
	}


	
}
