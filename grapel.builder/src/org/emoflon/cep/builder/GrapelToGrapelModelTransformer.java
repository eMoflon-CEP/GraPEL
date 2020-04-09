package org.emoflon.cep.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.emoflon.cep.grapel.EditorGTFile;
import org.emoflon.ibex.gt.editor.gT.EditorPattern;
import org.emoflon.ibex.gt.transformations.EditorToIBeXPatternTransformation;
import GrapeLModel.Event;
import GrapeLModel.EventAttribute;
import GrapeLModel.EventPattern;
import GrapeLModel.EventPatternNode;
import GrapeLModel.EventNode;
import GrapeLModel.IBeXPatternNode;
import GrapeLModel.SimpleAttribute;
import IBeXLanguage.IBeXContextPattern;
import IBeXLanguage.IBeXPatternSet;
import GrapeLModel.ComplexAttribute;
import GrapeLModel.ContextConstraint;
import GrapeLModel.GrapeLModelContainer;
import GrapeLModel.GrapeLModelFactory;

public class GrapelToGrapelModelTransformer {
	
	private GrapeLModelFactory factory = GrapeLModelFactory.eINSTANCE;
	private GrapeLModelContainer container;
	private IBeXPatternSet ibexPatterns;
	
	private Map<EditorPattern, IBeXContextPattern> editor2IBeXPatterns = new HashMap<>();
	private Map<org.emoflon.cep.grapel.Event, Event> gEvent2Events = Collections.synchronizedMap(new HashMap<>()); 
	private Map<org.emoflon.cep.grapel.EventPatternNode, EventPatternNode> gNode2Nodes = Collections.synchronizedMap(new HashMap<>()); 
	
	public GrapeLModelContainer transform(EditorGTFile grapelFile) {
		container = factory.createGrapeLModelContainer();
		
		//transform GT to IBeXPatterns
		EditorToIBeXPatternTransformation ibexTransformer = new EditorToIBeXPatternTransformation();
		ibexPatterns = ibexTransformer.transform(grapelFile);
		container.getIbexPatterns().addAll(ibexPatterns.getContextPatterns().stream()
				.filter(cPattern -> (cPattern instanceof IBeXContextPattern))
				.map(cPattern -> (IBeXContextPattern)cPattern)
				.collect(Collectors.toList()));
		mapEditor2IBeXPatterns(grapelFile);
		
		//transform events
		container.getEvents().addAll(grapelFile.getEvents().parallelStream()
				.map(event -> transform(event))
				.collect(Collectors.toList()));
		
		//transform event patterns
		container.getEventPatterns().addAll(grapelFile.getEvenPatterns().parallelStream()
				.map(pattern -> transform(pattern))
				.collect(Collectors.toList()));
		
		return container;
	}
	
	private void mapEditor2IBeXPatterns(EditorGTFile grapelFile) {
		for(EditorPattern ePattern : grapelFile.getPatterns()) {
			for(IBeXContextPattern iPattern : container.getIbexPatterns()) {
				if(iPattern.getName().equals(ePattern)) {
					editor2IBeXPatterns.put(ePattern, iPattern);
				}
			}
		}
	}
	
	private Event transform(org.emoflon.cep.grapel.Event gEvent) {
		Event event = factory.createEvent();
		event.setName(gEvent.getName());
		event.getAttributes().addAll(gEvent.getAttributes().parallelStream()
				.map(attribute -> transform(attribute))
				.collect(Collectors.toList()));
		
		gEvent2Events.put(gEvent, event);
		return event;
	}
	
	private EventAttribute transform(org.emoflon.cep.grapel.EventAttribute gAttribute) {
		EventAttribute attribute = null;
		if(gAttribute.getType() instanceof EDataType) {
			attribute = factory.createSimpleAttribute();
			((SimpleAttribute)attribute).setType((EDataType) gAttribute.getType());
		}else {
			attribute = factory.createComplexAttribute();
			((ComplexAttribute)attribute).setType((EClass) gAttribute.getType());
		}
		attribute.setName(gAttribute.getName());
		
		return attribute;
	}
	
	private EventPattern transform(org.emoflon.cep.grapel.EventPattern gEventPattern) {
		EventPattern pattern = factory.createEventPattern();
		pattern.setName(gEventPattern.getName());
		pattern.getNodes().addAll(gEventPattern.getNodes().parallelStream()
				.map(eventPatternNode -> transform(eventPatternNode))
				.collect(Collectors.toList()));
		return pattern;
	}
	
	private EventPatternNode transform(org.emoflon.cep.grapel.EventPatternNode gEventPatternNode) {
		EventPatternNode patternNode = null;
		if(gEventPatternNode.getType() instanceof org.emoflon.cep.grapel.Event) {
			patternNode = factory.createEventNode();
			((EventNode)patternNode).setType((Event) gEventPatternNode.getType());
		}else {
			patternNode = factory.createIBeXPatternNode();
			((IBeXPatternNode)patternNode).setType((IBeXContextPattern) gEventPatternNode.getType());
		}
		
		gNode2Nodes.put(gEventPatternNode, patternNode);
		return patternNode;
	}
	
	private ContextConstraint transform(org.emoflon.cep.grapel.EventPatternContextConstraint gContextConstraint) {
		ContextConstraint constraint = null;
//		if(gContextConstraint instanceof )
		
		return constraint;
	}
}
