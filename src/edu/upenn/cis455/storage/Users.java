package edu.upenn.cis455.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;
import static com.sleepycat.persist.model.Relationship.*;

@Entity
public class Users {
	
	@PrimaryKey(sequence="user_id")
	private long userId;
	
	@SecondaryKey(relate=ONE_TO_ONE)
	private String username;
	
	private String password;
	
	public void setUsername(String data){
		username = data;
	}
	
	public void setPassword(String data){
		password = data;
	}
	
	public long getUserId(){
		return userId;
	}
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}

}
