package org.emoflon.cep.generator

import org.emoflon.cep.generator.EventHandlerTemplate

/**
 * Template for GrapeL rule event handler generation
 */
class RuleEventHandlerTemplate extends EventHandlerTemplate {
	
	/**
	 * Constructor for a rule event handler template
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 * @param model the manager that includes the utility for model access
	 * @param patternName the pattern name specifying the pattern the rule event handler works with
	 */
	new(ImportManager imports, NSManager names, PathManager paths, ModelManager model, String patternName) {
		super(imports, names, paths, model, patternName)
	}
	
	override String generate() {
		return'''package «imports.packageFQN».eventhandler;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.Optional;

import org.emoflon.cep.engine.EMoflonRuleEventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.cep.util.IOUtils;

import com.apama.event.parser.EventType;

import «imports.EMoflonAPIFQN»;
import «imports.getMatchFQN(eventName)»;
import «imports.getPatternFQN(eventName, false)»;

import «imports.getEventFQN(eventName)»;
import «imports.getEventFQN(eventName)»Application;

public class «names.getMatchEventHandlerName(eventName)» extends EMoflonRuleEventHandler<«names.getMatchEventName(eventName)»Application, «names.getMatchEventName(eventName)», «names.getMatchName(eventName)», «names.getPatternName(eventName, false)»>{
	
	
	final public static String HANDLER_NAME = "«names.getMatchEventHandlerName(eventName)»";
	final public static String[] CHANNELS = {"channel1"};
	final public static String EPL_PATH = "«paths.getMatchEventMonitorLocation(eventName)»";

	public «names.getMatchEventHandlerName(eventName)»(GrapeEngine engine) {
		super(engine);
	}
	
	@Override
	public «names.getPatternName(eventName, false)» getPattern() {
		return ((«names.EMoflonAPIName»)engine.getEMoflonAPI()).«eventName»(«FOR param : model.getParameterFields(eventName) SEPARATOR ', '»«ModelManager.eDataTypeDefaultValue(param.type)»«ENDFOR»);
	}
	
	@Override
	public void subscribeToPattern(Consumer<«names.getMatchName(eventName)»> appearing, Consumer<«names.getMatchName(eventName)»> disappearing) {
		pattern.subscribeAppearing(appearing);
		pattern.subscribeDisappearing(disappearing);
	}

	@Override
	public «names.getMatchEventName(eventName)» matchToEvent(«names.getMatchName(eventName)» match, boolean vanished) {
		return new «names.getMatchEventName(eventName)»(pattern, match, vanished);
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
	protected Optional<«names.getMatchName(eventName)»> apply(«names.getMatchName(eventName)» match, «names.getMatchEventName(eventName)»Application event) {
		«FOR param : model.getParameterFields(eventName)»
		pattern.set«param.name.toFirstUpper»(event.get«param.name.toFirstUpper»());
		«ENDFOR»
		return pattern.apply(match);
	}

	@Override
	public «names.getMatchEventName(eventName)» convertEvent(com.apama.event.Event apamaEvent) {
		return new «names.getMatchEventName(eventName)»(apamaEvent, registry, pattern);
	}
	
	@Override
	public «names.getMatchEventName(eventName)»Application convertRuleEvent(com.apama.event.Event apamaEvent) {
		return new «names.getMatchEventName(eventName)»Application(apamaEvent, registry, pattern);
	}

	@Override
	public EventType getEventType() {
		return «names.getMatchEventName(eventName)».EVENT_TYPE;
	}

	@Override
	public EventType getRuleEventType() {
		return «names.getMatchEventName(eventName)»Application.EVENT_TYPE;
	}
}
'''
	}
	
}