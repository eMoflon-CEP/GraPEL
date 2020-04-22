package org.emoflon.cep.builder;

import java.nio.channels.GatheringByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.emoflon.cep.grapel.EditorGTFile;
import org.emoflon.ibex.gt.editor.gT.EditorNode;
import org.emoflon.ibex.gt.editor.gT.EditorPattern;
import org.emoflon.ibex.gt.transformations.EditorToIBeXPatternTransformation;
import GrapeLModel.Event;
import GrapeLModel.EventAttribute;
import GrapeLModel.EventPattern;
import GrapeLModel.EventPatternNode;
import GrapeLModel.EventPatternNodeExpression;
import GrapeLModel.EventNode;
import GrapeLModel.EventNodeExpression;
import GrapeLModel.IBeXPatternNode;
import GrapeLModel.IBeXPatternNodeExpression;
import GrapeLModel.IntegerLiteral;
import GrapeLModel.NodeContextConstraint;
import GrapeLModel.RelationalConstraint;
import GrapeLModel.RelationalConstraintLiteral;
import GrapeLModel.RelationalConstraintOperator;
import GrapeLModel.RelationalConstraintProduction;
import GrapeLModel.RelationalExpression;
import GrapeLModel.RelationalExpressionLiteral;
import GrapeLModel.RelationalExpressionOperator;
import GrapeLModel.RelationalExpressionProduction;
import GrapeLModel.SimpleAttribute;
import GrapeLModel.StringLiteral;
import IBeXLanguage.IBeXContextPattern;
import IBeXLanguage.IBeXNode;
import IBeXLanguage.IBeXPatternSet;
import GrapeLModel.ArithmeticExpression;
import GrapeLModel.ArithmeticExpressionLiteral;
import GrapeLModel.ArithmeticExpressionOperator;
import GrapeLModel.ArithmeticExpressionProduction;
import GrapeLModel.ArithmeticValue;
import GrapeLModel.ArithmeticValueExpression;
import GrapeLModel.ArithmeticValueLiteral;
import GrapeLModel.AttributeConstraint;
import GrapeLModel.AttributeConstraintExpression;
import GrapeLModel.AttributeConstraintLiteral;
import GrapeLModel.AttributeConstraintOperator;
import GrapeLModel.AttributeConstraintProduction;
import GrapeLModel.AttributeConstraintRelation;
import GrapeLModel.AttributeExpressionLiteral;
import GrapeLModel.BooleanLiteral;
import GrapeLModel.ComplexAttribute;
import GrapeLModel.ContextConstraint;
import GrapeLModel.ContextRelation;
import GrapeLModel.DoubleLiteral;
import GrapeLModel.GrapeLModelContainer;
import GrapeLModel.GrapeLModelFactory;

public class GrapelToGrapelModelTransformer {
	
	private GrapeLModelFactory factory = GrapeLModelFactory.eINSTANCE;
	private GrapeLModelContainer container;
	
	private Map<EditorPattern, IBeXContextPattern> editor2IBeXPatterns = new HashMap<>();
	private Map<org.emoflon.cep.grapel.Event, Event> gEvent2Events = Collections.synchronizedMap(new HashMap<>());
	private Map<Object, Object> gEventAttr2Attr = Collections.synchronizedMap(new HashMap<>());
	private Map<org.emoflon.cep.grapel.EventPatternNode, EventPatternNode> gNode2Nodes = Collections.synchronizedMap(new HashMap<>()); 
	
