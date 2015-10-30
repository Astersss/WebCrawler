package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HTTPClient {
	private static void parseResponse(BufferedReader br) throws IOException{
		String response;
		while((response = br.readLine())!=null){
			System.out.println(response);
		}		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		
		try{
			Socket mySocket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(mySocket.getOutputStream(),true);
			BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			
			if(mySocket !=null && out!=null&& in != null){
				/*request forms*/
				out.println("GET /servlet/xpath HTTP/1.1");
				out.println("Host: "+ hostName +":"+portNumber+"\r\n");
				out.println("\r\n");				
				/*send xpath queries and xml/html url for result*/
				out.println("POST /servlet/xpath?XPath=%2Fnote&URL=http%3A%2F%2Fwww.w3schools.com%2Fxml%2Fnote.xml HTTP/1.1");
				out.println("Host: "+ hostName +":"+portNumber+"\r\n");
				out.println("\r\n");
				parseResponse(in);
			}
			mySocket.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
