package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.*;

import org.w3c.dom.Document;

import edu.upenn.cis455.xpathengine.DOMReadXML;
import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/* TODO: Implement user interface for XPath engine here */
		System.out.println("Start processing post request");
		String XPath = request.getParameter("XPath");
		String URL = request.getParameter("URL");
		String[] XPaths = XPath.split(";");
		XPathEngine xe = XPathEngineFactory.getXPathEngine();
		xe.setXPaths(XPaths);
		DOMReadXML drx = new DOMReadXML(URL);
		Document d = drx.readXML();
		System.out.println("Document created.");
		boolean[] isMatch = xe.evaluate(d);
		int successCounter = 0;
		StringBuffer sb = new StringBuffer("Successfully matched queries are: \r\n");
		for (int i = 0; i<isMatch.length;i++){
			if(isMatch[i] == true){
				successCounter++;
				sb.append(XPaths[i]).append(";\r\n");
			}
		}
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html><head><title>Search Result</title></head>");
		if(successCounter == 0){
			out.println("<body><h1>XPath search failed!</h1><br>");
			out.println("None of the queries you entered matches the file");
			out.println("</body></html>");
		}
		else{
			out.println("<body><h1>XPath search succeeds!</h1><br>");
			out.println(sb);
			out.println("</body></html>");
		}
		out.close();
		
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/* TODO: Implement user interface for XPath engine here */
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html><head><title>Servlet User Interface</title></head>");
		out.println("<body>Linwei Chen; linweic<form method=\"post\">XPath:<br>");
		out.println("<input type=\"text\" name=\"XPath\"><br>HTML/XML document URL:<br>");
		out.println("<input type=\"text\" name=\"URL\"><br><br>");
		out.println("<input type=\"submit\" value=\"Submit\">");
		out.println("</form><p>@@@@</p></body></html>");
		out.close();
	}

}









