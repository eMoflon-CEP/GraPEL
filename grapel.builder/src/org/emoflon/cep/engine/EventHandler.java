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

/**
 * Handles events of the type E using the Apama event listener
 * 
 * @param <E> defining the event type
 */
public abstract class EventHandler <E extends Event> implements IEventListener{
	
	// Grape + Apama event interfaces
	protected final GrapeEngine engine;
	protected final TypeRegistry registry;
	protected final EngineClientInterface engineClient;
	protected ConsumerOperationsInterface eventConsumer;
	protected EventParser parser;
	
	/**
	 * Type of the events, that are handled by the handler
	 */
	protected EventType apamaEventType;
	/**
	 * All events received by the handler
	 */
	protected Collection<E> events = Collections.synchronizedList(new LinkedList<>());
	/**
	 * The last events received by the handler
	 */
	protected Collection<E> lastEvents = Collections.synchronizedList(new LinkedList<>());
	/**
	 * Consumers subscribing to the event handler at receiving events fitting the event type 
	 */
	protected Set<Consumer<E>> subscriber = new LinkedHashSet<>();
	
	public EventHandler(final GrapeEngine engine) {
		this.engine = engine;
		this.registry = engine.getTypeRegistry();
		this.engineClient = engine.getEngineClient();
	}
	
	/**
	 * @param parser to be used for event parsing by the handler
	 */
	protected void setEventParser(final EventParser parser) {
		this.parser = parser;
	}
	
	/**
	 * Initializes the the event handler
	 * @throws EngineException if Apama monitor script injection or adding the consumer fails
	 */
	protected void init() throws EngineException {
		apamaEventType = getEventType();
		parser.registerEventType(apamaEventType);
		MonitorScript script = new MonitorScript(loadEPLDescription());
		engineClient.injectMonitorScript(script);
		eventConsumer = engineClient.addConsumer(getHandlerName(), getChannelNames());
		eventConsumer.addEventListener(this);
	}
	
	/**
	 * Converts an Apama event to a Grape event
	 * @param apamaEvent to be converted
	 * @return Grape event of type E
	 */
	protected abstract E convertEvent(final com.apama.event.Event apamaEvent);
	
	/**
	 * @return the EPL description for the event handler
	 */
	protected abstract String loadEPLDescription();
	
	/**
	 * Clears the last events list
	 */
	protected void clearRecentEvents() {
		lastEvents.clear();
	}
	
	/**
	 * @return the name of the event handler
	 */
	public abstract String getHandlerName();
	
	/**
	 * @return the names of used Apama channels
	 */
	public abstract String[] getChannelNames();
	
	/**
	 * @return the event type for events handled by the event handler
	 */
	public abstract EventType getEventType();
	
	
	/**
	 * @return the last events received by the handler
	 */
	public Collection<E> getNewEvents() {
		return lastEvents;
	}
	
	/**
	 * @return all events received by the handler
	 */
	public Collection<E> getAllEvents() {
		return events;
	}
	
	/**
	 * Subscribes a consumer to event handler 
	 * @param consumer to be added to the subscribers of the event handler
	 * @return if the consumer was not already present as subscriber
	 */
	public boolean subscribe(Consumer<E> consumer) {
		return subscriber.add(consumer);
	}
	
	/**
	 * Removes a consumer from subscribers of the event handler
	 * @param consumer to be removed
	 * @return if the consumer was present as a subscriber
	 */
	public boolean unsubscribe(Consumer<E> consumer) {
		return subscriber.remove(consumer);
	}
	
	/**
	 * Sends an event to the Apama engine
	 * @param event to be send
	 */
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
