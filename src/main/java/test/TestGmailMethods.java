package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import main.java.quickstart.AuthGmail;
import main.java.quickstart.GmailMethods;

public class TestGmailMethods {
	
	static GmailMethods gmailMethods;
	static Gmail service;
	final String query = "from:noreply@youtube.com";;
	final String userID = "thisistheforestprimeval@gmail.com";
	final String messageID = "169c3b18a7d9dadb";
	
	Message message;
	
	@BeforeClass
	public static void setUp() {
		service = new Gmail.Builder(AuthGmail.HTTP_TRANSPORT, AuthGmail.JSON_FACTORY, AuthGmail.authorize())
	            .setApplicationName("YouTube Playlist Creator")
	            .build();
		gmailMethods = new GmailMethods(service);
	}
	
	//@Test
	public void testGmailService() {
		assertNotEquals(service, null);
		assertEquals(service.getApplicationName(), "YouTube Playlist Creator");
		assertNotNull(service.users());
		System.out.println("service is okay, I think: " + service.toString());
	}
	
	//@Test
	public void testSetEmailMessageList() throws IOException {
		
		List<Message> list = gmailMethods.getEmailMessageList();
		
		assertNull(list);
		System.out.println("unfilled email message list is null");
		
		System.out.println("filling now...");
		gmailMethods.setEmailMessageList(service, userID, query);
		list = gmailMethods.getEmailMessageList();
	
		assertNotNull(list);
		System.out.println("now the email message list is not null");
		
		assertFalse(list.isEmpty());
		System.out.println("EmailMessageList empty? " + list.isEmpty());
	}
	
	//@Test
	public void testSetEmailMessageListSIMPLE() throws IOException {
		gmailMethods.setEmailMessageList(service, userID, query);
		assertNotNull(gmailMethods.getEmailMessageList());
		assertFalse(gmailMethods.getEmailMessageList().isEmpty());
		
		//System.out.println(gmailMethods.getEmailMessageList().get(2).toPrettyString() + "a");
		//System.out.println(gmailMethods.getMessage("169c3b18a7d9dadb").toPrettyString() + "b");
		
	}
	
	//@Test
	public void testGetEmailMessageList() {
		List<Message> list = gmailMethods.getEmailMessageList();
		
		assertFalse(list.isEmpty());
		System.out.println("EmailMessageList: " + list.toString());
	}
	
	//@Test
	public void testGetMessage() {
		
		message = gmailMethods.getMessage(messageID);
		assertNotNull(message);
		
		//Files.writeString(Paths.get("output.txt"), m.toPrettyString());
		
	}
	
	//@Test
	//@Test
	public void testMessageBodyToString() {
		message = gmailMethods.getMessage(messageID);
		String out = gmailMethods.messageBodyToString(message);
		
		assertNotNull(out);
		assertNotEquals(out,"");
		System.out.println(out);
	}
	
	//@Test
	public void testGetVideoUrl() {
		String s = gmailMethods.getVideoUrl(gmailMethods.messageBodyToString(gmailMethods.getMessage(messageID)));
		assertNotNull(s);
		assertNotEquals(s,"");
		System.out.println(s);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testCreateVideoList() {
		gmailMethods.setEmailMessageList(service, userID, query);
		List list = gmailMethods.getEmailMessageList();
		list.replaceAll(x -> gmailMethods.getVideoUrl(gmailMethods.messageBodyToString((Message)x)));

		assertFalse(list.isEmpty());
		assertNotNull(list);
		list.forEach(System.out::println);
	}
	
	//@Test
	public void testgetVideoUrls(){
		
	}
	
	//@Test
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
	
	public static void main(String... args) {
		//setUp();
		//testCreateVideoList();
	}
}
