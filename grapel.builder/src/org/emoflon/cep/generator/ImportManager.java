package org.emoflon.cep.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClassifier;

import GrapeLModel.ComplexAttribute;
import GrapeLModel.EventAttribute;
import GrapeLModel.GrapeLModelContainer;

public class ImportManager {
	
	private String projectName;
	private GrapeLModelContainer container;
	private NSManager names;
	
	private Map<String, Map<String, EventAttribute>> fields = new HashMap<>();
	private Map<String, String> events = new HashMap<>();
	private Map<String, String> eventHandler = new HashMap<>();
	
	private void mapFields() {
		container.getEvents().forEach(event -> {
			Map<String, EventAttribute> localFields = new HashMap<>();
			fields.put(event.getName(), localFields);
			event.getAttributes().forEach(atr -> {
				localFields.put(atr.getName(), atr);
			});
		});
	}
	
	private void mapEvents() {
		container.getEvents().forEach(event -> {
			events.put(event.getName(), projectName + ".grapel." + names.getPkgName() + ".events." + names.getEventName(event.getName()));
			eventHandler.put(event.getName(), projectName + ".grapel." + names.getPkgName() + ".eventhandler." + names.getEventHandlerName(event.getName()));
		});
	}
	
	public ImportManager(String projectName, NSManager names, GrapeLModelContainer container) {
		this.projectName = projectName;
		this.names = names;
		this.container = container;
		mapFields();
		mapEvents();
	}

	
	public String getEMoflonAppFQN() {
		return projectName+".api."+StringUtil.firstToUpper(StringUtil.lastSegment(projectName, ".")+"App");
	}
	
	public String getEMoflonAPIFQN() {
		return projectName+".api."+StringUtil.firstToUpper(StringUtil.lastSegment(projectName, ".")+"API");
	}
	
	public String getEMoflonEngineAppFQN(String engineName) {
		return projectName+".api."+StringUtil.firstToUpper(StringUtil.lastSegment(projectName, ".")+engineName+"App");
	}
	
	public String getPackageFQN() {
		return projectName+".grapel."+names.getPkgName();
	}
	
	public String getAPIFQN() {
		return projectName+".grapel."+names.getPkgName()+"."+names.getAPIName();
	}
	
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
	
	public String getPatternFQN(String patternName, boolean isPattern) {
		return projectName+".api.rules."+StringUtil.firstToUpper(patternName)+((isPattern)?"Pattern":"Rule");
	}
	
	public String getMatchFQN(String patternName) {
		return projectName+".api.matches."+StringUtil.firstToUpper(patternName)+"Match";
	}
	
	public Collection<String> getEventFQNs() {
		return events.values();
	}
	
	public Collection<String> getEventHandlerFQNs() {
		return eventHandler.values();
	}
	
	public String getEventFQN(String eventName) {
//		return projectName+".grapel."+StringUtil.firstToUpper(container.getName()+".events."+names.getEventName(eventName));
		return events.get(eventName);
	}
	
	public String getEventHandlerFQN(String eventName) {
//		return projectName+".grapel."+StringUtil.firstToUpper(container.getName()+".events."+names.getEventName(eventName));
		return eventHandler.get(eventName);
	}
	
}
