package com.github.msx80.domoroboto.model;

public enum Type 
{
	Pulse(Void.class),
	Switch(Boolean.class),
	Temperature(Integer.class),
	Color(String.class),
	Enumeration(String.class),
	Scale(Integer.class),
	Host(String.class),
	Port(String.class),
	String(String.class),
	Integer(Integer.class);

	public final Class<?> valueClass;

	Type(Class<?> cls) {
		this.valueClass = cls;
	}
	
}
