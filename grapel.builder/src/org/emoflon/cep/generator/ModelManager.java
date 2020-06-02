package org.emoflon.cep.generator;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;

public class ModelManager {
	
	public List<EAttribute> getFields(String eventName) {
		return null;
	}
	
	public List<EAttribute> getComplexFields(String eventName) {
		return getFields(eventName).stream().filter(field -> isComplexType(field.getEType())).collect(Collectors.toList());
	}
	
	public String getApamaFieldType(String eventName, EAttribute field) {
		return null;
	}
	
	public static boolean isComplexType(final EClassifier classifier) {
		if(classifier instanceof EDataType) {
			return false;
		} else {
			return true;	
		}
	}
	
	public static String asApamaType(final EClassifier classifier) {
		if(isComplexType(classifier))
			return "integer";
		if(classifier instanceof EEnum)
			throw new RuntimeException("Unsupported type: "+classifier.getName());
		
		EDataType type = (EDataType)classifier;
		if(type.getName().equals("EInt") || type.getName().equals("EByte") || type.getName().equals("EShort") || type.getName().equals("ELong")) {
			return "integer";
		}else if(type.getName().equals("EDouble") || type.getName().equals("EFloat")) {
			return "float";
		}else if(type.getName().equals("EString") || type.getName().equals("EChar")) {
			return "string";
		}else if(type.getName().equals("EBoolean")) {
			return "boolean";
		}else {
			throw new RuntimeException("Unsupported type: "+classifier.getName());
		}

	}
}
