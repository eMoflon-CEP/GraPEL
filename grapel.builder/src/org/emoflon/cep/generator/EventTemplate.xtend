package org.emoflon.cep.generator

import org.emoflon.cep.generator.AbstractTemplate

class EventTemplate extends AbstractTemplate {
	
	protected String eventName;
	protected ModelManager model;
	
	new(ImportManager imports, NSManager names, PathManager paths, ModelManager model, String eventName) {
		super(imports, names, paths)
		this.model = model;
		this.eventName = eventName;
	}
	
	override String generate() {
		return '''package «imports.packageFQN»;
		
import org.emoflon.cep.engine.Event;
import org.emoflon.cep.engine.TypeRegistry;
		
import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

«FOR field : model.getFields(eventName)»
import «imports.getFieldFQN(eventName, field.name)»;
«ENDFOR»
		
public class «names.getEventName(eventName)» extends Event{
			
	final public static String EVENT_NAME = "«eventName»";
	final public static EventType EVENT_TYPE = createEventType();
			
	public «names.getEventName(eventName)»(final com.apama.event.Event apamaEvent, final TypeRegistry registry) {
		super(apamaEvent, registry);
	}
	
«FOR field : model.getFields(eventName)»
	public «field.EType.name» get«field.name»() {
		return («field.EType.name») fields.get("«field.name»");
	}
«ENDFOR»
		
	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
			
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		«FOR field : model.getFields(eventName)»
		type.addField("«field.name»", FieldTypes.«model.getApamaFieldType(eventName, field)»
		«ENDFOR»
		return type;
	}
		
	@Override
	public boolean isComplexType(String fieldName) {
		«FOR field : model.getComplexFields(eventName)»
		if("«field.name»".equals(fieldName)) {
			return true;
		}
		«ENDFOR»
		return false;
	}
		
	@Override
	public Class<?> getClassOfField(String fieldName) {
		«FOR field : model.getFields(eventName)»
		if("«field.name»".equals(fieldName)) {
			return «field.EType.name».class;
		}
		«ENDFOR»
		return null;
	}
		
}
'''
	}
	
}