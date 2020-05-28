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
				Object complexObj = registry.getObject(getClassOfField(name), (Integer)obj);
				fields.put(name, complexObj);
			} else {
				fields.put(name, obj);
			}
		});
	}
	
	public void addField(final String name, final Object value) {
		fields.put(name, value);
	}
	
	public Object getField(final String name) {
		return fields.get(name);
	}
	
	public com.apama.event.Event toApamaEvent(final TypeRegistry registry) {
		com.apama.event.Event apamaEvent = new com.apama.event.Event(type);
		fields.forEach((name, obj) -> {
			if(isComplexType(obj)) {
				int id = registry.addObject(obj);
				apamaEvent.setField(name, id);
			} else {
				apamaEvent.setField(name, obj);
			}
		});
		return apamaEvent;
	}
	
	public abstract EventType getStaticEventType();
	
	public static boolean isComplexType(final Object object ) {
		if(object instanceof Number) {
			return false;
		} else if(object instanceof String) {
			return false;
		} else if(object instanceof Boolean) {
			return false;
		} else {
			return true;
		}
	}
	
	public abstract boolean isComplexType(final String fieldName);
	
	public abstract Class<?> getClassOfField(final String fieldName);
	
}
