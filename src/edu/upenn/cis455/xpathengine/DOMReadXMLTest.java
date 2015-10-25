package edu.upenn.cis455.xpathengine;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMReadXMLTest {
	DOMReadXML XMLreader = new DOMReadXML("http://www.w3schools.com/xml/note.xml");
	DOMReadXML HTMLreader = new DOMReadXML("http://www.oracle.com/technetwork/java/javase/downloads/index.html");
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testReadXML() {
		//fail("Not yet implemented");
		Document doc1 = XMLreader.readXML();
		assertTrue(doc1.hasChildNodes());
		NodeList nl = doc1.getChildNodes();
		Node n = nl.item(0);
		assertTrue("note".equals(n.getNodeName()));
		NodeList childnl = n.getChildNodes();
		assertTrue("#text".equals(childnl.item(0).getNodeName()));
		assertTrue("to".equals(childnl.item(1).getNodeName()));
		assertTrue("from".equals(childnl.item(3).getNodeName()));
		assertTrue("heading".equals(childnl.item(5).getNodeName()));
		assertTrue("body".equals(childnl.item(7).getNodeName()));
		
		Document doc2 = HTMLreader.readXML();
		assertTrue(doc2.hasChildNodes());
		NodeList nl2 = doc2.getChildNodes();
		Node n2 = nl2.item(0);
		assertTrue("html".equals(n2.getNodeName()));
		NodeList childnl2 = n2.getChildNodes();
		assertTrue("head".equals(childnl2.item(0).getNodeName()));
		assertTrue("body".equals(childnl2.item(1).getNodeName()));
	}

}
