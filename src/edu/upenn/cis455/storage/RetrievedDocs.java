package edu.upenn.cis455.storage;

import static com.sleepycat.persist.model.Relationship.ONE_TO_ONE;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class RetrievedDocs {
	@PrimaryKey(sequence="doc_id")
	private long docId;
	
	@SecondaryKey(relate=ONE_TO_ONE)
	private String url;
	
	private String content;
	
	private long lastCrawled;
	
	public void setUrl(String data){
		url = data;
	}
	
	public void setContent(String data){
		content = data;
	}
	
	public void setLastCrawled(long data){
		lastCrawled = data;
	}
	
	public long getDocId(){
		return docId;
	}
	public String getUrl(){
		return url;
	}
	
	public String getContent(){
		return content;
	}

	public long getLastCrawled(){
		return lastCrawled;
	}
}
