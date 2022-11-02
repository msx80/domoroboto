package com.github.msx80.domoroboto.utils;

import java.util.Map;

import com.github.msx80.jouram.core.Mutator;

public interface StoredConf {

	@Mutator
	public void store(String id, Map<String, Object> map);
	
	public Map<String, Object> load(String id);
	
}
