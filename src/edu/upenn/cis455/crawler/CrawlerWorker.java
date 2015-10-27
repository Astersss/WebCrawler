package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import java.net.HttpURLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
//import org.w3c.tidy.Node;




import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.crawler.info.URLInfo;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.xpathengine.DOMReadXML;
import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

public class CrawlerWorker extends Thread{
	static final Logger logger = Logger.getLogger(CrawlerWorker.class);
	private final Queue<String> frontier;
	//private String startURL;
	private int maxSize;
	private String envDirectory;
	private boolean lengthCheckedFlag = false;
	private boolean updateDB = true;
	private HashMap<String, String> headerMap;
	
	public CrawlerWorker(Queue<String> queue, int size, String dbenv){
		frontier = queue;
		//startURL = url;
		maxSize = size;
		envDirectory = dbenv;
	}
	private void setLengthCheckedFlag (boolean b){
		lengthCheckedFlag = b;
	}
	private void setHeaderMap (HashMap<String, String> map){
		headerMap = map;
	}
	private String parseResponse(BufferedReader br) throws IOException{
        String response;
        while((response = br.readLine()).trim().equals("") == false);
        StringBuffer sb = new StringBuffer();
        while((response = br.readLine())!= null){
            sb.append(response);        
        }
        return sb.toString();
	}
  
	private Document parseWithJTidy(InputStream input) throws Exception{
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tidy.parse(input, baos);
        byte[] bytearrays = baos.toByteArray();
        //System.out.println(baos.toString());
        InputStream docInput = new ByteArrayInputStream(bytearrays);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(docInput);
        return doc;
    }
	private boolean checkHeadRequest(BufferedReader br) throws IOException{
		String response;
		boolean b;
		response = br.readLine();
		logger.debug("status line: "+response);
		if(response.equals("HTTP/1.1 200 OK")){
			b = true;
		}
		else if(response.contains("301")){
			logger.info("encounter url redirect");
			headerMap = parseHeaders(br);
			b = true;
		}
		else {
			b = false;
			logger.debug(response);
		}
		//while((response = br.readLine()) != null){System.out.println(response);}
		return b;
	}
	private HashMap<String, String> parseHeaders(BufferedReader br) throws IOException{
		HashMap<String, String> hm = new HashMap<String, String>();
		String response;
		String lastkey = new String();
		String lastvalue = new String();
		while((response = br.readLine()).trim().equals("") == false){
			response.trim();
			String[] strings = response.split(":\\s");
			if(strings[0].equals(""))hm.put(lastkey, lastvalue+";"+strings[1]);
			else{
				lastkey = strings[0];
				lastvalue = strings[1];
				hm.put(lastkey, lastvalue);
			}
		}
		return hm;
	}
	
	private RobotsTxtInfo HTTPSprocessRobots(String hostname) throws IOException{
		//URLInfo urlInfo = new URLInfo(url);
		//String hostname = urlInfo.getHostName();
		//int portNum = urlInfo.getPortNo();
		String robotsURL = "https://"+hostname+"/robots.txt";
		//System.out.println(robotsURL);
		URL robotsurl = new URL(robotsURL);
		HttpsURLConnection connection = (HttpsURLConnection) robotsurl.openConnection();
		
		connection.setRequestMethod("HEAD");
		connection.setRequestProperty("User-Agent","cis455crawler");
		int statusCode = connection.getResponseCode();
		logger.debug("Robots Response code is: "+ statusCode);
		if(statusCode != 200) {
			logger.info("Does not return good response");
			return null;
		}
		logger.info("Robots head check passed.");
		connection = (HttpsURLConnection) robotsurl.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent","cis455crawler");
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		String response;
		//while((response = in.readLine()).trim().equals("") == false);
		RobotsTxtInfo robotsInfo = new RobotsTxtInfo();
		//logger.debug("----robots content-----");
        while((response = in.readLine()).trim().contains("This should be ignored by your crawler")==false){
        	response.trim();
        	//logger.debug(response);
            if(response.startsWith("Crawl-delay")){
            	String[] strings = response.split(":");
            	robotsInfo.addCrawlDelay(strings[0].trim(), Integer.valueOf(strings[1].trim()));
            }
            else if(response.startsWith("User-agent")){
            	String[] strings = response.split(":");
            	robotsInfo.addUserAgent(strings[1].trim());
            }
            else if(response.startsWith("Disallow")){
            	String[] strings = response.split(":");
            	if(strings[1].contains("*")) continue;
            	else{
            		robotsInfo.addDisallowedLink(strings[0].trim(), strings[1].trim());
            	}
            }
        }
        //logger.debug("---robots content end----");
        in.close();
        return robotsInfo;
	}
	
