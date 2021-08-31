package org.emoflon.cep.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apama.event.parser.EventType;

/**
 * GrapeL engine representation of events
 */
public abstract class Event {
	/**
	 * Map containing the event fields w/ event field name and object
	 */
	protected Map<String, Object> fields = Collections.synchronizedMap(new LinkedHashMap<>());
	/**
	 * Representing the event type
	 */
	protected final EventType type;
	
	/**
	 * Constructor for a static type event
	 */
	public Event() {
		this.type = getStaticEventType();
	}
	
	/**
	 * @param apamaEvent to be represented
	 * @param registry containing the ID/object mapping per type
	 */
	public Event(final com.apama.event.Event apamaEvent, final TypeRegistry registry) {
		this.type = apamaEvent.getEventType();
		apamaEvent.getFieldsMap().forEach((name, obj) -> {
			if(isComplexType(name)) {
				Object complexObj = registry.getObject(getClassOfField(name), (Long)obj);
				fields.put(name, complexObj);
			} else {
				fields.put(name, obj);
			}
		});
	}
	
	/**
	 * @param fieldName of the field, which should be checked
	 * @return if the field w/ the given name has a complex type
	 */
	public abstract boolean isComplexType(final String fieldName);
	
	/**
	 * @param fieldName of the field
	 * @return the class corresponding to the field name
	 */
	public abstract Class<?> getClassOfField(final String fieldName);
	
	/**
	 * @return the event type for a static event
	 */
	public abstract EventType getStaticEventType();
	
	/**
	 * @param name of the field
	 * @return the object, which the field includs
	 */
	public Object getField(final String name) {
		return fields.get(name);
	}
	
	/**
	 * Adds a field w/ name and value to the fields map
	 * @param name of the field
	 * @param value for the field
	 */
	protected void addField(final String name, final Object value) {
		fields.put(name, value);
	}
	
	/**
	 * Get an Apama event for the given fields
	 * @param registry which includes the mapping for complex type events
	 * @return an Apama event for the fields included
	 */
	protected com.apama.event.Event toApamaEvent(final TypeRegistry registry) {
		com.apama.event.Event apamaEvent = new com.apama.event.Event(type);
		fields.forEach((name, obj) -> {
			if(isComplexType(name)) {
				long id = registry.addObject(getClassOfField(name), obj);
				apamaEvent.setField(name, id);
			} else {
				apamaEvent.setField(name, obj);
			}
		});
		return apamaEvent;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString() + "{\n");
		fields.forEach((name, value) -> {
			sb.append("\t-"+name+":"+value+"\n");
		});
		sb.append("}");
		return sb.toString();
	}
	
}
