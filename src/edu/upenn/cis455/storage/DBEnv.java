package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DBEnv {
	private Environment myEnv;
	private EntityStore store;
	
	public DBEnv(){};
	
	public void setup(String envDirectory){
		//System.out.println("environment setting up...");
		try{
			EnvironmentConfig envConfig = new EnvironmentConfig();
			StoreConfig storeConfig = new StoreConfig();
			
			envConfig.setAllowCreate(true);
			storeConfig.setAllowCreate(true);
			envConfig.setTransactional(true);
			storeConfig.setTransactional(true);
			//System.out.println("Environment path: "+ envDirectory);
			myEnv = new Environment(new File(envDirectory), envConfig);
			store = new EntityStore(myEnv, "EntityStore", storeConfig);
			
			SequenceConfig userSequenceConfig = store.getSequenceConfig("user_id");
			SequenceConfig docSequenceConfig = store.getSequenceConfig("doc_id");
			SequenceConfig channelSequenceConfig = store.getSequenceConfig("channel_id");
			userSequenceConfig.setCacheSize(1);
			docSequenceConfig.setCacheSize(1);
			channelSequenceConfig.setCacheSize(1);
			store.setSequenceConfig("user_id", userSequenceConfig);
			store.setSequenceConfig("doc_id", docSequenceConfig);
			store.setSequenceConfig("channel_id", channelSequenceConfig);			
		} catch (DatabaseException e){
			e.printStackTrace();
		}
	}
	public EntityStore getEntityStore(){
		return store;
	}
	public Environment getEnv(){
		return myEnv;
	}
	public void close(){
		if(store != null){
			try{
				store.close();
			}catch(DatabaseException e){
				e.printStackTrace();
			}
		}
		if(myEnv != null){
			try{
				myEnv.close();
			}catch(DatabaseException e){
				e.printStackTrace();
			}
		}
	}
}
