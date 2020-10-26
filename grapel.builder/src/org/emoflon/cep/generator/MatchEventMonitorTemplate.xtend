package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventMonitorTemplate

class MatchEventMonitorTemplate extends EventMonitorTemplate {
	
	new(String patternName, ImportManager imports, NSManager names, PathManager paths, ModelManager model) {
		super(patternName, imports, names, paths, model)
	}
	
	override String generate() {
		if(model.isMatchEventRuleEvent(eventName)){
			return generateRuleEvent;
		} else {
			return generateMatchEvent;
		}
	}
	
	def String generateMatchEvent() {
		return '''event «eventName» {
	boolean vanished;
	«FOR field : model.getFields(eventName)»
	«ModelManager.asApamaType(field)» «field.name»;
	«ENDFOR»
}
'''
	}
	
	def String generateRuleEvent() {
		return '''«generateMatchEvent»
		
event «eventName»Application {
	«FOR field : model.getApplicationEventFields(eventName)»
	«ModelManager.asApamaType(field)» «field.name»;
	«ENDFOR»
}
'''
	}
	
}