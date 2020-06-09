package org.emoflon.cep.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EDataType;

import GrapeLModel.Event;
import GrapeLModel.AttributeConstraint;
import GrapeLModel.ComplexAttribute;
import GrapeLModel.EventAttribute;
import GrapeLModel.EventPattern;
import GrapeLModel.GrapeLModelContainer;
import GrapeLModel.SimpleAttribute;

public class ModelManager {
	
	private String projectName;
	private GrapeLModelContainer container;
	
	private Map<String, EventPattern> eventPatterns = new HashMap<>();
	private Map<String, Map<String, EventAttribute>> fields = new LinkedHashMap<>();
	
	private void mapFields() {
		container.getEvents().forEach(event -> {
			Map<String, EventAttribute> localFields = new LinkedHashMap<>();
			fields.put(event.getName(), localFields);
			event.getAttributes().forEach(atr -> {
				localFields.put(atr.getName(), atr);
			});
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
	
	public Collection<EventAttribute> getComplexFields(String eventName) {
		return getFields(eventName).stream().filter(field -> isComplexType(field)).collect(Collectors.toList());
	}
	
	public EventPattern getEventPattern(String eventPattern) {
		return eventPatterns.get(eventPattern);
	}
	
	public static String getApamaFieldType(EventAttribute field) {
		if(isComplexType(field))
			return "FieldTypes.INTEGER";
		EDataType type = ((SimpleAttribute)field).getType();
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
		if(isComplexType(field)) {
			ComplexAttribute cAtr = (ComplexAttribute)field;
			return cAtr.getType().getName();
		}else {
			SimpleAttribute sAtr = (SimpleAttribute)field;
			return sAtr.getType().getInstanceClassName();
		}
	}
	
	public static boolean isComplexType(final EventAttribute field) {
		if(field instanceof SimpleAttribute) {
			return false;
		} else {
			return true;	
		}
	}
	
	public static String asApamaType(final EventAttribute field) {
		if(isComplexType(field))
			return "integer";
		EDataType type = ((SimpleAttribute)field).getType();
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
}
