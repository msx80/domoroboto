package com.github.msx80.domoroboto;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msx80.domoroboto.model.ThingData;
import com.github.msx80.domoroboto.utils.Templating;
import com.github.msx80.kitteh.DocumentProducer;
import com.github.msx80.simpleconf.Configuration;


public class DomorobotoDocumentProducer {

	
	private static Logger log = LoggerFactory.getLogger(DomorobotoDocumentProducer.class);

	public static Map<String, Object> createDocumentProducer(Core core, Configuration conf, String base) throws Exception {
		
		
		// this renders the list of things
		DocumentProducer list =  (req, res) -> 
		{
			
			String page = Templating
					.load("thing.list")
					.add("things", core.things())				
					.render();
			
			res.setContent(page);
		};
		
		
		// this renders a single page thing
		DocumentProducer thingPage =  (req, res) -> 
		{
			String id = req.getPageName();
			
			ThingData t = core.thing(id);
			if(t==null)
			{
				DocumentProducer.ERR_404_PRODUCER.produceDocument(req, res);
				return;
			}
			
			String html = WebRenderer.renderThing(core, t, core.getStoredConf().load(t.thing.id));
			res.setContent(html);
		};
		
		// this handles incoming command
		DocumentProducer action =  (req, res) -> 
		{
			ThingData t = core.thing(req.getPageName());
			if(t==null)
			{
				DocumentProducer.ERR_404_PRODUCER.produceDocument(req, res);
				return;
			}
			String body = req.getBody();
			log.info("Action by {} on thing {}. Body: {}", req.getLocalAttr("user"), t.getThing().getId(), body);
			
						
			try {
				sendCommand(core, t, body);
				log.info("Command OK");
				res.setContent("OK");
			} catch (Exception e) {
				log.error("Error executing command: "+e.getMessage(), e);
				res.setContent("ERROR: "+e.getMessage());
			}
		};
		

		
		
		Map<String, Object> rules = Map.of(
				
				base+"thing/(.*)", thingPage,
				base+"action/(.*)", action,
				base+"things", list
				);
		
		return rules;
	}

	

	private static void sendCommand(Core core, ThingData t, String jsonParams) {
		
		core.sendCommand(t, jsonParams);
	}

	
}
