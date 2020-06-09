package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventTemplate

class MatchEventTemplate extends EventTemplate {
	
	new(ImportManager imports, NSManager names, PathManager paths, ModelManager model, String patternName) {
		super(imports, names, paths, model, patternName)
	}
	
	override String generate() {
		return '''package «imports.packageFQN».events;
		
import org.emoflon.cep.engine.EMoflonEvent;
import org.emoflon.cep.engine.TypeRegistry;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.common.operational.SimpleMatch;
		
import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import «imports.getPatternFQN(eventName, true)»;
import «imports.getMatchFQN(eventName)»;
«FOR field : model.getFields(eventName)»
import «imports.getFieldFQN(eventName, field.name)»;
«ENDFOR»
		
public class «names.getMatchEventName(eventName)» extends EMoflonEvent<P1Match, P1Pattern>{
			
	final public static String EVENT_NAME = "«eventName»";
	final public static EventType EVENT_TYPE = createEventType();
	
	public «names.getEventName(eventName)»(final «names.getPatternName(eventName, true)» pattern, final «names.getMatchName(eventName)» match) {
		super(pattern, match);
	}
			
	public «names.getEventName(eventName)»(final com.apama.event.Event apamaEvent, final TypeRegistry registry, final «names.getPatternName(eventName, true)» pattern) {
		super(apamaEvent, registry, pattern);
	}
	
	«FOR field : model.getFields(eventName)»
	public «ModelManager.getJavaFieldType(field)» get«StringUtil.firstToUpper(field.name)»() {
		return («ModelManager.getJavaFieldType(field)») fields.get("«field.name»");
	}
	«ENDFOR»

	@Override
	public void assignFields() {
		«FOR field : model.getFields(eventName)»
		fields.put("«field.name»", match.get«StringUtil.firstToUpper(field.name)»());
		«ENDFOR»
	}
		
	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
			
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		type.addField("vanished", FieldTypes.BOOLEAN);
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