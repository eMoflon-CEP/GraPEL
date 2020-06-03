package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventMonitorTemplate

class MatchEventMonitorTemplate extends EventMonitorTemplate {
	
	new(String patternName, ModelManager model) {
		super(patternName, model)
	}
	
	override String generate() {
		return '''event «eventName» {
boolean vanished;
«FOR field : model.getFields(eventName)»
«ModelManager.asApamaType(field)» «field.name»
«ENDFOR»
}
'''
	}
	
}