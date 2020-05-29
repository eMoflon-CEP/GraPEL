package org.emoflon.cep.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apama.event.parser.EventType;

public abstract class Event {
	protected Map<String, Object> fields = Collections.synchronizedMap(new LinkedHashMap<>());
	protected final EventType type;
	
	public Event() {
		this.type = getStaticEventType();
	}
	
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
	
	public abstract boolean isComplexType(final String fieldName);
	
	public abstract Class<?> getClassOfField(final String fieldName);
	
	public abstract EventType getStaticEventType();
	
	public Object getField(final String name) {
		return fields.get(name);
	}
	
	protected void addField(final String name, final Object value) {
		fields.put(name, value);
	}
	
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
