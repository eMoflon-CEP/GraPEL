package org.emoflon.cep.generator

import org.emoflon.cep.generator.AbstractTemplate

/**
 * Template for GrapeL event generation
 */
class EventTemplate extends AbstractTemplate {
	
	/**
	 * Name of the event
	 */
	protected String eventName;
	/**
	 * Model manager utility for GrapeL model
	 */
	protected ModelManager model;
	
	/**
	 * Constructor for an event template
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 * @param model the manager that includes the utility for model access
	 * @param eventName the name of the event
	 */
	new(ImportManager imports, NSManager names, PathManager paths, ModelManager model, String eventName) {
		super(imports, names, paths)
		this.model = model;
		this.eventName = eventName;
	}
	
	override String generate() {
		return '''package «imports.packageFQN».events;
		
import org.emoflon.cep.engine.Event;
import org.emoflon.cep.engine.TypeRegistry;
		
import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

«FOR field : model.getComplexFields(eventName)»
import «imports.getFieldFQN(eventName, field.name)»;
«ENDFOR»
		
public class «names.getEventName(eventName)» extends Event{
			
	final public static String EVENT_NAME = "«eventName»";
	final public static EventType EVENT_TYPE = createEventType();
			
	public «names.getEventName(eventName)»(final com.apama.event.Event apamaEvent, final TypeRegistry registry) {
		super(apamaEvent, registry);
	}
	
	«FOR field : model.getNonVirtualFields(eventName)»
	public «ModelManager.getJavaFieldType(field)» get«StringUtil.firstToUpper(field.name)»() {
		return«IF !ModelManager.getJavaFieldType(field).equals("int")» («ModelManager.getJavaFieldType(field)») fields.get("«field.name»");
		«ELSE» ((Long)fields.get("«field.name»")).intValue();«ENDIF»
	}
	«ENDFOR»
		
	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
			
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		«FOR field : model.getFields(eventName)»
		type.addField("«field.name»", «ModelManager.getApamaFieldType(field)»);
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
			return «ModelManager.getJavaFieldType(field)».class;
		}
		«ENDFOR»
		return null;
	}
		
}
'''
	}
	
	override getPath() {
		return paths.getEventLocation(eventName)
	}
	
}