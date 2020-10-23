package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventTemplate
import GrapeLModel.EventAttribute
import GrapeLModel.VirtualEventAttribute

class RuleEventTemplate extends EventTemplate {
	
	new(ImportManager imports, NSManager names, PathManager paths, ModelManager model, String patternName) {
		super(imports, names, paths, model, patternName)
	}
	
	override String generate() {
		return '''package «imports.packageFQN».events;
		
import org.emoflon.cep.engine.EMoflonEvent;
import org.emoflon.cep.engine.TypeRegistry;
import org.emoflon.cep.engine.GrapeLMatch;
		
import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import «imports.getPatternFQN(eventName, false)»;
import «imports.getMatchFQN(eventName)»;
«FOR fieldName : model.getComplexFields(eventName).map[field | imports.getFieldFQN(eventName, field.name)].toSet»
import «fieldName»;
«ENDFOR»
		
public class «names.getMatchEventName(eventName)»Application extends EMoflonEvent<«names.getMatchName(eventName)», «names.getPatternName(eventName, false)»>{
			
	final public static String EVENT_NAME = "«eventName»Application";
	final public static EventType EVENT_TYPE = createEventType();
	
	public «names.getEventName(eventName)»Application(final «names.getPatternName(eventName, false)» pattern, final «names.getMatchName(eventName)» match, boolean vanished) {
		super(pattern, match, vanished);
	}
			
	public «names.getEventName(eventName)»Application(final com.apama.event.Event apamaEvent, final TypeRegistry registry, final «names.getPatternName(eventName, false)» pattern) {
		super(apamaEvent, registry, pattern);
	}
	
	«FOR field : model.getApplicationEventFields(eventName)»
	public «ModelManager.getJavaFieldType(field)» get«StringUtil.firstToUpper(field.name)»() {
		return («ModelManager.getJavaFieldType(field)») fields.get("«field.name»");
	}
	«ENDFOR»

	@Override
	public void assignFields() {
		throw new UnsupportedOperationException();
	}
		
	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
			
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		«FOR field : model.getApplicationEventFields(eventName)»
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
		«FOR field : model.getApplicationEventFields(eventName)»
		if("«field.name»".equals(fieldName)) {
			return «ModelManager.getJavaFieldType(field)».class;
		}
		«ENDFOR»
		return null;
	}
	
	@Override
	public void assignMatch() {
		GrapeLMatch iMatch = new GrapeLMatch(pattern.getPatternName());
		«FOR field : model.getNonVirtualFields(eventName)»
		iMatch.put("«field.name»", fields.get("«field.name»"));
		«ENDFOR»
		«FOR field : model.getParameterFields(eventName)»
		iMatch.addRuleParameter("«field.name»", fields.get("«field.name»"));
		«ENDFOR»
		«names.getMatchName(eventName)» match = new «names.getMatchName(eventName)»(pattern, iMatch);
		this.match = match;
	}
}
'''		
	}
	
	def String getAccessAttribute(EventAttribute eAtr) {
		if(!(eAtr instanceof VirtualEventAttribute)) {
			return '''match.get«StringUtil.firstToUpper(eAtr.name)»()'''
		}else {
			val vAtr = eAtr as VirtualEventAttribute
			return '''match.get«StringUtil.firstToUpper(vAtr.baseAttribute.name)»().get«StringUtil.firstToUpper(vAtr.attribute.name)»()'''
		}
	}
	
	override getPath() {
		return paths.getRuleEventLocation(eventName)
	}
	
}