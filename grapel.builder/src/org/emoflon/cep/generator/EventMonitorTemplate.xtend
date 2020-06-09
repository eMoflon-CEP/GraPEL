package org.emoflon.cep.generator

class EventMonitorTemplate extends AbstractTemplate{
	
	protected String eventName;
	protected ModelManager model;
	
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