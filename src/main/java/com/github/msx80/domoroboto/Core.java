package com.github.msx80.domoroboto;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msx80.domoroboto.model.Attribute;
import com.github.msx80.domoroboto.model.State;
import com.github.msx80.domoroboto.model.Thing;
import com.github.msx80.domoroboto.model.ThingData;
import com.github.msx80.domoroboto.model.Type;
import com.github.msx80.domoroboto.utils.JsonUtils;
import com.github.msx80.domoroboto.utils.MultiplexingSub;
import com.github.msx80.domoroboto.utils.StoredConf;
import com.github.msx80.domoroboto.utils.StoredConfImpl;
import com.github.msx80.domoroboto.utils.Templating;
import com.github.msx80.jouram.Jouram;
import com.github.msx80.jouram.core.fs.VFile;
import com.github.msx80.jouram.kryo.KryoSeder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class Core implements Domo {
	
	private static Logger log = LoggerFactory.getLogger(Core.class);
	
	private static final Set<String> ACTIVE_WORDS = Set.of("TRUE", "ON", "1", "Y");

	Map<String, Type> allTypes;
	
	Map<String, ThingData> thingsById = new HashMap<>();
	List<ThingData> things = new ArrayList<>();
	
	// store the "last sent" configuration for all things, so that the page can be prepared with the same values as the last time
	final StoredConf storedConf;

	private MultiplexingSub multi;
	
	public Core(MultiplexingSub mqtt) throws Exception 
	{
		this.multi = mqtt;
		storedConf = Jouram.open(VFile.fromPath( Path.of(".") ), "storedConf", StoredConf.class, new StoredConfImpl(),  new KryoSeder(), false);
		allTypes = new HashMap<>();
		for (Type t : Type.values()) 
		{
			allTypes.put(t.name().toLowerCase(), t);
		}
		
		String j = String.join("\n", Files.readAllLines(Path.of( "things.json" )));
		Gson g= new Gson();
		Config  c = g.fromJson(j, Config.class);
		
		for (Thing t : c.things) {
			ThingData td = new ThingData(t, State.UNKNOWN, "");
			thingsById.put(t.id, td);
			things.add(td);
		}
		
		for (ThingData t : thingsById.values()) 
		{
			multi.subscribe(t.thing.statusTopic,  (x, m) ->  mqttStatus(t, m));
			multi.subscribe(t.thing.replyTopic,  (x, m) ->  mqttReply(t, m));
			
			if(t.thing.activeTopic!=null)
			{
				multi.subscribe(t.thing.activeTopic,  (x, m) ->  mqttActive(t, m));
			}
			else
			{
				// if no active mechanism, assume non active
				t.active = false;
			}
		}
	}
	
	private void mqttActive(ThingData thing, String sn)
	{
		
		log.info("Received active: {} {}",thing.thing.id,  sn);
		if(thing.thing.activeTopicPath != null)
		{
			try
			{
				Object o = JsonPath.read(sn, thing.thing.activeTopicPath);
				sn = o.toString();
			}catch (PathNotFoundException e) {
				log.info("Active message without activeTopic {}", thing.thing.id);
				return;
			}
		}
		
		
		thing.active=isActive(sn);
		log.info("Active: {} -> {}",thing.thing.id,  thing.active);
	}		
	private Boolean isActive(String sn) {
		
		return ACTIVE_WORDS.contains(sn.toUpperCase());
	}

	private void mqttStatus(ThingData thing, String sn)
	{
		sn = sn.toUpperCase();
		log.info("Received status: {}");
		thing.state = State.valueOf(sn);
	}	
	private void mqttReply(ThingData thing, String s)
	{
		log.info("Received reply: {} {}", thing.thing.id, s);
		
		// fullfill the future if anyone is waiting.
		// we could receive unexpected reply, for example if the device is activated with a different system
		// in that case we won't have the future, so just ignore.
		var fx = thing.reply;
		if(fx!=null)
		{
			fx.complete(s);
		}
	}

	@Override
	public Map<String, Type> getAllTypes() {
		
		return allTypes;
	}

	public Collection<ThingData> things() 
	{
		return things;
	}

	public ThingData thing(String id) {
		
		return thingsById.get(id);
	}

	public void sendCommand(ThingData t, String jsonParams) 
	{
		Map<String, ?> params = parseCommand(t, jsonParams);
		
		
		// either generate the command automatically as json, or handle the template.
		String payload;
		if(t.thing.commandJson == Boolean.TRUE) // could be null
		{
			payload = JsonUtils.toJson(params);
		}
		else if (t.thing.commandTemplate != null)
		{
			String template = t.thing.commandTemplate;
			payload = Templating.direct(template).addAll(params).render();
		}
		else
		{
			throw new RuntimeException("No command mode");
		}
		
		log.info("COMMAND: "+payload);
		
		
		
		t.reply = new CompletableFuture<>(); // prepare the future for the Reply callback
		try {
			String b;
			try {
				// send the command
				multi.publish(t.thing.commandTopic, payload, 2, false);
				// wait 5 seconds to get the reply 
				b = t.reply.get(5, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				throw new RuntimeException("Timed out", e);
			} catch (MqttException e) {
				throw new RuntimeException("Error sending on MQTT", e);
			} catch (Exception e) {
				throw new RuntimeException("Generic Error", e);
			}
			// check if the reply was ok
			if(!checkReply(t, b))
			{
				throw new RuntimeException("Command reply was not OK: "+b);
			}
		}
		finally
		{
			t.reply = null;
		}

		// if all is good, store this parameters as the "last sent" 
		storedConf.store(t.thing.getId(), new HashMap<> (params));
		
	}

	private Map<String, ?> parseCommand(ThingData t, String jsonParams) 
	{
		// jsonParams has all fields as string (ie: "active":"true" and "temp":"23")
		// we need to convert all fields to their correct value
		
		Map<String, Object> res = new HashMap<>();
		
		JsonObject p = JsonParser.parseReader(new StringReader(jsonParams)).getAsJsonObject();
		
		for (Attribute a : t.thing.attributes) {
			Type tt = allTypes.get(a.type);
			var elem = p.get(a.id);
			if(elem == null)
			{
				if(tt == Type.Pulse)
				{
					// nothing to do here
				}
				else
				{
					// from the web command all attributes should be valorized
					throw new RuntimeException("Missing key: "+a.id);
					// res.put(a.id, a.defaultValue);
				}
			}
			else
			{
				var input = elem.getAsString();
				Object val = this.parseTypeValue(tt, input);
				res.put(a.id, val);
			}
			
		}
		
		return res;
	}

	private boolean checkReply(ThingData t, String b) {
		if(t.thing.resultOk != null)
		{
			return FilenameUtils.wildcardMatch(b, t.thing.resultOk);
		}
		return false;
	}

	@Override
	public StoredConf getStoredConf() {
		return storedConf;
	}

	@Override
	public Object getCorrectDefault(Attribute a) {
		Type t = allTypes.get(a.type);
		if(t == Type.Pulse) return null;
		Object val = a.defaultValue;
		if(t.valueClass != val.getClass())
		{
			if(t.valueClass == Integer.class && val.getClass() == Double.class)
			{
				// json read Numbers as Double if there's no context (target is Object)
				val = ((Double)val).intValue();
			}
			else
			{
				throw new RuntimeException("Unable to correct default value "+t.valueClass+" <- "+val.getClass());
			}
		}
		return val;
	}
	
	
}
