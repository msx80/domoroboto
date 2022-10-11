# domoroboto
A web interface for MQTT-based domotic systems.

You provide a json based configuration file describing your device(s) and it generates a "pretty" interface, served with an internal webserver, and lets you send mqtt commands to control the device and captures responses.

It turns this:

```json
	{
		"id": "aria_studio",
		"label": "Aria Studio 💻",
		"kind": "Daikin A/C",
		"statusTopic": "tele/Plutone/LWT",
		"commandTopic": "cmnd/Plutone/DaikinCtrl",
		"replyTopic": "stat/Plutone/RESULT",
		"activeTopic": "tele/Plutone/SENSOR",
		"activeTopicPath": "$.active",

		"attributes": [
			{ "id": "active", "label": "Active", "desc": "bla", "type":"switch", "defaultValue":false },
			{ "id": "mode", "label":"Mode", "type":"enumeration", "defaultValue":"COOL", "possibleValues": ["FAN", "COOL", "HEAT", "DRY", "AUTO"] },
			{ "id": "fan", "label":"Fan", "type":"enumeration", "defaultValue":"1", "possibleValues": ["1", "2", "3", "4", "5", "NIGHT", "AUTO"] },
			{ "id": "temperature", "label":"Temperature", "type":"temperature", "defaultValue":21, "minValue": 18, "maxValue":30  },
			{ "id": "swingV", "label": "Swing Vert.", "type":"switch", "defaultValue": false },
			{ "id": "swingH", "label": "Swing Horiz.", "type":"switch", "defaultValue": false }
		],
		"commandJson": true,
		"commandTemplate2": "{ 'active':{{active}}, 'mode':'{{mode}}', 'fan':'{{fan}}', 'temperature':{{temperature}}, 'swingH':false, 'swingV':false }",
		"resultOk": "*\"DaikinCtrl\":\"Done\"*"
	}
	
```
Into this:

![image](https://user-images.githubusercontent.com/2278103/195146528-cbbdd9e2-0c67-41e9-91e0-6414a3fd4133.png)

which can send messages like this:

```json
{"mode":"DRY","swingH":false,"fan":"3","swingV":false,"temperature":21,"active":false}
```

Also supports some rudimentary sensor reading and logging.
