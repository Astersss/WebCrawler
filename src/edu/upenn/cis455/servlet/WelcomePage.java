package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.upenn.cis455.storage.DBWrapper;

public class WelcomePage extends HttpServlet{
	static final Logger logger = Logger.getLogger(WelcomePage.class);
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String envDirectory = getServletContext().getInitParameter("BDBstore");
		response.setContentType("text/html");
		String name = request.getParameter("username");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html><head><title>Welcome Page</title></head>");
		out.println("<body><br>Welcome, " + name);
		out.println("<form action=\"logout\"><input type=\"submit\" value=\"logout\">");
		out.println("</form>");
		out.println("<form action=\"addChannel\" method=\"post\">channel:<br>");
		out.println("<input type=\"text\" name=\"channel\"><br>XPath:<br>");
		out.println("<input type=\"text\" name=\"XPath\"><br><br>");
		out.println("<input type=\"hidden\" name=\"user\" value=\""+name+"\">");
		out.println("<input type=\"submit\" value=\"Submit\">");
		DBWrapper db = new DBWrapper(envDirectory);
		Set<String> channels = db.readAllChannel();
		int length = channels.size();
		out.println("<br>Now we have "+ length + " channels<br>");
		out.println("<ul>");
		for(String channel: channels){
			out.println("<li>"+ channel);//</li>");
			String[] paths = db.readXPathByChannelName(channel);
			for(String path: paths){
				Set<Long> docs = db.readDocByXpath(path);
				for(Long doc: docs){
					HashMap<String, String> hm =db.readDocById(doc.longValue());
					logger.info("@@@@"+hm.get("Location"));
					logger.info("@@@@"+hm.get("Content"));
					if(hm.get("date")!=null)out.println("<ul><li>Crawled on: "+ hm.get("date")+"</li>");
					if(hm.get("Location")!=null)out.println("<li>Localtion: "+ hm.get("Location")+"</li>");
					if(hm.get("Content")!=null)out.println("<li><div>"+db.readDocContentByLink(hm.get("Location"))+"<div></li></ul></li><br>");
				}
			}
		}
		out.println("</ul>");
		out.println("</body></html>");
		out.close();
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
	}
}
