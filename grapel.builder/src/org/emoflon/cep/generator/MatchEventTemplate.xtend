package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventTemplate

class MatchEventTemplate extends EventTemplate {
	
	new(ImportManager imports, NSManager names, PathManager paths, ModelManager model, String patternName) {
		super(imports, names, paths, model, patternName)
	}
	
	override String generate() {
		return '''package «imports.packageFQN»;
		
import org.emoflon.cep.engine.Event;
import org.emoflon.cep.engine.TypeRegistry;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.common.operational.SimpleMatch;
		
import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import «imports.getPatternFQN(eventName)»;
import «imports.getMatchFQN(eventName)»;
«FOR field : model.getFields(eventName)»
import «imports.getFieldFQN(eventName, field.name)»;
«ENDFOR»
		
public class «names.getMatchEventName(eventName)» extends EMoflonEvent<P1Match, P1Pattern>{
			
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
		type.addField("vanished", FieldTypes.BOOLEAN);
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
	
	@Override
	public void assignMatch() {
		IMatch iMatch = new SimpleMatch(pattern.getPatternName());
		«FOR field : model.getFields(eventName)»
		iMatch.put("«field.name»", fields.get("«field.name»"));
		«ENDFOR»
		P1Match match = new P1Match(pattern, iMatch);
		this.match = match;
	}	
}
'''		
	}
	
}