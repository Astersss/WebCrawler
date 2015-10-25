package edu.upenn.cis455.xpathengine;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;

public class DOMReadXML {
	private String filePath;
	
	private static Document parseWithJTidy(String HTML) throws Exception{
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
	    tidy.setInputEncoding("UTF-8");
	    tidy.setOutputEncoding("UTF-8");
	    tidy.setWraplen(Integer.MAX_VALUE);
	    tidy.setQuiet(true);
        //tidy.setPrintBodyOnly(false);
        tidy.setShowWarnings(false);
	    tidy.setXmlOut(true);
	    tidy.setSmartIndent(true);
	    URL url = new URL(HTML);
        URLConnection con = url.openConnection();
	    //ByteArrayInputStream inputStream = new ByteArrayInputStream(HTML.getBytes("UTF-8"));
        Document doc = tidy.parseDOM(con.getInputStream(), System.out);
	    return doc;
	    }

	
	public DOMReadXML(String path){
		this.filePath = path;
	}
	
	public Document readXML(){
		Document  doc = null;
		try{
			//File XMLFile = new File(filePath);
			if(filePath.contains(".xml")){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(filePath);
				System.out.println("xml file converted to DOM");
			}
			else if(filePath.contains(".html")){
				doc = parseWithJTidy(filePath);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return doc;
	}


}
