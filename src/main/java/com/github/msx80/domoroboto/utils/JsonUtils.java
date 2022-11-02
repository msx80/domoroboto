package com.github.msx80.domoroboto.utils;

import java.io.StringReader;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.msx80.simpleconf.Configuration;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtils {

	
	public static Configuration readConf(String o) 
	{
		JsonObject p = JsonParser.parseReader(new StringReader(o)).getAsJsonObject();
		return readConf(p);
		
	}

	public static Configuration readConf(JsonObject o) 
	{
		Map<String, String> map = o.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsString()));
		return Configuration.ofMap(map);
	}

	public static String toJson(Map<String, ?> x) {
		
		JsonObject o = new JsonObject();
		x.entrySet().forEach(e -> {
			Object val = e.getValue();
			if(val instanceof String)
				o.addProperty(e.getKey(), (String)val);
			else if (val instanceof Number)
				o.addProperty(e.getKey(), (Number)val);
			else if (val instanceof Boolean)
				o.addProperty(e.getKey(), (Boolean)val);
			else
				throw new RuntimeException("Invalid type "+val.getClass().getName());
			
		});
		
		return o.toString();
	}

}
