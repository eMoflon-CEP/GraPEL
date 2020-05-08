package org.emoflon.cep.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apama.event.parser.EventType;

public abstract class Event {
	protected Map<String, Object> fields = Collections.synchronizedMap(new LinkedHashMap<>());
	protected final EventType type;
	
	public Event(final EventType type) {
		this.type = type;
	}
	
	public Event(final com.apama.event.Event apamaEvent) {
		this.type = apamaEvent.getEventType();
		apamaEvent.getFieldsMap().forEach((name, obj) -> fields.put(name, obj));
	}
	
	public void addField(final String name, final Object value) {
		fields.put(name, value);
	}
	
	public Object getField(final String name) {
		return fields.get(name);
	}
	
	public com.apama.event.Event toApamaEvent() {
		com.apama.event.Event apamaEvent = new com.apama.event.Event(type);
		fields.forEach((name, obj) -> apamaEvent.setField(name, obj));
		return apamaEvent;
	}
	
}
