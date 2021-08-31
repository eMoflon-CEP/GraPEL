package org.emoflon.cep.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;

import GrapeLModel.ComplexAttribute;
import GrapeLModel.Event;
import GrapeLModel.EventAttribute;
import GrapeLModel.EventPattern;
import GrapeLModel.GrapeLModelContainer;
import GrapeLModel.RuleEvent;
import GrapeLModel.SimpleAttribute;
import GrapeLModel.VirtualEventAttribute;

/**
 * Utility for managing GrapeL models
 */
public class ModelManager {
	
	// general project stuff
	private String projectName;
	private GrapeLModelContainer container;
	
	// event (pattern) maps
	private Map<String, EventPattern> eventPatterns = new HashMap<>();
	private Map<String, Event> events = new HashMap<>();
	// event (pattern) attributes
	private Map<String, Map<String, EventAttribute>> fields = new LinkedHashMap<>();
	private Map<String, Map<String, SimpleAttribute>> parameterFields = new LinkedHashMap<>();
	
	/**
	 * Maps event names to event attributes/fields and parameter attributes
	 */
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
	
	/**
	 * Maps event pattern names to event patterns
	 */
	private void mapEventPatterns() {
		container.getEventPatterns().forEach(eventPattern -> {
			eventPatterns.put(eventPattern.getName(), eventPattern);
		});
	}
	
	/**
	 * Constructor for the Model Manager
	 * @param projectName the name of the project
	 * @param container the model container that includes all elements that should be generated specified in the grapel file
	 */
	public ModelManager(String projectName, GrapeLModelContainer container) {
		this.projectName = projectName;
		this.container = container;
		mapEventPatterns();
		mapFields();
	}
	
	/**
	 * @param eventName the event name that specifies the event
	 * @return the event attribute fields for an event specified by a given name
	 */
	public Collection<EventAttribute> getFields(String eventName) {
		return fields.get(eventName).values();
	}
	
	/**
	 * @param eventName the event name that specifies the event
	 * @return the parameter fields for an event specified by a given name
	 */
	public Collection<SimpleAttribute> getParameterFields(String eventName) {
		return parameterFields.get(eventName).values();
	}
	
	/**
	 * @param eventName the event name specifying the event
	 * @return the event attributes for the fields of the specified event, that are non virtual attributes
	 */
	public Collection<EventAttribute> getNonVirtualFields(String eventName) {
		return fields.get(eventName).values().stream().filter(eatr -> !(eatr instanceof VirtualEventAttribute)).distinct().collect(Collectors.toList());
	}
	
	/**
	 * @param eventName the event name specifying the event
	 * @return the event attributes for the fields of the specified event, that are complex attributes
	 */
	public Collection<EventAttribute> getComplexFields(String eventName) {
		return getFields(eventName).stream().filter(field -> (field instanceof ComplexAttribute)).distinct().collect(Collectors.toList());
	}
	
	/**
	 * @param eventName the event name specifying the event
	 * @return the event attributes of the specified event
	 */
	public Collection<EventAttribute> getApplicationEventFields(String eventName) {
		return Stream.concat(getParameterFields(eventName).stream(), getComplexFields(eventName).stream()).collect(Collectors.toList());
	}
	
	/**
	 * @param eventPattern the event pattern name specifying the event pattern
	 * @return the event pattern for a given event pattern name
	 */
	public EventPattern getEventPattern(String eventPattern) {
		return eventPatterns.get(eventPattern);
	}
	
	/**
	 * @param eventName the event name specifying the event to be checked
	 * @return true, if the event with the eventName as specifier is a instance of a rule event
	 */
	public boolean isMatchEventRuleEvent(String eventName) {
		return events.get(eventName) instanceof RuleEvent;
	}
	
