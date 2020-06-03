package org.emoflon.cep.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import GrapeLModel.GrapeLModelContainer;

public class NSManager {
	
	private String projectName;
	private GrapeLModelContainer container;
	private Map<String, String> event2handler = new HashMap<>();
	
	private void mapHandlers() {
		container.getEvents().forEach(event -> {
			event2handler.put(event.getName(), StringUtil.firstToUpper(event.getName())+"EventHandler");
		});
	}
	
	public NSManager(String projectName, GrapeLModelContainer container) {
		this.projectName = projectName;
		this.container = container;
		mapHandlers();
	}
	
	public String getPkgName() {
		return StringUtil.firstToUpper(container.getName());
	}

	public String getAPIName() {
		return StringUtil.firstToUpper(container.getName())+"GrapeLAPI";
	}
	
	public String getEMoflonAppName() {
		return StringUtil.firstToUpper(StringUtil.lastSegment(projectName, "."))+"App";
	}
	
	public String getEMoflonAPIName() {
		return StringUtil.firstToUpper(StringUtil.lastSegment(projectName, "."))+"API";
	}
	
	public Collection<String> getEventHandlerNames() {
		return event2handler.values();
	}
	
	public String getEngineName(String engineName) {
		return StringUtil.firstToUpper(container.getName())+"GrapeLEngine";
	}
	
	public String getEMoflonEngineAppName(String engineName) {
		return StringUtil.firstToUpper(StringUtil.lastSegment(projectName, "."))+engineName+"App";
	}
	
	public String getEventName(String eventName) {
		return StringUtil.firstToUpper(eventName)+"Event";
	}
	
	public String getMatchEventName(String patternName) {
		return getEventName(patternName);
	}
	
	public String getPatternName(String patternName, boolean isPattern) {
		return StringUtil.firstToUpper(patternName)+((isPattern)?"Pattern":"Rule");
	}
	
	public String getMatchName(String patternName) {
		return StringUtil.firstToUpper(patternName)+"Match";
	}
	
	public String getEventHandlerName(String eventName) {
		return event2handler.get(eventName);
	}
	
	public String getMatchEventHandlerName(String eventName) {
		return getEventHandlerName(eventName);
	}
}
