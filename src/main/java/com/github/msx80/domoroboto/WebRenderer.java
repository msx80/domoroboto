package com.github.msx80.domoroboto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msx80.domoroboto.model.Attribute;
import com.github.msx80.domoroboto.model.Thing;
import com.github.msx80.domoroboto.model.ThingData;
import com.github.msx80.domoroboto.model.Type;
import com.github.msx80.domoroboto.utils.Templating;

public class WebRenderer {

	private static Logger log = LoggerFactory.getLogger(WebRenderer.class);
	
	public static String renderAttribute(Domo domo, Attribute attr, Object value)
	{
		
		String title = Templating.load("attributeTitle").add("label", attr.label).render();
		
		Type t = domo.getType(attr.type);
		
		
		var vals = attr.possibleValues == null ? List.of() : Arrays.asList( attr.possibleValues );
		// if there are actual possible values but we're not one of them, overwrite with the first available
		if( (!vals.isEmpty()) && (!vals.contains(value))) value = vals.get(0).toString();

		log.info("rendering attribute: {} value: {}", attr.id, value);
		
		if(Files.exists(Path.of("./templates/attributes/"+t.name()+".hbs"))) {
			// we have a specific template for this Type
			title += Templating
					.load("attributes/"+t.name())
					.add("label", attr.label)
					.add("id", attr.id)
					.add("value", value)
					.add("enums", attr.possibleValues)
					.render();
		} else {
			// use a generic template for the class of this Type

			title += Templating
					.load("attributes/"+t.valueClass.getName())
					.add("label", attr.label)
					.add("id", attr.id)
					.add("value", value)
					.render();
		}
		
		return title;
		
	}
	
	

	public static String renderThing(Domo domo, ThingData td, Map<String, Object> values)
	{
		log.info("rendering thing: {}", td.thing.id);
		
		Thing ti = td.thing;
		String page = Templating
				.load("thing.start")
				.add("id", ti.id)
				.add("label", ti.label)
				.add("kind", ti.kind)
				.add("state", td.state)
				.add("autosend", ti.autosend)
				.add("stateDescription", "fakedescription")				
				.render();
		
		for (Attribute a : ti.attributes) {
			page+=WebRenderer.renderAttribute(domo, a, values.getOrDefault(a.id, domo.getCorrectDefault(a)));
		}
		
		page += Templating.load("thing.end")
				.add("id", ti.id)
				.add("autosend", ti.autosend)
				.render();
		
		return page;
		
	}
	
	
	
}
