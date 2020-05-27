package org.emoflon.cep.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.ConsumerOperationsInterface;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.parser.EventType;
import com.apama.event.IEventListener;

public abstract class EventHandler <E extends Event> implements IEventListener{
	
	protected final GrapeEngine engine;
	protected final TypeRegistry registry;
	protected final EngineClientInterface engineClient;
	protected ConsumerOperationsInterface eventConsumer;
	
	protected EventType apamaEventType;
	protected Collection<E> events = Collections.synchronizedSet(new LinkedHashSet<>());
	
	public EventHandler(final GrapeEngine engine) {
		this.engine = engine;
		this.registry = engine.getTypeRegistry();
		this.engineClient = engine.getEngineClient();
	}
	
	public void init() throws EngineException {
		apamaEventType = getEventType();
		engineClient.injectMonitorScriptFromFile(loadEPLDescription());
		eventConsumer = engineClient.addConsumer(getHandlerName(), getChannelNames());
		eventConsumer.addEventListener(this);
	}
	
	public abstract String getHandlerName();
	
	public abstract String[] getChannelNames();
	
	public abstract String loadEPLDescription();
	
	public abstract EventType getEventType();
	
	public void sendEvent(E event) {
		try {
			engineClient.sendEvents(event.toApamaEvent(registry));
		} catch (EngineException e) {
			e.printStackTrace();
		}
	}
	
	public Collection<E> getEvents() {
		return events; 
	}
	
	public abstract E convertEvent(final com.apama.event.Event apamaEvent);

	@Override
	public void handleEvent(com.apama.event.Event arg0) {
		if(!arg0.getEventType().getName().equals(apamaEventType.getName()))
			return;
		
		events.add(convertEvent(arg0));
	}

	@Override
	public void handleEvents(com.apama.event.Event[] arg0) {
		for(com.apama.event.Event event :  arg0) {
			handleEvent(event);
		}
	}

}