	/**
	 * @param field the attribute whose Apama field type should be returned
	 * @return the Apama field type for the given field
	 */
	public static String getApamaFieldType(EventAttribute field) {
		if(field instanceof ComplexAttribute)
			return "FieldTypes.INTEGER";
		EClassifier type = (field instanceof SimpleAttribute)?((SimpleAttribute)field).getType():((VirtualEventAttribute)field).getType();
		if(type.getName().equals("EInt") || type.getName().equals("EByte") || type.getName().equals("EShort") || type.getName().equals("ELong")) {
			return "FieldTypes.INTEGER";
		}else if(type.getName().equals("EDouble") || type.getName().equals("EFloat")) {
			return "FieldTypes.FLOAT";
		}else if(type.getName().equals("EString") || type.getName().equals("EChar")) {
			return "FieldTypes.STRING";
		}else if(type.getName().equals("EBoolean")) {
			return "FieldTypes.BOOLEAN";
		}else {
			if(type instanceof EEnum) {
				return "FieldTypes.STRING";
			} else {
				throw new RuntimeException("Unsupported type: "+type.getName());
			}
		}
	}
	
	/**
	 * @param field the attribute whose type should be returned as the java equivalent
	 * @return the java type for a given field
	 */
	public static String getJavaFieldType(EventAttribute field) {
		if(field instanceof ComplexAttribute) {
			ComplexAttribute cAtr = (ComplexAttribute)field;
			return cAtr.getType().getName();
		}else if(field instanceof SimpleAttribute) {
			SimpleAttribute sAtr = (SimpleAttribute)field;
			if(sAtr.getType() instanceof EEnum)
				return "java.lang.String";
			
			return sAtr.getType().getInstanceClassName();
		} else {
			VirtualEventAttribute vAtr = (VirtualEventAttribute)field;
			if(vAtr.getType() instanceof EEnum)
				return "java.lang.String";
			
			return vAtr.getType().getInstanceClassName();
		}
	}
	
	
	/**
	 * @param field the attribute whose type should be returned as the Apama equivalent
	 * @return the corresponding Apama type for the given attribute field
	 */
	public static String asApamaType(final EventAttribute field) {
		if(field instanceof ComplexAttribute)
			return "integer";
		EClassifier type = (field instanceof SimpleAttribute)?((SimpleAttribute)field).getType():((VirtualEventAttribute)field).getType();
		if(type.getName().equals("EInt") || type.getName().equals("EByte") || type.getName().equals("EShort") || type.getName().equals("ELong")) {
			return "integer";
		}else if(type.getName().equals("EDouble") || type.getName().equals("EFloat")) {
			return "float";
		}else if(type.getName().equals("EString") || type.getName().equals("EChar")) {
			return "string";
		}else if(type.getName().equals("EBoolean")) {
			return "boolean";
		}else {
			if(type instanceof EEnum) {
				return "string";
			} else {
				throw new RuntimeException("Unsupported type: "+type.getName());
			}
		}

	}
	
	/**
	 * @param type the EClassifier to be returned as the Apama equivalent
	 * @return the corresponding Apama type for given EClassifier
	 */
	public static String dataTypeAsApamaType(final EClassifier type) {
		if(type.getName().equals("EInt") || type.getName().equals("EByte") || type.getName().equals("EShort") || type.getName().equals("ELong")) {
			return "integer";
		}else if(type.getName().equals("EDouble") || type.getName().equals("EFloat")) {
			return "float";
		}else if(type.getName().equals("EString") || type.getName().equals("EChar")) {
			return "string";
		}else if(type.getName().equals("EBoolean")) {
			return "boolean";
		}else {
			if(type instanceof EEnum) {
				return "string";
			} else {
				throw new RuntimeException("Unsupported type: "+type.getName());
			}
		}
	}
	
	/**
	 * @param type the EClassifier whose default value should be returned
	 * @return the default value for a given EClassifier
	 */
	public static String eDataTypeDefaultValue(final EClassifier type) {
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

	/**
	 * @return the name of the project
	 */
	public String getProjectName() {
		return projectName;
	}
}
