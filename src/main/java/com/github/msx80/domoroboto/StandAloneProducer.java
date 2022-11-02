package com.github.msx80.domoroboto;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.github.msx80.domoroboto.sensors.SensorSystem;
import com.github.msx80.domoroboto.utils.MqttUtils;
import com.github.msx80.domoroboto.utils.MultiplexingSub;
import com.github.msx80.domoroboto.utils.Templating;
import com.github.msx80.domoroboto.utils.TokenGenerator;
import com.github.msx80.domoroboto.web.FileProducer;
import com.github.msx80.domoroboto.web.Sessions;
import com.github.msx80.domoroboto.web.SessionsImpl;
import com.github.msx80.jouram.Jouram;
import com.github.msx80.jouram.core.fs.VFile;
import com.github.msx80.jouram.kryo.KryoSeder;
import com.github.msx80.kitteh.DocumentProducer;
import com.github.msx80.kitteh.Redirection;
import com.github.msx80.kitteh.Request;
import com.github.msx80.kitteh.Response;
import com.github.msx80.kitteh.producers.DispatcherProducer;
import com.github.msx80.simpleconf.Configuration;


public class StandAloneProducer implements DocumentProducer {


	private static Sessions sessions = Jouram.open(VFile.fromPath( Path.of(".") ), "sessions", Sessions.class, new SessionsImpl(),  new KryoSeder(), false);
	
	private DocumentProducer dp;

	public StandAloneProducer() throws Exception 
	{
		Configuration conf = Configuration.load("domoroboto.conf");
		var mqtt = MqttUtils.openMqtt(conf);
		MultiplexingSub m = new MultiplexingSub(mqtt);
		Core c = new Core(m);
		
		String base = conf.get("http.base", "");
		
		// obtain the rules of the Controller and Sensors subsystems 
		
		Map<String, Object> rules1 = DomorobotoDocumentProducer.createDocumentProducer(c, conf,  base);
		Map<String, Object> rules2 = new SensorSystem(m).createDocumentProducer( base);
		
		
		
		Map<String, Object> rules = new HashMap<>();
		rules.putAll(rules1);
		rules.putAll(rules2);
		
		// create a documentproducer that is protected by login
		DocumentProducer protectedP = checkLogin(base,  new DispatcherProducer(rules) );
		
		// requests will first pass throu "unprotected" pages (login, dologin and files), if nothing is found it fallback to protected domain (sensors and stuff)
		this.dp = unprotected(base, conf, protectedP);
	}
	
	private DocumentProducer unprotected(String base, Configuration conf, DocumentProducer fallback) throws Exception {
		DocumentProducer login =  (req, res) -> 
		{
			
			String page = Templating
					.load("login")
					.render();
			
			res.setContent(page);
		};

		DocumentProducer doLogin =  (req, res) -> 
		{
			String user = req.getParameter("user");
			String pass = req.getParameter("pass");
						
			String pp = conf.get("auth.user."+user, "no"+pass);
			
			if(pass.equals(pp))
			{
			
				String sid = TokenGenerator.token();
				sessions.putUser(user, sid);
				res.getHeaders().set("Set-Cookie", "lsid="+sid+"; Max-Age=64000000; Secure");
				res.getHeaders().set("Location", "things");
				res.setContent("Document moved");
			    res.setHtmlReturnCode(302);
			}
			else
			{
				res.setHtmlReturnCode(401);
				res.setContent("Unauthorized");
			}
		};
		
		
		Map<String, Object> rules = Map.of(
				
				base+"dologin", doLogin,
				base+"login", login
				);
		
		FileProducer fileProducer = new FileProducer(base, "static", fallback);
		
		return new DispatcherProducer(rules, fileProducer);
	}

	@Override
	public void produceDocument(Request request, Response response) throws Exception, Redirection {
		dp.produceDocument(request, response);
	}
	
	public static DocumentProducer checkLogin(String base, DocumentProducer thingPage) {
		
		return (request, response) -> {
				String cookie = request.getHeaders().get("cookie");
								
				if(cookie != null)
				{
					String c = readCookie(cookie);
					if(c!=null)
					{
						String user = sessions.getUser(c);
						if(user != null)
						{
							request.setLocalAttr("user", user);
							thingPage.produceDocument(request, response);
							return;
						}
					}
				}
				throw new Redirection("/"+base+"login");
//				response.setHtmlReturnCode(401);
//				response.setContent("Unauthorized");
			
		};
		
		
	}	
	
	protected static String readCookie(String cookie) {
		String[] cookies = cookie.split(";");
		
		for (String c : cookies) {
			String toks[] = c.trim().split("=");
			if("lsid".equals( toks[0] ))
			{
				return toks[1];
			}
		}
		
		return null;

	}



}
