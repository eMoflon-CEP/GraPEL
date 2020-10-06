package org.emoflon.cep.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

import com.apama.EngineException;
import com.apama.engine.MonitorScript;
import com.apama.engine.beans.interfaces.ConsumerOperationsInterface;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.parser.EventParser;
import com.apama.event.parser.EventType;
import com.apama.event.IEventListener;

public abstract class EventHandler <E extends Event> implements IEventListener{
	
	protected final GrapeEngine engine;
	protected final TypeRegistry registry;
	protected final EngineClientInterface engineClient;
	protected ConsumerOperationsInterface eventConsumer;
	protected EventParser parser;
	
	protected EventType apamaEventType;
	protected Collection<E> events = Collections.synchronizedList(new LinkedList<>());
	protected Collection<E> lastEvents = Collections.synchronizedList(new LinkedList<>());
	protected Set<Consumer<E>> subscriber = new LinkedHashSet<>();
	
	public EventHandler(final GrapeEngine engine) {
		this.engine = engine;
		this.registry = engine.getTypeRegistry();
		this.engineClient = engine.getEngineClient();
	}
	
	protected void setEventParser(final EventParser parser) {
		this.parser = parser;
	}
	
	protected void init() throws EngineException {
		apamaEventType = getEventType();
		parser.registerEventType(apamaEventType);
		MonitorScript script = new MonitorScript(loadEPLDescription());
		engineClient.injectMonitorScript(script);
		eventConsumer = engineClient.addConsumer(getHandlerName(), getChannelNames());
		eventConsumer.addEventListener(this);
	}
	
	protected abstract E convertEvent(final com.apama.event.Event apamaEvent);
	
	protected abstract String loadEPLDescription();
	
	protected void clearRecentEvents() {
		lastEvents.clear();
	}
	
	public abstract String getHandlerName();
	
	public abstract String[] getChannelNames();
	
	public abstract EventType getEventType();
	
	
	public Collection<E> getNewEvents() {
		return lastEvents;
	}
	
	public Collection<E> getAllEvents() {
		return events;
	}
	
	public boolean subscribe(Consumer<E> consumer) {
		return subscriber.add(consumer);
	}
	
	public boolean unsubscribe(Consumer<E> consumer) {
		return subscriber.remove(consumer);
	}
	
	public void sendEvent(E event) {
		try {
			com.apama.event.Event apamaEvent = event.toApamaEvent(registry);
			handleEvent(apamaEvent);
			engineClient.sendEvents(apamaEvent);
		} catch (EngineException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleEvent(com.apama.event.Event arg0) {
		arg0.setEventParser(parser);
		if(!arg0.getEventType().getName().equals(apamaEventType.getName()))
			return;
		
		E event = convertEvent(arg0);
		events.add(event);
		lastEvents.add(event);
		subscriber.forEach(sub -> sub.accept(event));
	}

	@Override
	public void handleEvents(com.apama.event.Event[] arg0) {
		for(com.apama.event.Event event :  arg0) {
			handleEvent(event);
		}
	}

}
