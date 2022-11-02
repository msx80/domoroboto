package com.github.msx80.domoroboto.model;

import java.util.concurrent.CompletableFuture;

public class ThingData 
{
	public Thing thing;
	public State state;
	public String stateDescription;
	public Boolean active;
	public CompletableFuture<String> reply;
	
	public ThingData(Thing thing, State state, String stateDescription) {
		super();
		this.thing = thing;
		this.state = state;
		this.stateDescription = stateDescription;
	}

	public Thing getThing() {
		return thing;
	}

	public State getState() {
		return state;
	}

	public String getStateDescription() {
		return stateDescription;
	}

	public Boolean getActive() {
		return active;
	}
	
	public boolean getUnknowActive()
	{
		return active==null;
	}

	public CompletableFuture<String> getReply() {
		return reply;
	}


}
