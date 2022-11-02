package com.github.msx80.domoroboto.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.FileTemplateLoader;

/**
 * Quick flow based api for Handlebar templates
 *
 */
public class Templating {

	final Template page;
	final Map<String, Object> ctx = new HashMap<>();
	
	public static Templating load(String template)
	{
		return new Templating(template, true);
	}
	
	public static Templating direct(String template)
	{
		return new Templating(template);
	}
	
	private Templating(String template, boolean file) {
		Handlebars handlebars = new Handlebars(new FileTemplateLoader(new File("./templates")));
		handlebars.registerHelper("eq", ConditionalHelpers.eq);
		try {
			page = handlebars.compile(template);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Templating(String direct) {
		Handlebars handlebars = new Handlebars();
		handlebars.registerHelper("eq", ConditionalHelpers.eq);
		try {
			page = handlebars.compileInline(direct);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Templating add(String key, Object value)
	{
		ctx.put(key, value);
		return this;
	}
	
	
	public Templating addAll(Map<String, ?> values)
	{
		ctx.putAll(values);
		return this;
	}
	
	public String render()
	{
		try {
			return page.apply(ctx);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
