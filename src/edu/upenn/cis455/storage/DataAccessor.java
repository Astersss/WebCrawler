package edu.upenn.cis455.storage;

import java.util.Set;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class DataAccessor {
	PrimaryIndex<Long, Users> userById;
	SecondaryIndex<String, Long, Users> userByName;
	
	PrimaryIndex<Long, RetrievedDocs> docById;
	SecondaryIndex<String, Long, RetrievedDocs> docByLink;
	
	PrimaryIndex<String, Channels> channelByName;
	SecondaryIndex<String, String, Channels> channelByXpath;
	
	PrimaryIndex<String, XPath> XpathByExpression;
	SecondaryIndex<Long, String, XPath> XpathByDoc;
	public DataAccessor(EntityStore store){
		userById = store.getPrimaryIndex(Long.class, Users.class);
		userByName = store.getSecondaryIndex(userById, String.class, "username");
		docById = store.getPrimaryIndex(Long.class, RetrievedDocs.class);
		docByLink = store.getSecondaryIndex(docById, String.class, "url");
		channelByName = store.getPrimaryIndex(String.class, Channels.class);
		channelByXpath = store.getSecondaryIndex(channelByName, String.class, "XpathExpressions");
		XpathByExpression = store.getPrimaryIndex(String.class, XPath.class);
		XpathByDoc = store.getSecondaryIndex(XpathByExpression,Long.class,"documents");
		
	}
}
