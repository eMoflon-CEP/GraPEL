package org.emoflon.cep.generator

class EventMonitorTemplate {
	
	protected String eventName;
	protected ModelManager model;
	
	new(String eventName, ModelManager model) {
		this.eventName = eventName;
		this.model = model;
	}
	
	def String generate() {
		return '''event «eventName» {
«FOR field : model.getFields(eventName)»
«ModelManager.asApamaType(field)» «field.name»
«ENDFOR»
}
'''
	}
}