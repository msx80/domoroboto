package com.github.msx80.domoroboto.sensors;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msx80.domoroboto.utils.MultiplexingSub;
import com.github.msx80.domoroboto.utils.Templating;
import com.github.msx80.jouram.Jouram;
import com.github.msx80.jouram.core.fs.VFile;
import com.github.msx80.jouram.kryo.KryoSeder;
import com.github.msx80.kitteh.DocumentProducer;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

public class SensorSystem 
{
	private static Logger log = LoggerFactory.getLogger(SensorSystem.class);
	
	Map<String, List<Sensor>> sensorsByTopic = new HashMap<>();
	Map<String, Sensor> sensorsById = new HashMap<>();
	ReadingDb db;
	
	public SensorSystem(MultiplexingSub m) throws Exception {
		this.init(m);
	}

	private void init(MultiplexingSub client) throws Exception
	{
		db = Jouram.open(VFile.fromPath( Path.of(".") ), "readingsDb", ReadingDb.class, new ReadingDbImpl(),  new KryoSeder(), false);
		Gson g= new Gson();
		
		Sensors sens;
		try(Reader r = new FileReader("sensors.json"))
		{
			sens = g.fromJson(r, Sensors.class);
		}
		
		for (Sensor s : sens.sensors) {
			List<Sensor> list = sensorsByTopic.computeIfAbsent(s.topic, t -> new ArrayList<>());
			list.add(s);
			
			sensorsById.put(s.id, s);
		}
		
		for(String topic : sensorsByTopic.keySet())
		{
			client.subscribe(topic, this::messageArrived);
		}
	
	}
	
	private void messageArrived(String topic, String message) 
	{
		try {
			log.info("Message received fot topic {} -> {}", topic, message);
			
			var list = sensorsByTopic.get(topic);
			Objects.requireNonNull(list, "No listener for this topic! "+topic);
			
			String text = message;
			for (Sensor sensor : list) {
				readSensor(sensor, text);
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}

	private void readSensor(Sensor sensor, String text) 
	{
		Number value = JsonPath.read(text, sensor.valuePath);
		if(value == null)
		{
			log.info("Message contains no value for {}", sensor.id);
			return;
		}
		
		LocalDateTime localDateTime;
		if(sensor.timestampPath != null)
		{
			String timestamp = JsonPath.read(text, sensor.timestampPath);
			var ta = DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(timestamp);
			localDateTime = LocalDateTime.from(ta);
		}
		else
		{
			// if timestampPath is not provided use sysdate
			localDateTime = LocalDateTime.now();
		}
		
		log.info("New data point: {} {} {}", sensor.id, localDateTime, value);
		
		db.add(sensor.id, value, localDateTime);
		
	}
	
	private void graph(Sensor sensor, OutputStream os) throws IOException
	{

		JFreeChart chart = ChartFactory.createTimeSeriesChart(sensor.label, // Chart
				"Instant", // X-Axis Label
				sensor.ylabel == null ? "Value" : sensor.ylabel, // Y-Axis Label
				timeSerie(sensor.label, db.getReading(sensor.id)));

		// Changes background color
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(new Color(255, 255, 220));

		((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesShapesVisible(0, true);

		ChartUtils.writeChartAsPNG(os, chart, 640, 480);

	}
	
	private TimeSeriesCollection timeSerie(String name, Map<LocalDateTime, Number> values)
	{
		TimeSeriesCollection dataset = new TimeSeriesCollection();  
			  
		TimeSeries series1 = new TimeSeries(name);
		
		LocalDateTime n = LocalDateTime.now().minus( 24 , ChronoUnit.HOURS);
		
		for (Entry<LocalDateTime, Number> e : values.entrySet()) {
			
			if(!e.getKey().isBefore(n))
			{
				Date d = java.sql.Timestamp.valueOf(e.getKey());
				series1.add(new Second(d), e.getValue());
			}
		}
			     
		dataset.addSeries(series1);
			  
		return dataset;  
  
	}

	public Map<String, Object> createDocumentProducer(String base) {
		
		// create the document producers (pages) for sensors.
		// theres a "list" one listing all sensors and a "graph"
		// producing the graph image.
		
		DocumentProducer list =  (req, res) -> 
		{
			
			var lista = sensorsById.values().stream().map(s -> {
				Map<String, Object> r = new HashMap<>();
				r.put("label", s.label);
				r.put("id", s.id);
				r.put("value", db.currentValue(s.id));
				return r;
			}).toList();
			
			String page = Templating
					.load("sensors.list")
					.add("sensors", lista)				
					.render();
			
			res.setContent(page);
		};
		
		
		DocumentProducer graph =  (req, res) -> 
		{
			String id = req.getPageName();
			
			Sensor s = sensorsById.get(id);
			
			if(s==null)
			{
				DocumentProducer.ERR_404_PRODUCER.produceDocument(req, res);
				return;
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			graph(s, baos);
			byte[] buf = baos.toByteArray();
			res.setContentType("image/png");
			res.setContent(buf);
		};
		
	
		Map<String, Object> rules = Map.of(
				
				base+"graph/(.*)", graph ,
				base+"sensors",  list 
				);
		
		return rules;
	}
	
}
