package com.github.msx80.domoroboto.utils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.msx80.simpleconf.Configuration;

public class MqttUtils 
{
	private static Logger log = LoggerFactory.getLogger(MqttUtils.class);

	public static MqttClient openMqtt(Configuration conf) throws Exception {
		

		String broker = conf.get("mqtt.broker.conn"); //"tcp://192.168.1.2:1883";
		String clientId = conf.get("mqtt.broker.clientId", "Domoroboto"); 
		MemoryPersistence persistence = new MemoryPersistence();

		MqttClient client = new MqttClient(broker, clientId, persistence);

		// MQTT connection option
		MqttConnectOptions connOpts = new MqttConnectOptions();
		if(conf.containsKey("mqtt.broker.user"))
		{
			connOpts.setUserName(conf.get("mqtt.broker.user"));
			connOpts.setPassword(conf.get("mqtt.broker.pass").toCharArray());
		}
	     // retain session
		connOpts.setAutomaticReconnect(true);
		connOpts.setCleanSession(true);
		
		// set callback
		client.setCallback(new MqttCallback() {
			
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				
				
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				
				
			}
			
			@Override
			public void connectionLost(Throwable cause) {
				log.error("Connection lost!", cause);
				
			}
		});
		// establish a connection
		log.info("Connecting to broker: {}", broker);
		client.connect(connOpts);
		return client;
	}

}
