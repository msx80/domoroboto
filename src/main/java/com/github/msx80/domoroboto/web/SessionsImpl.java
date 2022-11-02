package com.github.msx80.domoroboto.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionsImpl implements Sessions {

	Map<String, String> sessions = new HashMap<>();
	
	@Override
	public String getUser(String session) {
		
		return sessions.get(session);
	}

	@Override
	public void putUser(String user, String session) 
	{

		// remove old sessions for this user
		removeUser(user);
		
		sessions.put(session, user);

	}

	@Override
	public  void removeUser(String user) {
		List<String> toRemove = sessions.entrySet().stream().filter(e -> e.getValue().equals(user)).map(e -> e.getKey()).toList();
		toRemove.forEach(sessions::remove);
	}

}
