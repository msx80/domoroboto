package com.github.msx80.domoroboto.sensors;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReadingDbImpl implements ReadingDb {

	Map<String, Map<LocalDateTime, Number>> readings = new HashMap<>();
	Map<String, Number> current = new HashMap<>();
	
	@Override
	public void add(String sensor, Number value, LocalDateTime timestamp) {
		Map<LocalDateTime, Number> m = readings.computeIfAbsent(sensor, s -> new HashMap<>());
		m.put(timestamp, value);
		
		current.put(sensor, value);
	}

	@Override
	public Map<LocalDateTime, Number> getReading(String sensor) 
	{
		return readings.getOrDefault(sensor, Map.of());
	}

	@Override
	public Collection<String> sensors() {
		return Collections.unmodifiableCollection(readings.keySet());
	}

	@Override
	public Number currentValue(String sensor) {
		
		return current.get(sensor);
	}

}
