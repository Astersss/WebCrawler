package edu.upenn.cis455.storage;

import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;
import static com.sleepycat.persist.model.Relationship.*;
import static com.sleepycat.persist.model.DeleteAction.NULLIFY;

@Entity
public class XPath {
	@PrimaryKey
	private String expression;
	
	@SecondaryKey(relate=MANY_TO_MANY, relatedEntity=RetrievedDocs.class, onRelatedEntityDelete=NULLIFY)
	private Set<Long> documents = new HashSet<Long>();
	
	public void setXpathExpression(String data){
		expression = data;
	}
	public void setDocuments(Set<Long> data){
		documents = data;
	}
	public String getXpathExpression(){
		return expression;
	}
	public Set<Long> getDocuments(){
		return documents;
	}
}
