package edu.upenn.cis455.crawler;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;


public class XPathCrawler {
	static final Logger logger = Logger.getLogger(XPathCrawler.class);
	public static Queue<String> frontier = new LinkedList<String>();
	public static boolean isAllowed(String url){
		return false;
	}
	public static InputStream download(String s){
		return null;
	}
	public static boolean isVisited(InputStream input){
		return false;
	}
	public static void extractLinks(){
		
	}
	public static void main(String[] args){
		String startURL = args[0];
		String envDirectory = args[1];
		String maxSize = args[2];
		int size = Integer.parseInt(maxSize); 
		//String startURL = "https://dbappserv.cis.upenn.edu/crawltest.html";
		//String startURL = "http://www.cis.upenn.edu/prospective-students/index.php";
		//String envDirectory = "/usr/share/jetty/database";
		//int size = 5000000;
		frontier.add(startURL);
		CrawlerWorker worker = new CrawlerWorker(frontier, size, envDirectory);
		Thread workerThread = new Thread(worker);
		
		logger.info("start worker thread for crawling. [from background thread]");
		workerThread.start();
		
	}
}
