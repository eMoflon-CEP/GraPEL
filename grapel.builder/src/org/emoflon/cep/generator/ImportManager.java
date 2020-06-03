package org.emoflon.cep.generator;

import java.util.HashMap;
import java.util.Map;

import GrapeLModel.ComplexAttribute;
import GrapeLModel.EventAttribute;
import GrapeLModel.GrapeLModelContainer;

public class ImportManager {
	
	private String projectName;
	private GrapeLModelContainer container;
	private NSManager names;
	
	private Map<String, Map<String, EventAttribute>> fields = new HashMap<>();
	
	private void mapFields() {
		container.getEvents().forEach(event -> {
			Map<String, EventAttribute> localFields = new HashMap<>();
			fields.put(event.getName(), localFields);
			event.getAttributes().forEach(atr -> {
				localFields.put(atr.getName(), atr);
			});
		});
	}
	
	public ImportManager(String projectName, NSManager names, GrapeLModelContainer container) {
		this.projectName = projectName;
		this.names = names;
		this.container = container;
		mapFields();
	}

	public String getPackageFQN() {
		return projectName+".grapel."+names.getPkgName();
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
		return cAtr.getType().getInstanceClass().getName();
	}
	
	public String getPatternFQN(String patternName, boolean isPattern) {
		return projectName+".api.rules."+StringUtil.firstToUpper(patternName)+((isPattern)?"Pattern":"Rule");
	}
	
	public String getMatchFQN(String patternName) {
		return projectName+".api.rules."+StringUtil.firstToUpper(patternName)+"Match";
	}
	
	public String getEventFQN(String eventName) {
		return projectName+".grapel."+StringUtil.firstToUpper(container.getName()+".events."+names.getEventName(eventName));
	}
	
}
