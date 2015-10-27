package edu.upenn.cis455.xpathengine;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class XPathEngineImplTest {
	XPathEngine xe = XPathEngineFactory.getXPathEngine();
	//DOMReadXML reader = new DOMReadXML("http://www.w3schools.com/xml/note.xml");
	DOMReadXML reader = new DOMReadXML("https://dbappserv.cis.upenn.edu/crawltest/nytimes/Africa.xml");
	Document doc = reader.readXML();

	@Before
	public void setUp() throws Exception {
		//String[] s = {"/","/note","/foo/[bar]","/8ee/bar","/r/e4[[]]","/a[d/c[te  x t() = \"hello\"]]", "/note[to[text()=\"Tove\"]]","/not e /  b   od y"};
		String[] s={"/rss/channel/item/title[contains(text(),\"war\")]","/rss/channel/item/description[contains(text(),\"war\")]"};
		xe.setXPaths(s);
	}

	@Test
	public void testXPathEngineImpl() {
	}

	@Test
	public void testSetXPaths() {
	}

	@Test
	public void testIsValid() {
		//fail("Not yet implemented");
		/*
		assertFalse(xe.isValid(0));
		assertTrue(xe.isValid(1));
		assertFalse(xe.isValid(2));
		assertFalse(xe.isValid(3));
		assertFalse(xe.isValid(4));
		assertTrue(xe.isValid(5));
		assertTrue(xe.isValid(6));
		assertTrue(xe.isValid(7));
		*/
		assertTrue(xe.isValid(0));
		assertTrue(xe.isValid(1));
	}

	@Test
	public void testEvaluate() {
		//fail("Not yet implemented");
		boolean[] b = xe.evaluate(doc);
		/*
		assertFalse(b[0]);
		assertTrue(b[1]);
		assertFalse(b[2]);
		assertFalse(b[3]);
		assertFalse(b[4]);
		assertFalse(b[5]);
		assertTrue(b[6]);
		assertTrue(b[7]);
		*/
		assertFalse(b[0]);
		assertTrue(b[1]);
	}

	@Test
	public void testIsSAX() {
		assertFalse(xe.isSAX());
	}

	@Test
	public void testEvaluateSAX() {
	}

}
