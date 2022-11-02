package com.github.msx80.domoroboto;

import java.util.Map;
import java.util.Objects;

import com.github.msx80.domoroboto.model.Attribute;
import com.github.msx80.domoroboto.model.Type;
import com.github.msx80.domoroboto.utils.StoredConf;

public interface Domo {
	Map<String, Type> getAllTypes();

	StoredConf getStoredConf();
	
	default Type getType(String type)
	{
		return Objects.requireNonNull(getAllTypes().get(type.toLowerCase()), "Unsupported type: "+type);
	}
	
	@SuppressWarnings("unchecked")
	default <T> T parseTypeValue(Type type, String value)
	{
		Class<?> c = type.valueClass;
		if(c == String.class)
		{
			return (T) value;
		} 
		else if (c == Integer.class)
		{
			return (T)(Integer)Integer.parseInt(value);
		}
		else if (c == Boolean.class)
		{
			return (T)(Boolean)Boolean.parseBoolean(value);
		}
		else if (c == Float.class)
		{
			return (T)(Float)Float.parseFloat(value);
		}
		else if (c == Double.class)
		{
			return (T)(Double)Double.parseDouble(value);
		}
		else
		{
			throw new RuntimeException("Unknown type");
		}
	}

	Object getCorrectDefault(Attribute a);
	
}
