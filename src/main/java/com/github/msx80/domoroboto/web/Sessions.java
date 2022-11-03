package com.github.msx80.domoroboto.web;

import com.github.msx80.jouram.core.Mutator;

/**
 * Stores http sessions id for users
 *
 */
public interface Sessions {

	String getUser(String session);
	
	@Mutator
	void putUser(String user, String session);

	@Mutator
	void removeUser(String user);
	
	
	
}
