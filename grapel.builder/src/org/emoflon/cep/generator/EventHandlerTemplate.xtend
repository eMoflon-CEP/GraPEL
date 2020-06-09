package org.emoflon.cep.generator

import org.emoflon.cep.generator.AbstractTemplate

class EventHandlerTemplate extends AbstractTemplate {
	
	protected String eventName;
	protected ModelManager model;
	
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