package org.emoflon.cep.generator

import org.emoflon.cep.generator.AbstractTemplate

/**
 * Template for GrapeL event handler generation
 */
class EventHandlerTemplate extends AbstractTemplate {
	/**
	 * Name of the event the event handler works with
	 */
	protected String eventName;
	/**
	 * Model manager utility for GrapeL model
	 */
	protected ModelManager model;
	
	/**
	 * Constructor for an event handler template
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 * @param model the manager that includes the utility for model access
	 * @param eventName the name of the event the handler works with
	 */
	new(ImportManager imports, NSManager names, PathManager paths, ModelManager model, String eventName) {
		super(imports, names, paths)
		this.eventName = eventName;
		this.model = model;
	}
	
	override String generate() {
		return '''package «imports.packageFQN».eventhandler;

import java.io.IOException;

import org.emoflon.cep.engine.EventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.cep.util.IOUtils;

import com.apama.event.parser.EventType;

import «imports.getEventFQN(eventName)»;

public class «names.getEventHandlerName(eventName)» extends EventHandler<«names.getEventName(eventName)»>{
	
	final public static String HANDLER_NAME = "«names.getEventHandlerName(eventName)»";
	final public static String[] CHANNELS = {"channel1"};
	final public static String EPL_PATH = "«paths.getEventMonitorLocation(eventName)»";

	public «names.getEventHandlerName(eventName)»(GrapeEngine engine) {
		super(engine);
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

	@Override
	public String[] getChannelNames() {
		return CHANNELS;
	}

	@Override
	public String loadEPLDescription() {
		try {
			return IOUtils.loadTextFile(EPL_PATH);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public EventType getEventType() {
		return «names.getEventName(eventName)».EVENT_TYPE;
	}

	@Override
	public «names.getEventName(eventName)» convertEvent(com.apama.event.Event apamaEvent) {
		return new «names.getEventName(eventName)»(apamaEvent, registry);
	}

}
'''
	}
	
	override getPath() {
		paths.getEventHandlerLocation(eventName)
	}
	
}