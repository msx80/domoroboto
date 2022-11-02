package com.github.msx80.domoroboto.utils;

import java.security.SecureRandom;

public class TokenGenerator {

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final String chrs = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public static synchronized String token()
	{
	    String customTag = secureRandom
	    		.ints(32, 0, chrs.length())
	    		.mapToObj(i -> chrs.charAt(i))
	    		.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
	    		.toString();
	    		
	    return customTag;
	}
	
}
