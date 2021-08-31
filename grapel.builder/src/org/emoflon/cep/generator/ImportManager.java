package org.emoflon.cep.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClassifier;

import GrapeLModel.ComplexAttribute;
import GrapeLModel.EventAttribute;
import GrapeLModel.GrapeLModelContainer;

/**
 * Utility for managing the imports in a GrapeL project
 */
public class ImportManager {
	
	// general project stuff
	private String projectName;
	private GrapeLModelContainer container;
	private NSManager names;
	
	// mappings for events, event attributes and event handlers
	private Map<String, Map<String, EventAttribute>> fields = new HashMap<>();
	private Map<String, String> events = new HashMap<>();
	private Map<String, String> eventHandler = new HashMap<>();
	
	/**
	 * Maps the event names of the events from the model container to the names of event attributes/fields
	 */
	private void mapFields() {
		container.getEvents().forEach(event -> {
			Map<String, EventAttribute> localFields = new HashMap<>();
			fields.put(event.getName(), localFields);
			event.getAttributes().forEach(atr -> {
				localFields.put(atr.getName(), atr);
			});
		});
	}
	
	/**
	 * Maps the event names of events from the model container to fully qualified names for events and event handlers
	 */
	private void mapEvents() {
		container.getEvents().forEach(event -> {
			events.put(event.getName(), projectName + ".grapel." + names.getPkgName() + ".events." + names.getEventName(event.getName()));
			eventHandler.put(event.getName(), projectName + ".grapel." + names.getPkgName() + ".eventhandler." + names.getEventHandlerName(event.getName()));
		});
	}
	
	/**
	 * Constructor for the import manager
	 * @param projectName the name of the project
	 * @param names the manager including the name mapping for the project
	 * @param container the model container that includes all elements that should be generated specified in the grapel file
	 */
	public ImportManager(String projectName, NSManager names, GrapeLModelContainer container) {
		this.projectName = projectName;
		this.names = names;
		this.container = container;
		mapFields();
		mapEvents();
	}


	/**
	 * @return the fully qualified name of the project eMoflon APP
	 */
	public String getEMoflonAppFQN() {
		return projectName+".api."+StringUtil.firstToUpper(StringUtil.lastSegment(projectName, ".")+"App");
	}
	

	/**
	 * @return the fully qualified name of the project eMoflon API
	 */
	public String getEMoflonAPIFQN() {
		return projectName+".api."+StringUtil.firstToUpper(StringUtil.lastSegment(projectName, ".")+"API");
	}
	
	/**
	 * @return the fully qualified name of the project eMoflon engine APP
	 */
	public String getEMoflonEngineAppFQN(String engineName) {
		return projectName+".api."+StringUtil.firstToUpper(StringUtil.lastSegment(projectName, ".")+engineName+"App");
	}
	
	/**
	 * @return the fully qualified name of the project package
	 */
	public String getPackageFQN() {
		return projectName+".grapel."+names.getPkgName();
	}
	
	/**
	 * @return the fully qualified name of the project GrapeL API
	 */
	public String getAPIFQN() {
		return projectName+".grapel."+names.getPkgName()+"."+names.getAPIName();
	}

	/**
	 * @param eventName the event name specifying the event
	 * @param fieldName the field name specifying the field
	 * @return the fully qualified name for a given event field
	 */
	public String getFieldFQN(String eventName, String fieldName) {
		Map<String, EventAttribute> localFields = fields.get(eventName);
		if(localFields == null)
			return null;
		
		EventAttribute eAtr = localFields.get(fieldName);
		if(eAtr == null)
			return null;
		
		if(!(eAtr instanceof ComplexAttribute))
			return null;
		
		ComplexAttribute cAtr = (ComplexAttribute)eAtr;
		EClassifier type = cAtr.getType();
		if(type.getInstanceClassName() != null) {
			return type.getInstanceClassName();
		}else {
			return FQNUtil.getFQClassName(type.getEPackage())+"."+type.getName();
		}
	}
	
	/**
	 * @param patternName the pattern name specifying the pattern
	 * @param isPattern the flag indicating if the given pattern is of the pattern or the rule type
	 * @return the fully qualified name for the specified pattern
	 */
	public String getPatternFQN(String patternName, boolean isPattern) {
		return projectName+".api.rules."+StringUtil.firstToUpper(patternName)+((isPattern)?"Pattern":"Rule");
	}
	
	/**
	 * @param patternName the pattern name specifying the pattern, that produces the match
	 * @return the fully qualified name for the specified match
	 */
	public String getMatchFQN(String patternName) {
		return projectName+".api.matches."+StringUtil.firstToUpper(patternName)+"Match";
	}
	
	/**
	 * @return the fully qualified names for all events
	 */
	public Collection<String> getEventFQNs() {
		return events.values();
	}
	
	/**
	 * @return the fully qualified names for all event handlers
	 */
	public Collection<String> getEventHandlerFQNs() {
		return eventHandler.values();
	}
	
	
	/**
	 * @param eventName the event name specifying the event
	 * @return the fully qualified name for the specified event
	 */
	public String getEventFQN(String eventName) {
//		return projectName+".grapel."+StringUtil.firstToUpper(container.getName()+".events."+names.getEventName(eventName));
		return events.get(eventName);
	}
	
	/**
	 * @param eventName the event name specifying the event handler
	 * @return the fully qualified name for the specified event handler
	 */
	public String getEventHandlerFQN(String eventName) {
//		return projectName+".grapel."+StringUtil.firstToUpper(container.getName()+".events."+names.getEventName(eventName));
		return eventHandler.get(eventName);
	}
	
}
