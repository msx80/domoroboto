package com.github.msx80.domoroboto.sensors;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

import com.github.msx80.jouram.core.Mutator;

public interface ReadingDb {

	@Mutator
	public void add(String sensor, Number value, LocalDateTime timestamp);
	public Map<LocalDateTime, Number> getReading(String sensor);
	public Collection<String> sensors();
	public Number currentValue(String sensor);
}
