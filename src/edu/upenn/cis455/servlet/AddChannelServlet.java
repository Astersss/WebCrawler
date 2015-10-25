package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.DatabaseException;

import edu.upenn.cis455.storage.DBWrapper;

@SuppressWarnings("serial")
public class AddChannelServlet extends HttpServlet{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String name = request.getParameter("user");
		PrintWriter out = response.getWriter();
		String channel = request.getParameter("channel");
		String XPath = request.getParameter("XPath");
		String[] XPaths = XPath.split(";");
		String envDirectory = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(envDirectory);
		try{
			Set<String> x = new HashSet<String>();
			try{
				for(String s:XPaths){
					x.add(s);
					db.saveXpath(s);
				}
			}catch(DatabaseException e){}
			db.saveChannel(channel, x, name);
			RequestDispatcher rd = request.getRequestDispatcher("welcome");
			System.out.println("Add channel succeeds.");
			rd.forward(request, response);
		}catch(DatabaseException e){
			out.println("<!DOCTYPE html>");
			out.println("<html><head><title>Oops</title></head>");
			out.println("<body><form action=\"welcome\"><br>");
			out.println("[ERROR]failed to add channel<br>");
			out.println("<input type=\"submit\" value=\"back\">");
			out.println("</form></body></html>");
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
	}

}
