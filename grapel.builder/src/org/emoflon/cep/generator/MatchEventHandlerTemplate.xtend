package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventHandlerTemplate

class MatchEventHandlerTemplate extends EventHandlerTemplate {
	
	new(ImportManager imports, NSManager names, PathManager paths, ModelManager model, String patternName) {
		super(imports, names, paths, model, patternName)
	}
	
	override String generate() {
		return'''package «imports.packageFQN».eventhandler;

import java.io.IOException;
import java.util.function.Consumer;

import org.emoflon.cep.engine.EMoflonEventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.cep.util.IOUtils;

import com.apama.event.parser.EventType;

import «imports.EMoflonAPIFQN»;
import «imports.getMatchFQN(eventName)»;
import «imports.getPatternFQN(eventName, true)»;

import «imports.getEventFQN(eventName)»;

public class «names.getMatchEventHandlerName(eventName)» extends EMoflonEventHandler<«names.getMatchEventName(eventName)», «names.getMatchName(eventName)», «names.getPatternName(eventName, true)»>{
	
	
	final public static String HANDLER_NAME = "«names.getMatchEventHandlerName(eventName)»";
	final public static String[] CHANNELS = {"channel1"};
	final public static String EPL_PATH = "«paths.getMatchEventMonitorLocation(eventName)»";

	public «names.getMatchEventHandlerName(eventName)»(GrapeEngine engine) {
		super(engine);
	}
	
	@Override
	public «names.getPatternName(eventName, true)» getPattern() {
		return ((«names.EMoflonAPIName»)engine.getEMoflonAPI()).«eventName»();
	}
	
	@Override
	public void subscribeToPattern(Consumer<«names.getMatchName(eventName)»> appearing, Consumer<«names.getMatchName(eventName)»> disappearing) {
		pattern.subscribeAppearing(appearing);
		pattern.subscribeDisappearing(disappearing);
	}

	@Override
	public «names.getMatchEventName(eventName)» matchToEvent(«names.getMatchName(eventName)» match) {
		return new «names.getMatchEventName(eventName)»(pattern, match);
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
	public «names.getMatchEventName(eventName)» convertEvent(com.apama.event.Event apamaEvent) {
		return new «names.getMatchEventName(eventName)»(apamaEvent, registry, pattern);
	}

	@Override
	public EventType getEventType() {
		return «names.getMatchEventName(eventName)».EVENT_TYPE;
	}


}
'''
	}
	
}