package org.emoflon.cep.engine;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.parser.EventType;

public abstract class EventHandler {
	
	protected final GrapeEngine engine;
	protected final TypeRegistry registry;
	protected final EngineClientInterface engineClient;
	protected EventType apamaEventType;
	
	public EventHandler(final GrapeEngine engine) {
		this.engine = engine;
		this.registry = engine.getTypeRegistry();
		this.engineClient = engine.getEngineClient();
	}
	
	public void init() throws EngineException {
		apamaEventType = createEventType();
		engineClient.injectMonitorScriptFromFile(loadEPLDescription());
	}
	
	public abstract String loadEPLDescription();
	
	public abstract EventType createEventType();
	
	public void sendEvent(Event event) throws EngineException {
		engineClient.sendEvents(event.toApamaEvent());
	}

}
