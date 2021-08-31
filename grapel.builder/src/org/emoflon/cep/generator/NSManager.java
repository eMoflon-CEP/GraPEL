package org.emoflon.cep.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import GrapeLModel.GrapeLModelContainer;

/**
 * GrapeL name space manager 
 */
public class NSManager {
	
	// names for control monitors
	public static final String MAINTAINANCE_MONITOR = "Maintainance";
	// names for control events
	public static final String SYNCHRONIZATION_EVENT = "RequestSynchronizationEvent";
	public static final String UPDATE_EVENT = "UpdateEvent";
	
	private String projectName;
	private GrapeLModelContainer container;
	// mapping of events to event handlers
	private Map<String, String> event2handler = new HashMap<>();
	
	/**
	 * Maps events to event handlers
	 */
	private void mapHandlers() {
		container.getEvents().forEach(event -> {
			event2handler.put(event.getName(), StringUtil.firstToUpper(event.getName())+"EventHandler");
		});
	}
	
	/**
	 * Constructor for the name space manager
	 * @param projectName the name of the project
	 * @param container the model container that includes all elements that should be generated specified in the grapel file
	 */
	public NSManager(String projectName, GrapeLModelContainer container) {
		this.projectName = projectName;
		this.container = container;
		mapHandlers();
	}
	
	/**
	 * @return the package name from the GrapeL container
	 */
	public String getPkgName() {
		return StringUtil.firstToUpper(container.getName());
	}

	/**
	 * @return the name of the project GrapeL API
	 */
	public String getAPIName() {
		return StringUtil.firstToUpper(container.getName())+"GrapeLAPI";
	}
	
	/**
	 * @return the name of the project eMoflon App
	 */
	public String getEMoflonAppName() {
		return StringUtil.firstToUpper(StringUtil.lastSegment(projectName, "."))+"App";
	}
	
	/**
	 * @return the name of the project eMoflon API
	 */
	public String getEMoflonAPIName() {
		return StringUtil.firstToUpper(StringUtil.lastSegment(projectName, "."))+"API";
	}
	
	/**
	 * @return the names of the event handlers
	 */
	public Collection<String> getEventHandlerNames() {
		return event2handler.values();
	}
	
	/**
	 * @param engineName the name the engine
	 * @return the full GrapeL engine name for the package
	 */
	public String getEngineName(String engineName) {
		return getPkgName()+"GrapeL"+engineName+"Engine";
	}
	
	/**
	 * @param engineName the name of the engine
	 * @return the full EMoflon engine app name
	 */
	public String getEMoflonEngineAppName(String engineName) {
		return StringUtil.firstToUpper(StringUtil.lastSegment(projectName, "."))+engineName+"App";
	}
	
	/**
	 * @param eventName the name of the event
	 * @return the full event name
	 */
	public String getEventName(String eventName) {
		return StringUtil.firstToUpper(eventName)+"Event";
	}
	
	/**
	 * @param patternName the name of the pattern that produces the match event
	 * @return the full match event name
	 */
	public String getMatchEventName(String patternName) {
		return getEventName(patternName);
	}
	
	/**
	 * @param patternName the name of the pattern
	 * @param isPattern the indicator if this a pattern or a rule
	 * @return the full pattern name
	 */
	public String getPatternName(String patternName, boolean isPattern) {
		return StringUtil.firstToUpper(patternName)+((isPattern)?"Pattern":"Rule");
	}
	
	/**
	 * @param patternName the name of the pattern that produces the match
	 * @return the full match name
	 */
	public String getMatchName(String patternName) {
		return StringUtil.firstToUpper(patternName)+"Match";
	}
	
	/**
	 * @param eventName the name of the event for which to get the event handler name for
	 * @return the event handler name for a specific event
	 */
	public String getEventHandlerName(String eventName) {
		return event2handler.get(eventName);
	}
	
	/**
	 * @param eventName the name of the match event for which to get the event handler name for
	 * @return the event handler name for a specific match event
	 */
	public String getMatchEventHandlerName(String eventName) {
		return getEventHandlerName(eventName);
	}

}
