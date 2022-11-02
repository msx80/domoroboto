package com.github.msx80.domoroboto.utils;

import java.util.HashMap;
import java.util.Map;

public class StoredConfImpl implements StoredConf {

	Map<String, Map<String, Object>> store = new HashMap<>();
	
	@Override
	public void store(String id, Map<String, Object> map) {
		store.put(id, map);

	}

	@Override
	public Map<String, Object> load(String id) 
	{
		return store.getOrDefault(id, Map.of());
	}

}
