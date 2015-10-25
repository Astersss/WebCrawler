package edu.upenn.cis455.storage;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;
import static com.sleepycat.persist.model.Relationship.*;
import static com.sleepycat.persist.model.DeleteAction.NULLIFY;

@Entity
public class Channels {
	@PrimaryKey
	private String channelName;
	private String creator;
	
	@SecondaryKey(relate=MANY_TO_MANY, relatedEntity=XPath.class, onRelatedEntityDelete=NULLIFY)
	private Set<String> XpathExpressions = new HashSet<String>();

	public void setChannelName(String data){
		channelName = data;
	}
	public void setCreator(String data){
		creator = data;
	}
	public void setXPath(Set<String> data){
		XpathExpressions = data;
	}
	
	public String getChannelName(){
		return channelName;
	}
	public String getCreator(){
		return creator;
	}
	public Set<String> getXPath(){
		return XpathExpressions;
	}
}
