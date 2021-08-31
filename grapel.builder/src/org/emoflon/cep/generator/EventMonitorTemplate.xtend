package org.emoflon.cep.generator

/**
 * Template for GrapeL Apama event struct monitor generation
 */
class EventMonitorTemplate extends AbstractTemplate{
	
	/**
	 * The event the monitor works with
	 */
	protected String eventName;
	/**
	 * Model manager utility for GrapeL model
	 */
	protected ModelManager model;
	
	/**
	 * Constructor for an event monitor template
	 * @param eventName the name of the event the monitor works with
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 * @param model the manager that includes the utility for model access
	 */
	new(String eventName, ImportManager imports, NSManager names, PathManager paths, ModelManager model) {
		super(imports, names, paths)
		this.eventName = eventName;
		this.model = model;
	}
	
	override String generate() {
		return '''event «eventName» {
	«FOR field : model.getFields(eventName)»
	«ModelManager.asApamaType(field)» «field.name»;
	«ENDFOR»
}
'''
	}
	
	override getPath() {
		return paths.getEventMonitorLocation(eventName)
	}
	
}