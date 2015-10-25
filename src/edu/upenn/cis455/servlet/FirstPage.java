package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.DatabaseException;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.Users;

public class FirstPage extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String name = request.getParameter("username");
		String password = request.getParameter("password");
		HashMap<String, String> hm = new HashMap<String, String>();
		if(request.getParameter("login_button")!=null){
			System.out.println("user pressed login");
			String envDirectory = getServletContext().getInitParameter("BDBstore");
			if(LoginValidate.isValidate(envDirectory, name, password) == true){
				RequestDispatcher rd = request.getRequestDispatcher("welcome");
				System.out.println("login successfully.");
				try {
					rd.forward(request, response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				out.println("<!DOCTYPE html>");
				out.println("<html><head><title>Oops</title></head>");
				out.println("<body><form action=\"firstpage\"><br>");
				out.println("[ERROR]Login failed, please re-enter your user credentials.<br>");
				out.println("<input type=\"submit\" value=\"back\">");
				out.println("</form></body></html>");  
			}
		}
		else if(request.getParameter("signup_button")!= null){
			System.out.println("user pressed sign up");
			if(name.equals("") || password.equals("")){
				out.println("<!DOCTYPE html>");
				out.println("<html><head><title>Oops</title></head>");
				out.println("<body><form action=\"firstpage\"><br>");
				out.println("[ERROR]Username and password fields should not be empty.<br>");
				out.println("<input type=\"submit\" value=\"back\">");
				out.println("</form></body></html>");
			}
			else{
				try{
					String envDirectory = getServletContext().getInitParameter("BDBstore");
					hm.put("username", name);
					hm.put("password", password);
					DBWrapper dbWrapper = new DBWrapper(envDirectory);
					dbWrapper.saveUser(hm);
					RequestDispatcher rd = request.getRequestDispatcher("welcome");
					try {
						rd.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//dbWrapper.readAllUser();
				}catch(DatabaseException e){
					out.println("<!DOCTYPE html>");
					out.println("<html><head><title>Oops</title></head>");
					out.println("<body><form action=\"firstpage\"><br>");
					out.println("[ERROR]Sign up failed! Username already exists!<br>");
					out.println("<input type=\"submit\" value=\"back\">");
					out.println("</form></body></html>");
				}
			}
		}
		out.close();
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String envDirectory = getServletContext().getInitParameter("BDBstore");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html><head><title>Login/Sign up</title></head>");
		out.println("<body>Linwei Chen; linweic<form method=\"post\">Username:<br>");
		out.println("<input type=\"text\" name=\"username\"><br>Password:<br>");
		out.println("<input type=\"password\" name=\"password\"><br><br>");
		out.println("<input type=\"submit\" name=\"login_button\" value=\"login\">");
		out.println("<input type=\"submit\" name=\"signup_button\" value=\"signup\">");
		out.println("</form><br>");
		DBWrapper db = new DBWrapper(envDirectory);
		Set<String> channels = db.readAllChannel();
		int length = channels.size();
		out.println("Now we have "+ length + " channels<br>");
		out.println("<ul>");
		for(String channel: channels){
			out.println("<li>"+ channel+"</li>");
		}
		out.println("</ul>");
		out.println("</body></html>");
		out.close();
	}
}
