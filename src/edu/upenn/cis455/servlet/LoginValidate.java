package edu.upenn.cis455.servlet;

import java.util.Map;

import edu.upenn.cis455.storage.DBWrapper;

public class LoginValidate {
	public static boolean isValidate(String envDirectory, String username, String password){
		DBWrapper dbw = new DBWrapper(envDirectory);
		Map<String, String> userInfo = dbw.readUserByName(username);
		if(userInfo == null) return false;
		else{
			String realPass = userInfo.get(username);
			if(realPass.equals(password)) return true;
		}
		return false;
	}
}
