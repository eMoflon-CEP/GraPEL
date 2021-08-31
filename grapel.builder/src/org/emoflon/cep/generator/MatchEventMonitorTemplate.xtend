package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventMonitorTemplate

/**
 * Template for GrapeL Apama match event struct monitor generation
 */
class MatchEventMonitorTemplate extends EventMonitorTemplate {
	
	/**
	 * Constructor for a match event monitor template
	 * @param eventName the event name specifying the event the monitor works with
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 * @param model the manager that includes the utility for model access
	 */
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
	
	/**
	 * Generates the code for an match event struct
	 */
	def String generateMatchEvent() {
		return '''event «eventName» {
	boolean vanished;
	«FOR field : model.getFields(eventName)»
	«ModelManager.asApamaType(field)» «field.name»;
	«ENDFOR»
}
'''
	}
	
	/**
	 * Generates the code for a rule event struct
	 */
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