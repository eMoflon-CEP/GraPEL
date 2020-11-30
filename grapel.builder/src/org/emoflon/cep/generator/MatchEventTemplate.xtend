package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventTemplate
import GrapeLModel.EventAttribute
import GrapeLModel.VirtualEventAttribute
import org.eclipse.emf.ecore.EEnum

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

import «imports.getPatternFQN(eventName, !model.isMatchEventRuleEvent(eventName))»;
import «imports.getMatchFQN(eventName)»;
«FOR fieldName : model.getComplexFields(eventName).map[field | imports.getFieldFQN(eventName, field.name)].toSet»
import «fieldName»;
«ENDFOR»
		
public class «names.getMatchEventName(eventName)» extends EMoflonEvent<«names.getMatchName(eventName)», «names.getPatternName(eventName, !model.isMatchEventRuleEvent(eventName))»>{
			
	final public static String EVENT_NAME = "«eventName»";
	final public static EventType EVENT_TYPE = createEventType();
	
	public «names.getEventName(eventName)»(final «names.getPatternName(eventName, !model.isMatchEventRuleEvent(eventName))» pattern, final «names.getMatchName(eventName)» match, boolean vanished) {
		super(pattern, match, vanished);
	}
			
	public «names.getEventName(eventName)»(final com.apama.event.Event apamaEvent, final TypeRegistry registry, final «names.getPatternName(eventName, !model.isMatchEventRuleEvent(eventName))» pattern) {
		super(apamaEvent, registry, pattern);
	}
	
	«FOR field : model.getNonVirtualFields(eventName)»
	public «ModelManager.getJavaFieldType(field)» get«StringUtil.firstToUpper(field.name)»() {
		return«IF !ModelManager.getJavaFieldType(field).equals("int")» («ModelManager.getJavaFieldType(field)») fields.get("«field.name»");
		«ELSE» ((Long)fields.get("«field.name»")).intValue();«ENDIF»
	}
	«ENDFOR»

	@Override
	public void assignFields() {
		fields.put("vanished", vanished);
		«FOR field : model.getFields(eventName)»
		fields.put("«field.name»", «getAccessAttribute(field)»);
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
		«FOR field : model.getComplexFields(eventName)»
		iMatch.put("«field.name»", fields.get("«field.name»"));
		«ENDFOR»
		this.match = new «names.getMatchName(eventName)»(pattern, iMatch);
	}	
}
'''		
	}
	
	def String getAccessAttribute(EventAttribute eAtr) {
		if(!(eAtr instanceof VirtualEventAttribute)) {
			return '''match.get«StringUtil.firstToUpper(eAtr.name)»()'''
		}else {
			val vAtr = eAtr as VirtualEventAttribute
			if(vAtr.type instanceof EEnum) {
				return '''"«vAtr.type.name»." + match.get«StringUtil.firstToUpper(vAtr.baseAttribute.name)»().get«StringUtil.firstToUpper(vAtr.attribute.name)»().getLiteral()'''
			}else {
				return '''match.get«StringUtil.firstToUpper(vAtr.baseAttribute.name)»().get«StringUtil.firstToUpper(vAtr.attribute.name)»()'''
			}
		}
	}
	
}