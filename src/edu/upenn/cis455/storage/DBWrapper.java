package edu.upenn.cis455.storage;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.EntityStore;

public class DBWrapper {
	
	private static String envDirectory = null;
	
	private static Environment myEnv;
	private static EntityStore store;
	
	private static DBEnv myDbEnv = new DBEnv();
	private DataAccessor da;
	
	public DBWrapper(){}
	public DBWrapper(String path){
		envDirectory = path;
	}
	/* TODO: write object store wrapper for BerkeleyDB */
	public void saveUser(HashMap<String, String> data) throws DatabaseException{
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		Users usr = new Users();
		usr.setUsername(data.get("username"));
		usr.setPassword(data.get("password"));
		da.userById.put(usr);
		myDbEnv.close();
	}
	public void saveFile(String url, String content, long date) throws DatabaseException{
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		RetrievedDocs doc = new RetrievedDocs();
		doc.setUrl(url);
		doc.setContent(content);
		doc.setLastCrawled(date);
		da.docById.put(doc);
		myDbEnv.close();
	}
	public void updateFile(String url, String content, long date) throws DatabaseException{
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		SecondaryIndex<String, Long, RetrievedDocs> docByLink = da.docByLink;
		RetrievedDocs doc = docByLink.get(url);
		doc.setContent(content);
		doc.setLastCrawled(date);
		docByLink.getPrimaryIndex().put(doc);
		myDbEnv.close();
	}
	public void readAllFile(){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		EntityCursor<RetrievedDocs> doc_cursor = da.docById.entities();
		for(RetrievedDocs doc: doc_cursor){
			System.out.println(doc.getDocId()+" "+ doc.getUrl()+" "+ doc.getContent()+" "+doc.getLastCrawled());
		}
		doc_cursor.close();
		myDbEnv.close();
	}
	public long readDocTimeByLink(String link){
		myDbEnv.setup(envDirectory);
		//System.out.println("environment set up");
		da = new DataAccessor(myDbEnv.getEntityStore());
		SecondaryIndex<String, Long, RetrievedDocs> docByLink = da.docByLink;
		RetrievedDocs doc = docByLink.get(link);
		if(doc == null) return -1;
		long date = doc.getLastCrawled();
		myDbEnv.close();
		return date;
	}
	public String readDocContentByLink(String link){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		SecondaryIndex<String, Long, RetrievedDocs> docByLink = da.docByLink;
		RetrievedDocs doc = docByLink.get(link);
		if(doc == null) return null;
		String content = doc.getContent();
		myDbEnv.close();
		return content;
	}
	public void readAllUser(){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		EntityCursor<Users> usr_cursor = da.userById.entities();
		for(Users usr: usr_cursor){
			System.out.println(usr.getUserId()+" "+ usr.getUsername()+" "+ usr.getPassword());
		}
		usr_cursor.close();
		myDbEnv.close();
	}
	public Map<String, String> readUserByName(String name){
		//System.out.println("environment path in readUserByName method: "+envDirectory);
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		SecondaryIndex<String, Long, Users> userByName = da.userByName;
		Users user = userByName.get(name);
		if(user == null) return null;
		String password = user.getPassword();
		Map<String, String> userInfo = new HashMap<String,String>();
		userInfo.put(name, password);
		myDbEnv.close();
		return userInfo;
	}
	public String[] readXPathByChannelName(String channel){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		PrimaryIndex<String, Channels> XPathByChannel = da.channelByName;
		Channels chan = XPathByChannel.get(channel);
		Set<String> XpathSet = chan.getXPath();
		String[] Xpaths = new String[XpathSet.size()];
		int i = 0;
		for(String xpath:XpathSet){
			Xpaths[i] = xpath;
			i++;
		}
		return Xpaths;		
	}
	public Set<Long> readDocByXpath(String xpath){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		PrimaryIndex<String, XPath> XpathByExpression = da.XpathByExpression;
		XPath x = XpathByExpression.get(xpath);
		Set<Long> docSet = x.getDocuments();
		return docSet;
	}
	public HashMap<String, String> readDocById(Long Id){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		PrimaryIndex<Long, RetrievedDocs> docById = da.docById;
		RetrievedDocs doc = docById.get(Id);
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("Location", doc.getUrl());
		hm.put("Content", doc.getContent());
		long last = doc.getLastCrawled();
		Date date = new Date(last);
		hm.put("date", date.toString());
		return hm;
	}
	public Set<String> readAllChannel(){
		Set<String> allChannels = new HashSet<String>();
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		EntityCursor<Channels> channel_cursor = da.channelByName.entities();
		for(Channels chan: channel_cursor){
			allChannels.add(chan.getChannelName());
			String channelname = chan.getChannelName();
			Set<String> xpaths = chan.getXPath();
			System.out.print(channelname+":");
			for(String xpath: xpaths){
				System.out.print(xpath+"\t");
			}
			System.out.println(" ");
		}
		channel_cursor.close();
		myDbEnv.close();
		return allChannels;
	}
	public void updateXpath(String xpath, String url){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		SecondaryIndex<String, Long, RetrievedDocs> docByLink = da.docByLink;
		PrimaryIndex<String, XPath> xpathByExpression = da.XpathByExpression;
		RetrievedDocs doc = docByLink.get(url);
		XPath xpathEntity = xpathByExpression.get(xpath);
		long docKey = doc.getDocId();
		Set<Long> newDocs = xpathEntity.getDocuments();
		newDocs.add(Long.valueOf(docKey));
		xpathEntity.setDocuments(newDocs);
		xpathByExpression.put(xpathEntity);
		myDbEnv.close();
	}
	public void deletaXpath(String xpath, String url){
		System.out.println("In delete xpath method: "+ xpath+", "+url);
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		SecondaryIndex<String, Long, RetrievedDocs> docByLink = da.docByLink;
		PrimaryIndex<String, XPath> xpathByExpression = da.XpathByExpression;
		RetrievedDocs doc = docByLink.get(url);
		XPath xpathEntity = xpathByExpression.get(xpath);
		long docKey = doc.getDocId();
		Set<Long> docs = xpathEntity.getDocuments();
		if(docs.contains(Long.valueOf(docKey))){
			docs.remove(Long.valueOf(docKey));
		}
	}
	public void updateChannel(String channel, String xpath){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		//PrimaryIndex<String, XPath> xpathByExpression = da.XpathByExpression;
		PrimaryIndex<String, Channels> channelByName = da.channelByName;
		Channels chan = channelByName.get(channel);
		Set<String> newXpath = chan.getXPath();
		newXpath.add(xpath);
		chan.setXPath(newXpath);
		channelByName.put(chan);
	}
	public void saveXpath(String xpath) throws DatabaseException{
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		XPath x = new XPath();
		x.setXpathExpression(xpath);
		da.XpathByExpression.put(x);
		myDbEnv.close();
	}
	public void saveChannel(String channel, Set<String> xpath, String name) throws DatabaseException{
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		Channels chan = new Channels();
		chan.setChannelName(channel);
		chan.setXPath(xpath);
		chan.setCreator(name);
		da.channelByName.put(chan);
		myDbEnv.close();
	}
}
