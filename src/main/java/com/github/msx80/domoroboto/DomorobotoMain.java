package com.github.msx80.domoroboto;

import com.github.msx80.kitteh.DebugExceptionHandler;
import com.github.msx80.kitteh.DocumentProducer;
import com.github.msx80.kitteh.WebServer;
import com.github.msx80.kitteh.WebServerBuilder;
import com.github.msx80.simpleconf.Configuration;

public class DomorobotoMain {

	public static void main(String[] args) throws Exception 
	{
	
		Configuration conf = Configuration.load("domoroboto.conf");
		
		int httpPort = conf.getInt("http.port", 8080);
		
		DocumentProducer dp = new StandAloneProducer();
				
		WebServer ws = WebServerBuilder
				.produce( dp )
				.port(conf.getInt("http.port", httpPort))
				.exceptionHandler(new DebugExceptionHandler())
				.run();
		
		String base = conf.get("http.base", "");

		System.out.println("Server running, you can point your browser to (something like):");
		System.out.println(" http://localhost:"+httpPort+"/"+base+"things");
		System.out.println(" http://localhost:"+httpPort+"/"+base+"sensors");
		
		ws.waitTermination();
			
	}

}
