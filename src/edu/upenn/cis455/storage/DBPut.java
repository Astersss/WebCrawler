package edu.upenn.cis455.storage;

public class DBPut {
	private String envDirectory;
	private DataAccessor da;
	private static DBEnv myDbEnv = new DBEnv();
	
	public DBPut(String path){
		envDirectory = path;
	}
	public void run(){
		myDbEnv.setup(envDirectory);
		da = new DataAccessor(myDbEnv.getEntityStore());
		
		Users usr = new Users();
	}
}
