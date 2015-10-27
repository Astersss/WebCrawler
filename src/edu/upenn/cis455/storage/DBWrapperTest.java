package edu.upenn.cis455.storage;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class DBWrapperTest {
	DBWrapper db = new DBWrapper("dbtest");
	HashMap<String, String> user = new HashMap<String, String>();
	
	@Before
	public void setUp() throws Exception {
		user.put("username", "br");
		user.put("password", "abc");
		//db.saveFile("https://ftoo.com", "<p>Hello World</p>", 1000);
	}

	@Test
	public void testDBWrapperString() {
	}

	@Test
	public void testSaveUser() {
		//db.saveUser(user);
	}

	@Test
	public void testSaveFile() {
		//db.saveFile("https://ftoo.com", "<p>Hello World</p>", 1000);
	}

	@Test
	public void testUpdateFile() {

	}

	@Test
	public void testReadAllFile() {

	}

	@Test
	public void testReadDocTimeByLink() {
		long time = db.readDocTimeByLink("https://ftoo.com");
		assertEquals(1000,time);
	}

	@Test
	public void testReadDocContentByLink() {
		String content = db.readDocContentByLink("https://ftoo.com");
		assertTrue(content.equals("<p>Hello World</p>"));
	}

	@Test
	public void testReadAllUser() {
		
	}

	@Test
	public void testReadUserByName() {
		Map<String,String> usrmap = db.readUserByName("br");
		assertTrue(usrmap.containsKey("br"));
		assertTrue(usrmap.get("br").equals("abc"));
		assertEquals(usrmap.keySet().size(),1);
	}

	@Test
	public void testReadXPathByChannelName() {
		
	}

	@Test
	public void testReadDocByXpath() {

	}

	@Test
	public void testReadDocById() {
		
	}

	@Test
	public void testReadAllChannel() {
		Set<String> allchannels = db.readAllChannel();
		assertTrue(allchannels.contains("sports"));
	}

	@Test
	public void testUpdateXpath() {

	}

	@Test
	public void testDeletaXpath() {

	}

	@Test
	public void testUpdateChannel() {

	}

	@Test
	public void testSaveXpath() {
		//db.saveXpath("/china/cba/yao");
		//db.saveXpath("/nba/rockets/beard");
	}

	@Test
	public void testSaveChannel() {
		/*
		String channel = "sports";
		Set<String> xpath = new HashSet<String>();
		xpath.add("/china/cba/yao");
		xpath.add("/nba/rockets/beard");
		String creator = "br";
		db.saveChannel(channel, xpath, creator);
		*/
	}

}
