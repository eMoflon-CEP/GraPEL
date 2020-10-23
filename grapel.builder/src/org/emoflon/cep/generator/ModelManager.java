package org.emoflon.cep.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EDataType;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXRule;

import GrapeLModel.ComplexAttribute;
import GrapeLModel.Event;
import GrapeLModel.EventAttribute;
import GrapeLModel.EventPattern;
import GrapeLModel.GrapeLModelContainer;
import GrapeLModel.RuleEvent;
import GrapeLModel.SimpleAttribute;
import GrapeLModel.VirtualEventAttribute;

public class ModelManager {
	
	private String projectName;
	private GrapeLModelContainer container;
	
	private Map<String, EventPattern> eventPatterns = new HashMap<>();
	private Map<String, Event> events = new HashMap<>();
	private Map<String, Map<String, EventAttribute>> fields = new LinkedHashMap<>();
	private Map<String, Map<String, SimpleAttribute>> parameterFields = new LinkedHashMap<>();
	private Map<String, IBeXRule> patternName2Rule = new LinkedHashMap<>();
	
	private void mapFields() {
		container.getEvents().forEach(event -> {
			events.put(event.getName(), event);
			Map<String, EventAttribute> localFields = new LinkedHashMap<>();
			fields.put(event.getName(), localFields);
			event.getAttributes().forEach(atr -> {
				localFields.put(atr.getName(), atr);
			});
			if(event instanceof RuleEvent) {
				Map<String, SimpleAttribute> localParameters = new LinkedHashMap<>();
				parameterFields.put(event.getName(), localParameters);
				((RuleEvent) event).getParameterAttributes().forEach(atr -> {
					localParameters.put(atr.getName(), atr);
				});
			}
		});
	}
	
	private void mapEventPatterns() {
		container.getEventPatterns().forEach(eventPattern -> {
			eventPatterns.put(eventPattern.getName(), eventPattern);
		});
	}
	
	public ModelManager(String projectName, GrapeLModelContainer container) {
		this.projectName = projectName;
		this.container = container;
		mapEventPatterns();
		mapFields();
	}
	
	public Collection<EventAttribute> getFields(String eventName) {
		return fields.get(eventName).values();
	}
	
	public Collection<SimpleAttribute> getParameterFields(String eventName) {
		return parameterFields.get(eventName).values();
	}
	
	public Collection<EventAttribute> getNonVirtualFields(String eventName) {
		return fields.get(eventName).values().stream().filter(eatr -> !(eatr instanceof VirtualEventAttribute)).distinct().collect(Collectors.toList());
	}
	
	public Collection<EventAttribute> getComplexFields(String eventName) {
		return getFields(eventName).stream().filter(field -> (field instanceof ComplexAttribute)).distinct().collect(Collectors.toList());
	}
	
	public Collection<EventAttribute> getApplicationEventFields(String eventName) {
		return Stream.concat(getParameterFields(eventName).stream(), getComplexFields(eventName).stream()).collect(Collectors.toList());
	}
	
	public EventPattern getEventPattern(String eventPattern) {
		return eventPatterns.get(eventPattern);
	}
	
	public boolean isMatchEventRuleEvent(String eventName) {
		return events.get(eventName) instanceof RuleEvent;
	}
	
	public static String getApamaFieldType(EventAttribute field) {
		if(field instanceof ComplexAttribute)
			return "FieldTypes.INTEGER";
		EDataType type = (field instanceof SimpleAttribute)?((SimpleAttribute)field).getType():((VirtualEventAttribute)field).getType();
		if(type.getName().equals("EInt") || type.getName().equals("EByte") || type.getName().equals("EShort") || type.getName().equals("ELong")) {
			return "FieldTypes.INTEGER";
		}else if(type.getName().equals("EDouble") || type.getName().equals("EFloat")) {
			return "FieldTypes.FLOAT";
		}else if(type.getName().equals("EString") || type.getName().equals("EChar")) {
			return "FieldTypes.STRING";
		}else if(type.getName().equals("EBoolean")) {
			return "FieldTypes.BOOLEAN";
		}else {
			throw new RuntimeException("Unsupported type: "+type.getName());
		}
	}
	
	public static String getJavaFieldType(EventAttribute field) {
		if(field instanceof ComplexAttribute) {
			ComplexAttribute cAtr = (ComplexAttribute)field;
			return cAtr.getType().getName();
		}else if(field instanceof SimpleAttribute) {
			SimpleAttribute sAtr = (SimpleAttribute)field;
			return sAtr.getType().getInstanceClassName();
		} else {
			VirtualEventAttribute vAtr = (VirtualEventAttribute)field;
			return vAtr.getType().getInstanceClassName();
		}
	}
	
	
	public static String asApamaType(final EventAttribute field) {
		if(field instanceof ComplexAttribute)
			return "integer";
		EDataType type = (field instanceof SimpleAttribute)?((SimpleAttribute)field).getType():((VirtualEventAttribute)field).getType();
		if(type.getName().equals("EInt") || type.getName().equals("EByte") || type.getName().equals("EShort") || type.getName().equals("ELong")) {
			return "integer";
		}else if(type.getName().equals("EDouble") || type.getName().equals("EFloat")) {
			return "float";
		}else if(type.getName().equals("EString") || type.getName().equals("EChar")) {
			return "string";
		}else if(type.getName().equals("EBoolean")) {
			return "boolean";
		}else {
			throw new RuntimeException("Unsupported type: "+type.getName());
		}

	}
	
	public static String eDataTypeAsApamaType(final EDataType type) {
		if(type.getName().equals("EInt") || type.getName().equals("EByte") || type.getName().equals("EShort") || type.getName().equals("ELong")) {
			return "integer";
		}else if(type.getName().equals("EDouble") || type.getName().equals("EFloat")) {
			return "float";
		}else if(type.getName().equals("EString") || type.getName().equals("EChar")) {
			return "string";
		}else if(type.getName().equals("EBoolean")) {
			return "boolean";
		}else {
			throw new RuntimeException("Unsupported type: "+type.getName());
		}
	}
	
	public static String eDataTypeDefaultValue(final EDataType type) {
		if(type.getName().equals("EInt") || type.getName().equals("EByte") || type.getName().equals("EShort") || type.getName().equals("ELong")) {
			return "0";
		}else if(type.getName().equals("EDouble") || type.getName().equals("EFloat")) {
			return "0.0";
		}else if(type.getName().equals("EString") || type.getName().equals("EChar")) {
			return "\"\"";
		}else if(type.getName().equals("EBoolean")) {
			return "true";
		}else {
			throw new RuntimeException("Unsupported type: "+type.getName());
		}
	}

	public String getProjectName() {
		return projectName;
	}
}
