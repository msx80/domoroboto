package com.github.msx80.domoroboto.model;

public class Thing {

	public String id;
	public String label;
	public String kind;
	public String statusTopic;
	public String commandTopic;
	public String replyTopic;
	public String commandTemplate;
	public Boolean commandJson;
	public String activeTopic;
	public String activeTopicPath;
	public String resultOk;
	public Attribute[] attributes;
	public Boolean autosend;
	
	public Boolean getAutosend() {
		return autosend;
	}
	
	public String getId() {
		return id;
	}
	public String getLabel() {
		return label;
	}
	public String getKind() {
		return kind;
	}
	public String getStatusTopic() {
		return statusTopic;
	}
	public String getCommandTopic() {
		return commandTopic;
	}
	public String getReplyTopic() {
		return replyTopic;
	}
	public String getCommandTemplate() {
		return commandTemplate;
	}
	public String getResultOk() {
		return resultOk;
	}
	public Attribute[] getAttributes() {
		return attributes;
	}
	
	
}