	public GrapeLModelContainer transform(EditorGTFile grapelFile) {
		container = factory.createGrapeLModelContainer();
		
		//transform GT to IBeXPatterns
		EditorToIBeXPatternTransformation ibexTransformer = new EditorToIBeXPatternTransformation();
		container.getIbexPatterns().add(ibexTransformer.transform(grapelFile));
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
			for(IBeXContextPattern iPattern : container.getIbexPatterns().stream()
					.flatMap(ps -> ps.getContextPatterns().stream())
					.filter(cp -> (cp instanceof IBeXContextPattern))
					.map(cp -> (IBeXContextPattern)cp)
					.collect(Collectors.toList())) {
				if(iPattern.getName().equals(ePattern.getName())) {
					editor2IBeXPatterns.put(ePattern, iPattern);
					for(EditorNode eNode : ePattern.getNodes()) {
						for(IBeXNode iNode : iPattern.getSignatureNodes()) {
							if(eNode.getName().equals(iNode.getName())) {
								gEventAttr2Attr.put(eNode, iNode);
							}
						}
					}
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
		gEventAttr2Attr.put(gAttribute, attribute);
		return attribute;
	}
	
	private EventPattern transform(org.emoflon.cep.grapel.EventPattern gEventPattern) {
		EventPattern pattern = factory.createEventPattern();
		pattern.setName(gEventPattern.getName());
		// transform pattern nodes
		pattern.getNodes().addAll(gEventPattern.getNodes().parallelStream()
				.map(eventPatternNode -> transform(eventPatternNode))
				.collect(Collectors.toList()));
		
		// transform context constraints
		pattern.getContextConstraints().addAll(gEventPattern.getContextConstraints().parallelStream()
				.map(contextConstraints -> transform(contextConstraints))
				.collect(Collectors.toList()));
		
		// transform relational constraints
		List<org.emoflon.cep.grapel.EventPatternRelationalConstraint> gRelationalConstraints = new LinkedList<>();
		gRelationalConstraints.addAll(gEventPattern.getRelationalConstraints());
		
		List<org.emoflon.cep.grapel.RelationalConstraintRelation> gRelations = new LinkedList<>();
		gRelations.addAll(gEventPattern.getRelConstraintRelations());
		
		pattern.setRelationalConstraint(transform(gRelationalConstraints, gRelations));
		
		// TODO: transform return values!
		
		return pattern;
	}
	
	private EventPatternNode transform(org.emoflon.cep.grapel.EventPatternNode gEventPatternNode) {
		EventPatternNode patternNode = null;
		if(gEventPatternNode.getType() instanceof org.emoflon.cep.grapel.Event) {
			patternNode = factory.createEventNode();
			((EventNode)patternNode).setType(gEvent2Events.get(gEventPatternNode.getType()));
		}else {
			patternNode = factory.createIBeXPatternNode();
			((IBeXPatternNode)patternNode).setType(editor2IBeXPatterns.get(gEventPatternNode.getType()));
		}
		patternNode.setName(gEventPatternNode.getName());
		gNode2Nodes.put(gEventPatternNode, patternNode);
		return patternNode;
	}
	
	private ContextConstraint transform(org.emoflon.cep.grapel.EventPatternContextConstraint gContextConstraint) {
		NodeContextConstraint constraint = factory.createNodeContextConstraint();
		constraint.setRelation(transform(gContextConstraint.getRelation()));
		constraint.setLhs(transform(gContextConstraint.getLhs()));
		constraint.setRhs(transform(gContextConstraint.getRhs()));
		return constraint;
	}
	
	private ContextRelation transform(org.emoflon.cep.grapel.ContextRelation gContextRelation) {
		switch(gContextRelation) {
		case EQUAL:
			return ContextRelation.EQUAL;
		case UNEQUAL:
			return ContextRelation.UNEQUAL;
		
		}
		return null;
	}
	
	//TODO: CCE Bug
	private EventPatternNodeExpression transform(org.emoflon.cep.grapel.EventPatternNodeExpression gNodeExpression) {
		if(gNodeExpression.getPatternNode().getType() instanceof org.emoflon.cep.grapel.Event) {
			EventNodeExpression nodeExpression = factory.createEventNodeExpression();
			nodeExpression.setEventPatternNode(gNode2Nodes.get(gNodeExpression.getPatternNode()));
			nodeExpression.setEventAttribute((EventAttribute)gEventAttr2Attr.get(gNodeExpression.getAttribute()));
			return nodeExpression;
		}else {
			IBeXPatternNodeExpression nodeExpression = factory.createIBeXPatternNodeExpression();
			nodeExpression.setEventPatternNode(gNode2Nodes.get(gNodeExpression.getPatternNode()));
			nodeExpression.setPatternAttribute((IBeXNode)gEventAttr2Attr.get(gNodeExpression.getAttribute()));
			return nodeExpression;
		}
	}
	
	private RelationalConstraint transform(List<org.emoflon.cep.grapel.EventPatternRelationalConstraint> gRelationalConstraints, List<org.emoflon.cep.grapel.RelationalConstraintRelation> gRelations) {
		if(gRelationalConstraints.size()<2) {
			org.emoflon.cep.grapel.EventPatternRelationalConstraint gConstraint = gRelationalConstraints.remove(0);
			RelationalConstraintLiteral rcl = factory.createRelationalConstraintLiteral();
			
			List<org.emoflon.cep.grapel.EventPatternNode> gNodes = new LinkedList<>();
			gNodes.addAll(gConstraint.getOperands());
			List<org.emoflon.cep.grapel.RelationalOperator> gOperators = new LinkedList<>();
			gOperators.addAll(gConstraint.getOperators());
			
			rcl.setRelationalExpression(transformRE(gNodes, gOperators));
			
			List<org.emoflon.cep.grapel.AttributeConstraint> gConstraints = new LinkedList<>();
			gConstraints.addAll(gConstraint.getConstraints());
			List<org.emoflon.cep.grapel.AttributeConstraintRelation> gSubRelations = new LinkedList<>();
			gSubRelations.addAll(gConstraint.getRelations());
			
			rcl.setAttributeConstraint(transformAC(gConstraints, gSubRelations));
			
			return rcl;
		}
		
		RelationalConstraintProduction rcp = factory.createRelationalConstraintProduction();
		rcp.setOp(transform(gRelations.remove(0)));
		
		List<org.emoflon.cep.grapel.EventPatternRelationalConstraint> leftConstraint = new LinkedList<>();
		leftConstraint.add(gRelationalConstraints.remove(0));
		rcp.setLhs(transform(leftConstraint, new LinkedList<>()));
		
		rcp.setRhs(transform(gRelationalConstraints, gRelations));
		
		return rcp;
	}
	
	private RelationalConstraintOperator transform(org.emoflon.cep.grapel.RelationalConstraintRelation gRelation) {
		switch(gRelation) {
		case AND:
			return RelationalConstraintOperator.AND;
		case OR:
			return RelationalConstraintOperator.OR;
		default:
			return null;
		
		}
	}
	
	private RelationalExpression transformRE(List<org.emoflon.cep.grapel.EventPatternNode> gNodes, List<org.emoflon.cep.grapel.RelationalOperator> gOperators) {
		if(gNodes.size()<2) {
			org.emoflon.cep.grapel.EventPatternNode gNode = gNodes.remove(0);
			RelationalExpressionLiteral rel = factory.createRelationalExpressionLiteral();
			rel.setEventPatternNode(gNode2Nodes.get(gNode));
			return rel;
		}
		
		RelationalExpressionProduction rep = factory.createRelationalExpressionProduction();
		rep.setOp(transform(gOperators.remove(0)));
		
		List<org.emoflon.cep.grapel.EventPatternNode> leftConstraint = new LinkedList<>();
		leftConstraint.add(gNodes.remove(0));
		rep.setLhs(transformRE(leftConstraint, new LinkedList<>()));
		
		rep.setRhs(transformRE(gNodes, gOperators));
		
		return rep;
	}
	
	private RelationalExpressionOperator transform(org.emoflon.cep.grapel.RelationalOperator gOperator) {
		switch(gOperator) {
		case AND:
			return RelationalExpressionOperator.AND;
		case FOLLOWS:
			return RelationalExpressionOperator.FOLLOWS;
		case OR:
			return RelationalExpressionOperator.OR;
		default:
			return null;
		
		
		}
	}
	
	private AttributeConstraint transformAC(List<org.emoflon.cep.grapel.AttributeConstraint> gConstraints, List<org.emoflon.cep.grapel.AttributeConstraintRelation> gRelations) {
		if(gConstraints.size()<2) {
			org.emoflon.cep.grapel.AttributeConstraint gConstraint = gConstraints.remove(0);
			AttributeConstraintLiteral acl = factory.createAttributeConstraintLiteral();
			AttributeConstraintExpression ace = factory.createAttributeConstraintExpression();
			acl.setConstraintExpression(ace);
			
			ace.setOp(transform(gConstraint.getRelation()));
			ace.setLhs(transform(gConstraint.getLhs()));
			ace.setRhs(transform(gConstraint.getRhs()));
			
			return acl;
		}
		
		AttributeConstraintProduction acp = factory.createAttributeConstraintProduction();
		acp.setOp(transform(gRelations.remove(0)));
		
		List<org.emoflon.cep.grapel.AttributeConstraint> leftConstraint = new LinkedList<>();
		leftConstraint.add(gConstraints.remove(0));
		acp.setLhs(transformAC(leftConstraint, new LinkedList<>()));
		
		acp.setRhs(transformAC(gConstraints, gRelations));
		
		return acp;
	}
	
	private AttributeConstraintOperator transform(org.emoflon.cep.grapel.AttributeConstraintRelation gRelation) {
		switch(gRelation) {
		case AND:
			return AttributeConstraintOperator.AND;
		case OR:
			return AttributeConstraintOperator.OR;
		default:
			return null;
		}
	}
	
	private AttributeConstraintRelation transform(org.emoflon.cep.grapel.AttributeRelation gRelation) {
		switch(gRelation) {
		case EQUAL:
			return AttributeConstraintRelation.EQUAL;
		case GREATER:
			return AttributeConstraintRelation.GREATER;
		case GREATER_OR_EQUAL:
			return AttributeConstraintRelation.GREATER_OR_EQUAL;
		case SMALLER:
			return AttributeConstraintRelation.SMALLER;
		case SMALLER_OR_EQUAL:
			return AttributeConstraintRelation.SMALLER_OR_EQUAL;
		case UNEQUAL:
			return AttributeConstraintRelation.UNEQUAL;
		default:
			return null;
		}
	}
	
	private ArithmeticExpression transform(org.emoflon.cep.grapel.AttributeExpression gExpression) {
		List<org.emoflon.cep.grapel.AttributeExpressionOperand> gOperands = new LinkedList<>();
		gOperands.addAll(gExpression.getOperands());
		List<org.emoflon.cep.grapel.ArithmeticOperator> gOperators = new LinkedList<>();
		gOperators.addAll(gExpression.getOperators());
		
		return transformAE(gOperands, gOperators);
		
	}
	
	private ArithmeticExpression transformAE(List<org.emoflon.cep.grapel.AttributeExpressionOperand> gOperands, List<org.emoflon.cep.grapel.ArithmeticOperator> gOperators) {
		if(gOperands.size()<2) {
			org.emoflon.cep.grapel.AttributeExpressionOperand gOperand = gOperands.remove(0);
			ArithmeticExpressionLiteral ael = factory.createArithmeticExpressionLiteral();
			ael.setValue(transform(gOperand));
			return ael;
		}
		
		ArithmeticExpressionProduction aep = factory.createArithmeticExpressionProduction();
		aep.setOp(transform(gOperators.remove(0)));
		
		List<org.emoflon.cep.grapel.AttributeExpressionOperand> leftConstraint = new LinkedList<>();
		leftConstraint.add(gOperands.remove(0));
		aep.setLhs(transformAE(leftConstraint, new LinkedList<>()));
		
		aep.setRhs(transformAE(gOperands, gOperators));
		
		return aep;
	}
	
	private ArithmeticValue transform(org.emoflon.cep.grapel.AttributeExpressionOperand gOperand) {
		if(gOperand instanceof org.emoflon.cep.grapel.AttributeExpressionLiteral) {
			return transform((org.emoflon.cep.grapel.AttributeExpressionLiteral)gOperand);
		}else {
			return transform((org.emoflon.cep.grapel.EventPatternNodeAttributeExpression)gOperand);
		}
	}
	
	private ArithmeticValue transform(org.emoflon.cep.grapel.AttributeExpressionLiteral gLiteral) {
		if(gLiteral instanceof org.emoflon.cep.grapel.NumberLiteral) {
			if(gLiteral instanceof org.emoflon.cep.grapel.DoubleLiteral) {
				DoubleLiteral literal = factory.createDoubleLiteral();
				literal.setValue(((org.emoflon.cep.grapel.DoubleLiteral) gLiteral).getValue());
				return literal;
			}else {
				IntegerLiteral literal = factory.createIntegerLiteral();
				literal.setValue(((org.emoflon.cep.grapel.IntegerLiteral) gLiteral).getValue());
				return literal;
			}
			
		}else if(gLiteral instanceof org.emoflon.cep.grapel.StringLiteral) {
			StringLiteral literal = factory.createStringLiteral();
			literal.setValue(((org.emoflon.cep.grapel.StringLiteral) gLiteral).getValue());
			return literal;
		}else {
			BooleanLiteral literal = factory.createBooleanLiteral();
			literal.setValue(((org.emoflon.cep.grapel.BooleanLiteral)gLiteral).isValue());
			return literal;
		}
	}
	
	private ArithmeticValue transform(org.emoflon.cep.grapel.EventPatternNodeAttributeExpression gExpression) {
		ArithmeticValueExpression expression = factory.createArithmeticValueExpression();
		expression.setNodeExpression(transform(gExpression.getNodeExpression()));
		if(gExpression.getField() == null) {
			return expression;
		}
		AttributeExpressionLiteral ael = factory.createAttributeExpressionLiteral();
		expression.setAttributeExpression(ael);
		ael.setAttribute(gExpression.getField());
		ael.setClass(gExpression.getField().getEContainingClass());
		
		return expression;
	}
	
	private ArithmeticExpressionOperator transform(org.emoflon.cep.grapel.ArithmeticOperator gOperator) {
		switch(gOperator) {
		case DIVIDE:
			return ArithmeticExpressionOperator.DIVIDE;
		case MINUS:
			return ArithmeticExpressionOperator.MINUS;
		case MULTIPLY:
			return ArithmeticExpressionOperator.MULTIPLY;
		case PLUS:
			return ArithmeticExpressionOperator.PLUS;
		default:
			return null;
		
		}
	}
	
}
