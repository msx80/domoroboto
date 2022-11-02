package com.github.msx80.domoroboto.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msx80.kitteh.DocumentProducer;
import com.github.msx80.kitteh.Mime;
import com.github.msx80.kitteh.Redirection;
import com.github.msx80.kitteh.Request;
import com.github.msx80.kitteh.Response;


public class FileProducer implements DocumentProducer
{
	private static Logger log = LoggerFactory.getLogger(FileProducer.class);
	
	private File webContentDir;
	private DocumentProducer fallback;
	private String defaultFile = null;
	private String base;
	
	public FileProducer(String base, String webContentDir, DocumentProducer fallback) throws Exception
	{
		this.webContentDir = new File(webContentDir).getCanonicalFile();
		this.fallback = fallback;
		this.base = base;
	}
	
	public FileProducer(String base, String webContentDir) throws Exception
	{
		this(base, webContentDir,  DocumentProducer.ERR_404_PRODUCER);
	}
	
	public void produceDocument(Request request, Response response) throws Exception, Redirection
	{
		String doc = request.getDocumentName();
		if(!doc.startsWith(base))throw new Exception("Access denied");
		
		doc=doc.substring(base.length(), doc.length());
		
		if (doc.equals("") && (defaultFile != null))
		{
			doc = defaultFile; 
		}
		File filex = new File(webContentDir+File.separator+doc);
		// expand eventual shortcuts or such
		File file = filex.getCanonicalFile();
		log.info("Searching file: {}", file);
		if (!file.toString().startsWith(webContentDir.toString()))
		{
			throw new Exception("Access denied");
		}
		if (!file.exists())
		{
			
			fallback.produceDocument(request, response);
			
		}
		else
		{
			sendFile(response, file);
		}
	}
	private void sendFile(Response response, File file) throws FileNotFoundException, IOException
	{
		response.setCacheable(true)
				.setContent( new FileInputStream(file), (int)file.length() )
				.setContentType( Mime.getMIMEType(file) );
	}
	public String getDefaultFile() {
		return defaultFile;
	}
	public void setDefaultFile(String defaultFile) {
		this.defaultFile = defaultFile;
	}

}
