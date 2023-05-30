package com.github.msx80.domoroboto.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msx80.domoroboto.Core;

/**
 * A wrapper for MqttClient that enable multiple listener on the same subscription.
 * Subscription is done only once but multiple listeners are delivered the message
 *
 */
public class MultiplexingSub {
	
	private static Logger log = LoggerFactory.getLogger(MultiplexingSub.class);
	
	MqttClient mqtt;
	
	Map<String, List<Listener>> listeners = new HashMap<>();
	
	public interface Listener {
		void onMessage(String topic, String payload);
	}

	public MultiplexingSub(MqttClient mqtt) {
		this.mqtt = mqtt;
	}

	public void unsubscribe(String topic, Listener listener)
	{
		throw new UnsupportedOperationException("Left as exercise to the reader");
	}
	
	public void subscribe(String topic, Listener listener) throws MqttException
	{

		if(listeners.containsKey(topic))
		{
			listeners.get(topic).add(listener);
		}
		else
		{
			mqtt.subscribe(topic, this::messageReceived);
			var list = new ArrayList<Listener>();
			list.add(listener);
			listeners.put(topic, list);
		}
	}
	
	 private void messageReceived(String topic, MqttMessage message) throws Exception
	 {
		 String payload = new String(message.getPayload());
		 var lis = listeners.get(topic);
		 log.info("Received msg for {} listeners: {}", listeners.size(), payload);
		 for (Listener listener : lis) {
			 try
			 {
				 listener.onMessage(topic, payload);
			 }
			 catch (Exception e) {
				e.printStackTrace();
			}
		}
				 
	 }

	public void publish(String topic, String payload,int qos, boolean retained) throws MqttPersistenceException, MqttException {
		mqtt.publish(topic, payload.getBytes(), qos, retained);
		
	}
	
}