	private RobotsTxtInfo processRobots(String protocol, String hostname, int portNum) throws UnknownHostException, IOException{
		Socket mySocket = new Socket(hostname, portNum);
		PrintWriter out = new PrintWriter(mySocket.getOutputStream(),true);
		BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		StringBuffer sb = new StringBuffer("HEAD");
		sb.append(" ").append("/robots.txt HTTP/1.1");
		logger.debug("robots head request: "+ sb.toString());
		out.println(sb);	
		out.println("Host: "+ hostname+":"+portNum);
		out.println("User-Agent: cis455crawler\r\n");
		logger.debug("----------");
		//parseResponse(in);
		boolean headCheck=checkHeadRequest(in);
		logger.debug("Robots Head request is:"+ headCheck);
		if(headCheck == false) {
			in.close();
			out.close();
			mySocket.close();
			return null;
		}
		else{
			in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			sb = new StringBuffer("GET");
			sb.append(" ").append("/robots.txt HTTP/1.1");
			out.println(sb);
			out.println("Host: "+ hostname+":"+portNum);
			out.println("User-Agent: cis455crawler\r\n");
			
			String response;
			//while((response = in.readLine()).trim().equals("") == false);
			RobotsTxtInfo robotsInfo = new RobotsTxtInfo();
			//logger.debug("----robots content-----");
	        while((response = in.readLine()).trim().contains("This should be ignored by your crawler")==false){
	        	response.trim();
	        	//logger.debug(response);
	            if(response.startsWith("Crawl-delay")){
	            	String[] strings = response.split(":");
	            	robotsInfo.addCrawlDelay(strings[0].trim(), Integer.valueOf(strings[1].trim()));
	            }
	            else if(response.startsWith("User-agent")){
	            	String[] strings = response.split(":");
	            	robotsInfo.addUserAgent(strings[1].trim());
	            }
	            else if(response.startsWith("Disallow")){
	            	String[] strings = response.split(":");
	            	if(strings[1].contains("*")) continue;
	            	else{
	            		robotsInfo.addDisallowedLink(strings[0].trim(), strings[1].trim());
	            	}
	            }
	        }
	        //logger.debug("----robots end-----");
	        in.close();
	        out.close();
	        mySocket.close();
	        return robotsInfo;
		}
	}
	private boolean checkAgent(RobotsTxtInfo r){
		if(r.containsUserAgent("*")==false && r.containsUserAgent("cis455crawler")==false){
			System.out.println("User-Agent illegal");
			return false;
		}
		return true;
	}
	private String downloadHTTP(String url) throws UnknownHostException, IOException{
		System.out.println(url+": Downloading");
		URLInfo urlInfo = new URLInfo(url);
		String hostname = urlInfo.getHostName();
		int portNum = urlInfo.getPortNo();
		String filePath = urlInfo.getFilePath();
		logger.debug("Hostname: "+hostname);
		logger.debug("portNum: "+ portNum);
		logger.debug("filepath: " + filePath);
		RobotsTxtInfo robotsInfo = processRobots("http://", hostname, portNum);
		if(robotsInfo == null){
			logger.info("Cannot visit robots.txt information, ignore.");
			return null;
		}
		else{
			boolean check = checkAgent(robotsInfo);
			if(check == false) return null;
		}
		ArrayList<String> disallow = robotsInfo.getDisallowedLinks("Disallow");
		for(String s: disallow){
			if(filePath.startsWith(s)){
				logger.debug(filePath);
				logger.debug(s);
				logger.info("The url is disallowed by this site..");
				return null;
			}
		}
		Socket mySocket = new Socket(hostname, portNum);
		PrintWriter out = new PrintWriter(mySocket.getOutputStream(),true);
		BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		
		if(mySocket !=null && out!=null&& in != null){
			/*first send HEAD request*/
			//in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			StringBuffer sb = new StringBuffer("HEAD");
			sb.append(" ").append(filePath).append(" HTTP/1.1");
			logger.debug("head request: "+ sb.toString());
			out.println(sb);	
			out.println("Host: "+ hostname+":"+portNum);
			out.println("User-Agent: cis455crawler\r\n");
			logger.debug("----------");
			//parseResponse(in);
			boolean headCheck=checkHeadRequest(in);
			logger.debug("Head request is:"+ headCheck);
			if(headCheck == false) {
				in.close();
				out.close();
				mySocket.close();
				return null;
			}
			else if(headerMap.containsKey("Location")){
				url = headerMap.get("Location");
				urlInfo = new URLInfo(url);
				hostname = urlInfo.getHostName();
				portNum = urlInfo.getPortNo();
				filePath = urlInfo.getFilePath();
				logger.debug("----redirect link------");
				logger.debug("Hostname: "+hostname);
				logger.debug("portNum: "+ portNum);
				logger.debug("filepath: " + filePath);
				sb = new StringBuffer("HEAD");
				sb.append(" ").append(filePath).append(" HTTP/1.1");
				logger.debug("head request: "+ sb.toString());
				out.println(sb);	
				out.println("Host: "+ hostname+":"+portNum);
				out.println("User-Agent: cis455crawler\r\n");
				logger.debug("----------");
				//parseResponse(in);
				in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
				headCheck=checkHeadRequest(in);
				logger.debug("Head request is:"+ headCheck);
				if(headCheck == false) {
					in.close();
					out.close();
					mySocket.close();
					return null;
				}
			}
			logger.debug("----Headers----");
			setHeaderMap(parseHeaders(in));
			//print headers for test
			
			for(String key: headerMap.keySet()){
				logger.debug(key+":"+headerMap.get(key));
			}
			if(headerMap.containsKey("Content-Type")){
				String type = headerMap.get("Content-Type").trim();
				if(type.contains("/html")==false && type.contains("/xml")==false){
					logger.info(url+" is not html or xml file");
					in.close();
					out.close();
					mySocket.close();
					return null;
				}
			}
			else{
				logger.info("no content type in the header");
				in.close();
				out.close();
				mySocket.close();
				return null;
			}
			if(headerMap.containsKey("Content-Length")){
				String length = headerMap.get("Content-Length").trim();
				int contentLength = Integer.parseInt(length);
				if (contentLength > maxSize){
					logger.info("file bigger than maximum size.");
					in.close();
					out.close();
					mySocket.close();
					setLengthCheckedFlag(true);
					return null;
				}
			}
			else{
				setLengthCheckedFlag(false);
			}
			
			if(headerMap.containsKey("Last-Modified")){
				String date = headerMap.get("Last-Modified").trim();
				long dateTime = GMTtoLong(date);
				date = String.valueOf(dateTime);
				headerMap.put("Last-Modified", date);
			}
			else{
				headerMap.put("Last-Modified", String.valueOf(0));
			}
			boolean isDuplicated = isDuplicate(url);
			if(isDuplicated == true){
				logger.debug(url+": No Modified");
				String content = readDocContentByLink(url);
				logger.debug("Content: "+content);
				updateDB = false;
				in.close();
				out.close();
				mySocket.close();
				return content;
			}
			logger.debug("----------");
			
			in = new BufferedReader(new InputStreamReader(mySocket.getInputStream(),"UTF-8"));
			sb = new StringBuffer("GET");
			sb.append(" ").append(filePath).append(" HTTP/1.1");
			out.println(sb);
			out.println("Host: "+ hostname+":"+portNum);
			out.println("User-Agent: cis455crawler\r\n");
			logger.debug("----------");
			String content = parseResponse(in);
			in.close();
			out.close();
			mySocket.close();
			logger.info("Download completed.");
			return content;
		}
		else {
			in.close();
			out.close();
			mySocket.close();
			return null;
		}
	}
	private InputStream downloadHTTPS(String str) throws Exception{
		String new_str = "http://" + str.substring(8);
		//System.out.println(new_str);
		URLInfo urlInfo = new URLInfo(new_str);
		String hostname = urlInfo.getHostName();
		String filePath = urlInfo.getFilePath();
		RobotsTxtInfo rti = HTTPSprocessRobots(hostname);
		if(rti == null) return null;
		ArrayList<String> disallow = rti.getDisallowedLinks("Disallow");
		for(String s: disallow){
			if(filePath.startsWith(s)){
				logger.debug(filePath);
				logger.debug(s);
				logger.info("The url is disallowed by this site..");
				return null;
			}
		}
		//System.out.println(str);
		URL url = new URL(str);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		HttpsURLConnection connection = (HttpsURLConnection) con;
		connection.setRequestMethod("HEAD");
		connection.setRequestProperty("User-Agent","cis455crawler");
		int statusCode = connection.getResponseCode();
		logger.debug("Response code is: "+ statusCode);
		if(statusCode != 200 && statusCode != 301) {
			logger.info("Does not return good response");
			return null;
		}
		else if(statusCode == 301){
			logger.info("Encountered url redirect");
			str = connection.getHeaderField("Location");
			url = new URL(str);
			con = (HttpURLConnection) url.openConnection();
			connection = (HttpsURLConnection) con;
			connection.setRequestMethod("HEAD");
			connection.setRequestProperty("User-Agent","cis455crawler");
			statusCode = connection.getResponseCode();
			if(statusCode != 200){
				logger.info("Does not return good response.");
				return null;
			}
		}
		logger.info("head check passed.");
		
		int contentLength = connection.getContentLength();
		String contentType = connection.getContentType().trim();
		long lastModified = connection.getLastModified();
		if(contentType != null){
			if(contentType.contains("/html")==false && contentType.contains("/xml")==false){
				logger.info(url+" is not html or xml file");
				return null;
			}
		}
		else{
			logger.info("no content type in the header");
			return null;
		}
		if(Integer.valueOf(contentLength) != null){
			if (contentLength > maxSize){
				logger.info("file bigger than maximum size.");
				setLengthCheckedFlag(true);
				return null;
			}
		}
		else{
			setLengthCheckedFlag(false);
		}
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("Content-Type",contentType);
		hm.put("Last-Modified", String.valueOf(lastModified));
		setHeaderMap(hm);
		boolean isDuplicated = isDuplicate(str);
		if(isDuplicated == true){
			System.out.println(str+": No Modified");
			String content = readDocContentByLink(str);
			InputStream inputstream = new ByteArrayInputStream(content.getBytes());
			updateDB = false;
			return inputstream;
		}
		logger.debug("----------");
		logger.debug(str+": Downloading");
		connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent","cis455crawler");
		//in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		logger.info("Download completed.");
		return connection.getInputStream();
	}
	private Set<String> extractLinks(Node document){
		Set<String> internalLinks = new HashSet<String>();
		if(document.hasChildNodes() == true){
			NodeList nl = document.getChildNodes();
			for(int i = 0; i< nl.getLength();i++){
				Node node = nl.item(i);
				NamedNodeMap nnm = node.getAttributes();
				if(nnm == null){
					Set<String> set = extractLinks(node);
					if(set != null) internalLinks.addAll(set);
				}
				else{
					Node attr = nnm.getNamedItem("href");
					if(attr == null){
						Set<String> set = extractLinks(node);
						if(set != null) internalLinks.addAll(set);
					}
					else{
						String link = attr.getNodeValue();
						internalLinks.add(link);
					}
				}
			}
		}
		else{
			NamedNodeMap nnm = document.getAttributes();
			if(nnm == null) return null;
			else{
				Node attr = nnm.getNamedItem("href");
				if(attr == null) return null;
				else{
					String link = attr.getNodeValue();
					internalLinks.add(link);
				}
			}
		}
		if(internalLinks.isEmpty() == true) return null;
		else return internalLinks;
	}
	private Set<String> normalizeLinks(Set<String> links, String currentLink){
		Set<String> newLinks = new HashSet<String>();
		currentLink.trim();
		String currentPath;
		if(currentLink.endsWith("/")){
			currentPath = currentLink.substring(0,currentLink.length()-1);
		}
		else{
			String[] strings = currentLink.split("/");
			String last = strings[strings.length - 1]; 
	        if(last.contains(".")) currentPath = currentLink.substring(0,currentLink.length()-last.length()-1);
	        else currentPath = currentLink;
		}
		//System.out.println("currentPath: "+ currentPath);
		for(String link: links){
			link.trim();
			if(link.startsWith("http")) newLinks.add(link);
			else{
				if(link.startsWith("/")){
					String newlink = currentPath.concat(link);
					newLinks.add(newlink);
				}
				else if(link.startsWith("#")) {}
				else if(link.startsWith("..")){
					String[] sublinks = link.split("/");
					int count = 0;
					for(int i = 0; i<sublinks.length; i++){
						if(sublinks[i].equals("..") == false) break;
						else{
							count++;
						}
					}
					String relativePath = link.substring(3*count);
					String[] subpaths = currentPath.split("/");
					StringBuffer sb = new StringBuffer();
					for(int j =0; j<subpaths.length - count; j++){
						sb.append(subpaths[j]).append("/");
					}
					//System.out.println("first half:"+sb);
					//System.out.println("second half:"+relativePath);
					String newlink = sb.append(relativePath).toString();
					newLinks.add(newlink);
				}
				else if(link.startsWith("Javascript")){continue;}
				else if(link.startsWith("mailto")){continue;}
				else if(link.matches("^\\w(.*)")){
		            String newlink = currentPath.concat("/").concat(link);
		            newLinks.add(newlink);
				}
			}
		}
		return newLinks;
	}
	private long GMTtoLong(String GMTDate){
		SimpleDateFormat sdf1 = new SimpleDateFormat();
        sdf1.applyPattern("EEE, dd MMM yyyy HH:mm:ss z");
        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateOfHeader = null;
        try {
            dateOfHeader = sdf1.parse(GMTDate);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dateOfHeader.getTime();
	}
	private void storeFile(String url, String fileContent){
		DBWrapper dbWrapper = new DBWrapper(envDirectory);
		String DocFromDB = dbWrapper.readDocContentByLink(url);
		Date date = new Date();
		if(DocFromDB != null) dbWrapper.updateFile(url,fileContent,date.getTime());
		else dbWrapper.saveFile(url,fileContent, date.getTime());
	}
	private String readDocContentByLink(String link){
		DBWrapper dbWrapper = new DBWrapper(envDirectory);
		return dbWrapper.readDocContentByLink(link);
	}
	private boolean politeCheck(String link){
		return true;
	}
	private boolean isDuplicate(String link){
		DBWrapper dbWrapper = new DBWrapper(envDirectory);
		//System.out.println("Environment path: "+ envDirectory);
		long lastCrawled = dbWrapper.readDocTimeByLink(link);
		if(lastCrawled == -1){
			logger.info("link has not been crawled yet.");
			return false;
		}
		else{
			String lastModified = headerMap.get("Last-Modified");
			Long last = Long.valueOf(lastModified);
			if(last.longValue() == 0){
				logger.info("last modified time unknown, continue downloading...");
				return false;
			}
			else if(last.longValue()>lastCrawled){
				logger.info("The file has been modified after last crawling, continue downloading...");
				return false;
			}
			else{
				logger.info("The file stays the same after last crawling.");
				return true;
			}
		}
	}
	public void run(){
		//logger.info("----files in DB-----");
		//DBWrapper db = new DBWrapper(envDirectory);
		//db.readAllFile();
		logger.info("----files end----");
		while(frontier.isEmpty()==false){
			String url = frontier.remove();
			DBWrapper dbWrapper = new DBWrapper(envDirectory);
			Set<String> allChannels = dbWrapper.readAllChannel();
			for(String channel: allChannels){
				logger.info("channel:"+ channel);
				String[] allXpath = dbWrapper.readXPathByChannelName(channel);
				logger.debug("---------");
				updateDB = true;
				//System.out.println(url);
				String content;
				InputStream is;
				Document doc = null;
				if(url.startsWith("http://")){
					try {
						content = downloadHTTP(url);
						if(content == null) continue;
						if(updateDB == true) {
							storeFile(url,content);
						}
						if(headerMap.get("Content-Type").contains("/xml")){
							is = new ByteArrayInputStream(content.getBytes());
							DOMReadXML drx = new DOMReadXML(is);
							doc = drx.readXML();
						}
						else{
							is = new ByteArrayInputStream(content.getBytes());
							doc = parseWithJTidy(is);
							is.close();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(url.startsWith("https://")){
					try {
						is = downloadHTTPS(url);
						//InputStream is2 = is;
						if(is == null) continue;
						if(updateDB == true) {
							
							BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
							StringBuffer sb = new StringBuffer();
							String line;
							while((line = br.readLine())!=null){
								sb.append(line);
							}
							content = sb.toString();
							
							//logger.debug("-----");
							//logger.debug(content);
							//logger.debug("-----");
							//storeFile(url,content);
							is = new ByteArrayInputStream(content.getBytes());
						}
						//ReaderInputStream ris = new ReaderInputStream(new InputStreamReader(is,"UTF-8"));
						if(headerMap.get("Content-Type").contains("/xml")){
							DOMReadXML drx = new DOMReadXML(url);
							doc = drx.readXML();
						}
						else{
							doc = parseWithJTidy(is);
							is.close();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else continue;
				if(doc == null) break;
				logger.info("String converted to DOM.");
				logger.debug("----------");
				if(headerMap.get("Content-Type").contains("html")){
					logger.debug("This is a HTML file");
					logger.debug("Start extracting urls in file");
					Set<String> links = extractLinks(doc);
					links = normalizeLinks(links, url);
					/*
					System.out.println("-------extracted links---------");
		       		for(String link: links) System.out.println(link);
					 */
					for(String link: links){
						if(frontier.contains(link) == false){
							frontier.add(link);
						}
					}
				}
				else{
					logger.debug("This is a XML file, start matching...");
					XPathEngine xe = XPathEngineFactory.getXPathEngine();
					xe.setXPaths(allXpath);
					boolean[] isMatch = xe.evaluate(doc);
					for(int i = 0; i<isMatch.length;i++){
						if(isMatch[i] == true){
							logger.info("[SUCCESS]document matches "+ allXpath[i]);
							dbWrapper.updateXpath(allXpath[i],url);
							//dbWrapper.updateChannel(channel, allXpath[i]);
						}
						else{
							dbWrapper.deletaXpath(allXpath[i],url);
						}
					}
				}
				
				//logger.info("----files in DB-----");
				//DBWrapper db = new DBWrapper(envDirectory);
				//db.readAllFile();
			}
		}
	}
	public static void main(String[] args) throws Exception{
	}
}
