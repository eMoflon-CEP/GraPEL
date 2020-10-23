package org.emoflon.cep.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EcorePackage;
import org.emoflon.cep.grapel.EditorGTFile;
import org.emoflon.ibex.gt.editor.gT.EditorNode;
import org.emoflon.ibex.gt.editor.gT.EditorParameter;
import org.emoflon.ibex.gt.editor.gT.EditorPattern;
import org.emoflon.ibex.gt.editor.utils.GTEditorPatternUtils;
import org.emoflon.ibex.gt.transformations.EditorToIBeXPatternTransformation;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXContextPattern;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXNode;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXParameter;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXRule;

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
import GrapeLModel.MatchEvent;
import GrapeLModel.MatchVanishedConstraint;
import GrapeLModel.NodeContextConstraint;
import GrapeLModel.RelationalConstraint;
import GrapeLModel.RelationalConstraintLiteral;
import GrapeLModel.RelationalConstraintOperator;
import GrapeLModel.RelationalConstraintProduction;
import GrapeLModel.RelationalConstraintUnary;
import GrapeLModel.RelationalConstraintUnaryOperator;
import GrapeLModel.ReturnStatement;
import GrapeLModel.RuleEvent;
import GrapeLModel.SimpleAttribute;
import GrapeLModel.SpawnStatement;
import GrapeLModel.StringLiteral;
import GrapeLModel.VirtualEventAttribute;
import GrapeLModel.ApplyStatement;
import GrapeLModel.ArithmeticExpression;
import GrapeLModel.ArithmeticExpressionLiteral;
import GrapeLModel.ArithmeticExpressionOperator;
import GrapeLModel.ArithmeticExpressionProduction;
import GrapeLModel.ArithmeticExpressionUnary;
import GrapeLModel.ArithmeticExpressionUnaryOperator;
import GrapeLModel.ArithmeticValue;
import GrapeLModel.ArithmeticValueExpression;
import GrapeLModel.ArithmeticValueLiteral;
import GrapeLModel.AttributeConstraint;
import GrapeLModel.AttributeConstraintExpression;
import GrapeLModel.AttributeConstraintLiteral;
import GrapeLModel.AttributeConstraintOperator;
import GrapeLModel.AttributeConstraintProduction;
import GrapeLModel.AttributeConstraintRelation;
import GrapeLModel.AttributeConstraintUnary;
import GrapeLModel.AttributeConstraintUnaryOperator;
import GrapeLModel.AttributeExpression;
import GrapeLModel.AttributeExpressionLiteral;
import GrapeLModel.AttributeExpressionProduction;
import GrapeLModel.BooleanLiteral;
import GrapeLModel.ComplexAttribute;
import GrapeLModel.Context;
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
	private Map<String, Event> events = Collections.synchronizedMap(new HashMap<>());
	private Map<Object, Object> gEventAttr2Attr = Collections.synchronizedMap(new HashMap<>());
	private Map<org.emoflon.cep.grapel.EventPatternNode, EventPatternNode> gNode2Nodes = Collections.synchronizedMap(new HashMap<>());
	private Map<Event, Map<String, EventAttribute>> nonVirtualFields = Collections.synchronizedMap(new HashMap<>());
	private Map<Event, Map<String, VirtualEventAttribute>> virtualFields = Collections.synchronizedMap(new HashMap<>());
	private Map<EventPattern, org.emoflon.cep.grapel.EventPattern> eventPattern2gEventPattern= Collections.synchronizedMap(new HashMap<>());
	
	public GrapeLModelContainer transform(EditorGTFile grapelFile) {
		container = factory.createGrapeLModelContainer();
		//transform GT to IBeXPatterns
		EditorToIBeXPatternTransformation ibexTransformer = new EditorToIBeXPatternTransformation();
		container.setIbexModel(ibexTransformer.transform(grapelFile));
		mapEditor2IBeXPatterns(grapelFile);
		
		//transform events
		container.getEvents().addAll(grapelFile.getEvents().parallelStream()
				.map(event -> transform(event))
				.collect(Collectors.toList()));
		
		//transform event patterns
		container.getEventPatterns().addAll(grapelFile.getEventPatterns().parallelStream()
				.map(pattern -> transform(pattern))
				.collect(Collectors.toList()));
		
		//scan through spawn parameters, look for and create required virtual fields
		int vFields = (int) virtualFields.values().stream().flatMap(map -> map.values().stream()).count();
		do {
			vFields = (int)virtualFields.values().stream().flatMap(map -> map.values().stream()).count();
			container.getEventPatterns().forEach(pattern -> {
				// transform return statement
				transform(eventPattern2gEventPattern.get(pattern).getReturnStatement());
			});
		}while((int)virtualFields.values().stream().flatMap(map -> map.values().stream()).count() != vFields);
		
		//do the final transformation and creation of spawn parameters
		container.getEventPatterns().forEach(pattern -> {
			// transform return statement
			pattern.setReturnStatement(transform(eventPattern2gEventPattern.get(pattern).getReturnStatement()));
		});
		
		return container;
	}
	
	private void mapEditor2IBeXPatterns(EditorGTFile grapelFile) {
		for(EditorPattern ePattern : grapelFile.getPatterns().stream()
				.filter(pattern -> !GTEditorPatternUtils.containsCreatedOrDeletedElements(pattern))
				.collect(Collectors.toList())) {
			for(IBeXContextPattern iPattern : container.getIbexModel().getPatternSet().getContextPatterns().stream()
					.filter(cp -> (cp instanceof IBeXContextPattern))
					.map(cp -> (IBeXContextPattern)cp)
					.collect(Collectors.toList())) {
				if(iPattern.getName().equals(ePattern.getName())) {
					editor2IBeXPatterns.put(ePattern, iPattern);
					
					MatchEvent matchEvent = factory.createMatchEvent();
					container.getEvents().add(matchEvent);
					matchEvent.setName(iPattern.getName());
					events.put(matchEvent.getName(), matchEvent);
					matchEvent.setRelatedPattern(iPattern);
					
					Map<String, EventAttribute> localNonVFields = Collections.synchronizedMap(new LinkedHashMap<>());
					nonVirtualFields.put(matchEvent, localNonVFields);
					
					for(EditorNode eNode : ePattern.getNodes()) {
						for(IBeXNode iNode : iPattern.getSignatureNodes()) {
							if(eNode.getName().equals(iNode.getName())) {
								gEventAttr2Attr.put(eNode, iNode);
								
								ComplexAttribute cAtr = factory.createComplexAttribute();
								cAtr.setName(iNode.getName());
								cAtr.setType(iNode.getType());
								matchEvent.getAttributes().add(cAtr);
								matchEvent.getEventAttribute2patternNode().put(cAtr, iNode);
								localNonVFields.put(cAtr.getName(), cAtr);
							}
						}
					} 
				}
			}
		}
		
		for(EditorPattern ePattern : grapelFile.getPatterns().stream()
				.filter(pattern -> GTEditorPatternUtils.containsCreatedOrDeletedElements(pattern))
				.collect(Collectors.toList())) {
			for(IBeXRule iRule : container.getIbexModel().getRuleSet().getRules()) {
				if(iRule.getName().equals(ePattern.getName())) {
					editor2IBeXPatterns.put(ePattern, (IBeXContextPattern)iRule.getLhs());
					
					RuleEvent ruleEvent = factory.createRuleEvent();
					container.getEvents().add(ruleEvent);
					ruleEvent.setName(iRule.getName());
					events.put(ruleEvent.getName(), ruleEvent);
					ruleEvent.setRelatedRule(iRule);
					ruleEvent.setRelatedPattern((IBeXContextPattern)iRule.getLhs());
					
					Map<String, EventAttribute> localNonVFields = Collections.synchronizedMap(new LinkedHashMap<>());
					nonVirtualFields.put(ruleEvent, localNonVFields);
					
					for(EditorNode eNode : ePattern.getNodes()) {
						for(IBeXNode iNode : ((IBeXContextPattern)iRule.getLhs()).getSignatureNodes()) {
							if(eNode.getName().equals(iNode.getName())) {
								gEventAttr2Attr.put(eNode, iNode);
								
								ComplexAttribute cAtr = factory.createComplexAttribute();
								cAtr.setName(iNode.getName());
								cAtr.setType(iNode.getType());
								ruleEvent.getAttributes().add(cAtr);
								ruleEvent.getEventAttribute2patternNode().put(cAtr, iNode);
								localNonVFields.put(cAtr.getName(), cAtr);
							}
						}
					}
					
					for(EditorParameter eParam : ePattern.getParameters()) {
						for(IBeXParameter iParam : iRule.getParameters()) {
							if(eParam.getName().equals(iParam.getName())) {
								SimpleAttribute sAtr = factory.createSimpleAttribute();
								sAtr.setName(iParam.getName());
								sAtr.setType(iParam.getType());
								ruleEvent.getParameterAttributes().add(sAtr);
								ruleEvent.getEventAttribute2patternParameter().put(sAtr, iParam);
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
		events.put(event.getName(), event);
		
		Map<String, EventAttribute> localNonVFields = Collections.synchronizedMap(new LinkedHashMap<>());
		nonVirtualFields.put(event, localNonVFields);
		event.getAttributes().forEach(atr -> {
			localNonVFields.put(atr.getName(), atr);
		});
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
		if(!gEventPattern.getContextConstraints().isEmpty()) {
			Context context = factory.createContext();
			context.getContextConstraints().addAll(gEventPattern.getContextConstraints().parallelStream()
					.map(contextConstraints -> transform(contextConstraints))
					.collect(Collectors.toList()));
			context.getParams().addAll(getContextConstraintParams(context));
			pattern.setContext(context);
		}
		
		
		// transform relational constraints		
		RelationalConstraint relConstraint = transform(gEventPattern.getRelationalConstraint());
		pattern.setRelationalConstraint(relConstraint);
		
		// transform attribute constraints	
		if(gEventPattern.getAttributeConstraint()!=null) {
			AttributeConstraint atrConstraint = transform(gEventPattern.getAttributeConstraint());
			pattern.setAttributeConstraint(atrConstraint);
			atrConstraint.getParams().addAll(getAttributeConstraintParams(atrConstraint));
		}
		
		eventPattern2gEventPattern.put(pattern, gEventPattern);
		
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
	
	private Set<EventPatternNode> getContextConstraintParams(Context context) {
		Set<EventPatternNode> params = new LinkedHashSet<>();
		context.getContextConstraints().stream().filter(cc -> (cc instanceof NodeContextConstraint)).map(cc -> (NodeContextConstraint)cc).forEach(cc -> {
			params.add(cc.getLhs().getEventPatternNode());
			params.add(cc.getRhs().getEventPatternNode());
		});
		
		return params;
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
	
	private RelationalConstraint transform(org.emoflon.cep.grapel.RelationalConstraint gConstraint) {
		if(gConstraint instanceof org.emoflon.cep.grapel.RelationalNodeExpression) {
			RelationalConstraintLiteral literal = factory.createRelationalConstraintLiteral();
			literal.setEventPatternNode(gNode2Nodes.get(((org.emoflon.cep.grapel.RelationalNodeExpression)gConstraint).getNode()));
			return literal;
		} else if (gConstraint instanceof org.emoflon.cep.grapel.UnaryRelationalConstraint) {
			RelationalConstraintUnary constraint = factory.createRelationalConstraintUnary();
			org.emoflon.cep.grapel.UnaryRelationalConstraint gUnaryConstraint = (org.emoflon.cep.grapel.UnaryRelationalConstraint)gConstraint;
			if(gUnaryConstraint.getOperator() == org.emoflon.cep.grapel.UnaryRelationalOperator.NONE) {
				constraint.setOperator(RelationalConstraintUnaryOperator.BRACKET);
				constraint.setOperand(transform(gUnaryConstraint.getOperand()));
			} else {
				constraint.setOperator(RelationalConstraintUnaryOperator.ALL);
				constraint.setOperand(transform(gUnaryConstraint.getOperand()));
			}
			
			if(gUnaryConstraint.isNegated()) {
				RelationalConstraintUnary negate = factory.createRelationalConstraintUnary();
				negate.setOperator(RelationalConstraintUnaryOperator.NOT);
				negate.setOperand(constraint);
				return negate;
			} else {
				return constraint;
			}
		} else {
			org.emoflon.cep.grapel.BinaryRelationalConstraint gBinaryConstraint = (org.emoflon.cep.grapel.BinaryRelationalConstraint)gConstraint;
			RelationalConstraintProduction constraint = factory.createRelationalConstraintProduction();
			constraint.setLhs(transform(gBinaryConstraint.getLeft()));
			constraint.setRhs(transform(gBinaryConstraint.getRight()));
			constraint.setOp(transform(gBinaryConstraint.getOperator()));
			return constraint;
		}
	}
	
	private RelationalConstraintOperator transform(org.emoflon.cep.grapel.BinaryRelationalOperator op) {
		switch(op) {
			case AND: return RelationalConstraintOperator.AND;
			case FOLLOWS: return RelationalConstraintOperator.FOLLOWS;
			case OR: return RelationalConstraintOperator.OR;
			default: return null;
		}
	}
	
	
	private AttributeConstraint transform(org.emoflon.cep.grapel.AttributeConstraint gConstraint) {
		if(gConstraint instanceof org.emoflon.cep.grapel.BinaryAttributeConstraint) {
			return transform((org.emoflon.cep.grapel.BinaryAttributeConstraint)gConstraint);
		} else {
			return transform((org.emoflon.cep.grapel.UnaryAttributeConstraint)gConstraint);
		}
	}
	
	private void checkACCast(ArithmeticExpression lhs, ArithmeticExpression rhs) {
		if(rhs == null) {
			lhs.setRequiresCast(false);
			return;
		}
		
		if(lhs.getType() == rhs.getType()) {
			lhs.setRequiresCast(false);
			rhs.setRequiresCast(false);
			return;
		}
		
		if(lhs.getType() == EcorePackage.Literals.ELONG && rhs.getType() == EcorePackage.Literals.EINT) {
			lhs.setRequiresCast(false);
			rhs.setRequiresCast(false);
			return;
		}
		
		if(rhs.getType() == EcorePackage.Literals.ELONG && lhs.getType() == EcorePackage.Literals.EINT) {
			lhs.setRequiresCast(false);
			rhs.setRequiresCast(false);
			return;
		}
		
		if(lhs.getType() == EcorePackage.Literals.EDOUBLE) {
			rhs.setRequiresCast(true);
			rhs.setCastTo(EcorePackage.Literals.EDOUBLE);
			return;
		}
		
		if(rhs.getType() == EcorePackage.Literals.EDOUBLE) {
			lhs.setRequiresCast(true);
			lhs.setCastTo(EcorePackage.Literals.EDOUBLE);
		}
	}
	
	private Set<EventPatternNode> getAttributeConstraintParams(AttributeConstraint root) {
		if(root instanceof AttributeConstraintLiteral) {
			AttributeConstraintLiteral literal = (AttributeConstraintLiteral)root;
			AttributeConstraintExpression expr = literal.getConstraintExpression();
			
			Set<EventPatternNode> params = new LinkedHashSet<>();
			params.addAll(getArithmeticExpressionParams(expr.getLhs()));
			if(expr.getRhs() != null)
				params.addAll(getArithmeticExpressionParams(expr.getRhs()));
			
			return params;
		} else if(root instanceof MatchVanishedConstraint) {
			Set<EventPatternNode> params = new LinkedHashSet<>();
			params.add(((MatchVanishedConstraint)root).getEventPatternNode());
			return params;
		} else if(root instanceof AttributeConstraintUnary) {
			AttributeConstraintUnary acu = (AttributeConstraintUnary)root;
			return getAttributeConstraintParams(acu.getOperand());
		}
		
		AttributeConstraintProduction production = (AttributeConstraintProduction)root;
		Set<EventPatternNode> params = new LinkedHashSet<>();
		params.addAll(getAttributeConstraintParams(production.getLhs()));
		params.addAll(getAttributeConstraintParams(production.getRhs()));
		
		return params;
	}
	
	private AttributeConstraint transform(org.emoflon.cep.grapel.BinaryAttributeConstraint gConstraint) {
		AttributeConstraintProduction acp = factory.createAttributeConstraintProduction();
		acp.setLhs(transform(gConstraint.getLeft()));
		acp.setOp(transform(gConstraint.getOperator()));
		acp.setRhs(transform(gConstraint.getRight()));
		return acp;
	}
	
	private AttributeConstraint transform(org.emoflon.cep.grapel.UnaryAttributeConstraint gConstraint) {
		AttributeConstraintUnary acu = factory.createAttributeConstraintUnary();
		
		if(gConstraint.isNegated() && gConstraint.getOperand() instanceof org.emoflon.cep.grapel.AttributeConstraint) {
			acu.setOperator(AttributeConstraintUnaryOperator.NOT);
			AttributeConstraintUnary acuBracket = factory.createAttributeConstraintUnary();
			acu.setOperand(acuBracket);
			acuBracket.setOperator(AttributeConstraintUnaryOperator.BRACKET);
			acuBracket.setOperand(transform((org.emoflon.cep.grapel.AttributeConstraint)gConstraint.getOperand()));
		} else if(!gConstraint.isNegated() && gConstraint.getOperand() instanceof org.emoflon.cep.grapel.AttributeConstraint) {
			acu.setOperator(AttributeConstraintUnaryOperator.BRACKET);
			acu.setOperand(transform((org.emoflon.cep.grapel.AttributeConstraint)gConstraint.getOperand()));
		} else {
			if(gConstraint.isNegated()) {
				acu.setOperator(AttributeConstraintUnaryOperator.NOT);
				if(gConstraint.getOperand() instanceof org.emoflon.cep.grapel.AttributeRelation) {
					AttributeConstraintLiteral acl = factory.createAttributeConstraintLiteral();
					acu.setOperand(acl);
					AttributeConstraintExpression ace = factory.createAttributeConstraintExpression();
					acl.setConstraintExpression(ace);
					
					org.emoflon.cep.grapel.AttributeRelation relation = (org.emoflon.cep.grapel.AttributeRelation) gConstraint.getOperand();
					ace.setLhs(transform(relation.getLhs()));
					if(relation.getRhs() != null) {
						ace.setOp(transform(relation.getRelation()));
						ace.setRhs(transform(relation.getRhs()));
					}
					checkACCast(ace.getLhs(), ace.getRhs());
				} else {
					MatchVanishedConstraint mvc = factory.createMatchVanishedConstraint();
					acu.setOperand(mvc);
					
					org.emoflon.cep.grapel.MatchEventState state = (org.emoflon.cep.grapel.MatchEventState) gConstraint.getOperand();
					mvc.setEventPatternNode(gNode2Nodes.get(state.getEvent()));
				}
			} else {
				if(gConstraint.getOperand() instanceof org.emoflon.cep.grapel.AttributeRelation) {
					AttributeConstraintLiteral acl = factory.createAttributeConstraintLiteral();
					AttributeConstraintExpression ace = factory.createAttributeConstraintExpression();
					acl.setConstraintExpression(ace);
					
					org.emoflon.cep.grapel.AttributeRelation relation = (org.emoflon.cep.grapel.AttributeRelation) gConstraint.getOperand();
					ace.setLhs(transform(relation.getLhs()));
					if(relation.getRhs() != null) {
						ace.setOp(transform(relation.getRelation()));
						ace.setRhs(transform(relation.getRhs()));
					}
					checkACCast(ace.getLhs(), ace.getRhs());
					return acl;
				} else {
					MatchVanishedConstraint mvc = factory.createMatchVanishedConstraint();
					org.emoflon.cep.grapel.MatchEventState state = (org.emoflon.cep.grapel.MatchEventState) gConstraint.getOperand();
					mvc.setEventPatternNode(gNode2Nodes.get(state.getEvent()));
					return mvc;
				}
			}
			
		}
		
		return acu;
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
	
	private AttributeConstraintRelation transform(org.emoflon.cep.grapel.AttributeRelationOperator gRelation) {
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
		if(gExpression instanceof org.emoflon.cep.grapel.BinaryAttributeExpression)
			return transform((org.emoflon.cep.grapel.BinaryAttributeExpression)gExpression);
		else if(gExpression instanceof org.emoflon.cep.grapel.UnaryAttributeExpression)
			return transform((org.emoflon.cep.grapel.UnaryAttributeExpression)gExpression);
		else {
			ArithmeticExpressionLiteral ael = factory.createArithmeticExpressionLiteral();
			ael.setValue(transform((org.emoflon.cep.grapel.AttributeExpressionOperand)gExpression));
			ael.setType(ael.getValue().getType());
			return ael;
		}
	}
	
	private Set<EventPatternNode> getArithmeticExpressionParams(ArithmeticExpression root) {
		if(root instanceof ArithmeticExpressionLiteral) {
			ArithmeticExpressionLiteral literal = (ArithmeticExpressionLiteral)root;
			if(literal.getValue() instanceof ArithmeticValueLiteral)
				return new LinkedHashSet<>();
			
			ArithmeticValueExpression expr = (ArithmeticValueExpression) literal.getValue();
			Set<EventPatternNode> params = new LinkedHashSet<>();
			params.add(expr.getNodeExpression().getEventPatternNode());
			return params;
		}
		
		Set<EventPatternNode> params = new LinkedHashSet<>();
		if(root instanceof ArithmeticExpressionProduction) {
			ArithmeticExpressionProduction production = (ArithmeticExpressionProduction)root;
			params.addAll(getArithmeticExpressionParams(production.getLhs()));
			params.addAll(getArithmeticExpressionParams(production.getRhs()));
		} else {
			ArithmeticExpressionUnary unary = (ArithmeticExpressionUnary)root;
			params.addAll(getArithmeticExpressionParams(unary.getOperand()));
		}
		
		return params;
	}
	
	private ArithmeticExpression transform(org.emoflon.cep.grapel.BinaryAttributeExpression gExpression) {
		ArithmeticExpressionProduction aep = factory.createArithmeticExpressionProduction();
		aep.setOp(transform(gExpression.getOperator()));
		
		if(gExpression.getLeft() instanceof org.emoflon.cep.grapel.BinaryAttributeExpression)
			aep.setLhs(transform((org.emoflon.cep.grapel.BinaryAttributeExpression) gExpression.getLeft()));
		else if(gExpression.getLeft() instanceof org.emoflon.cep.grapel.UnaryAttributeExpression)
			aep.setLhs(transform((org.emoflon.cep.grapel.UnaryAttributeExpression) gExpression.getLeft()));
		else {
			ArithmeticExpressionLiteral ael = factory.createArithmeticExpressionLiteral();
			ael.setValue(transform((org.emoflon.cep.grapel.AttributeExpressionOperand)gExpression.getLeft()));
			ael.setType(ael.getValue().getType());
			aep.setLhs(ael);
		}
		
		if(gExpression.getRight() instanceof org.emoflon.cep.grapel.BinaryAttributeExpression)
			aep.setRhs(transform((org.emoflon.cep.grapel.BinaryAttributeExpression) gExpression.getRight()));
		else if(gExpression.getRight() instanceof org.emoflon.cep.grapel.UnaryAttributeExpression)
			aep.setRhs(transform((org.emoflon.cep.grapel.UnaryAttributeExpression) gExpression.getRight()));
		else {
			ArithmeticExpressionLiteral ael = factory.createArithmeticExpressionLiteral();
			ael.setValue(transform((org.emoflon.cep.grapel.AttributeExpressionOperand)gExpression.getRight()));
			ael.setType(ael.getValue().getType());
			aep.setRhs(ael);
		}
		
		setBinaryTypeAndCast(aep, aep.getLhs(), aep.getRhs());
		return aep;
	}
	
	private ArithmeticExpression transform(org.emoflon.cep.grapel.UnaryAttributeExpression gExpression) {
		if(gExpression instanceof org.emoflon.cep.grapel.AttributeExpressionOperand) {
			ArithmeticExpressionLiteral ael = factory.createArithmeticExpressionLiteral();
			ael.setValue(transform((org.emoflon.cep.grapel.AttributeExpressionOperand)gExpression));
			ael.setType(ael.getValue().getType());
			return ael;
		}
		
		ArithmeticExpressionUnary aeu = factory.createArithmeticExpressionUnary();
		aeu.setIsNegative(gExpression.isNegative());
		if(gExpression.getOperator() != org.emoflon.cep.grapel.UnaryOperator.NONE) {
			aeu.setOperator(transform(gExpression.getOperator()));
			aeu.setOperand(transform(gExpression.getOperand()));
		} else {
			aeu.setOperator(ArithmeticExpressionUnaryOperator.BRACKETS);
			aeu.setOperand(transform(gExpression.getOperand()));
		}
		
		setUnaryTypeAndCast(aeu, aeu.getOperand());
		return aeu;
	}
	
	private void setBinaryTypeAndCast(ArithmeticExpressionProduction production, ArithmeticExpression lhs, ArithmeticExpression rhs) {
		if(lhs.getType() == rhs.getType()) {
			production.setType(lhs.getType());
			lhs.setRequiresCast(false);
			rhs.setRequiresCast(false);
			return;
		}
		
		if(lhs.getType() == EcorePackage.Literals.ELONG && rhs.getType() == EcorePackage.Literals.EINT) {
			production.setType(rhs.getType());
			lhs.setRequiresCast(false);
			rhs.setRequiresCast(false);
			return;
		}
		
		if(rhs.getType() == EcorePackage.Literals.ELONG && lhs.getType() == EcorePackage.Literals.EINT) {
			production.setType(lhs.getType());
			lhs.setRequiresCast(false);
			rhs.setRequiresCast(false);
			return;
		}
		
		if(lhs.getType() == EcorePackage.Literals.EDOUBLE && rhs.getType() != EcorePackage.Literals.ESTRING) {
			production.setType(lhs.getType());
			rhs.setRequiresCast(true);
			rhs.setCastTo(EcorePackage.Literals.EDOUBLE);
			return;
		} else if(lhs.getType() == EcorePackage.Literals.ESTRING) {
			production.setType(lhs.getType());
			rhs.setRequiresCast(true);
			rhs.setCastTo(EcorePackage.Literals.ESTRING);
			return;
		}
		
		if(rhs.getType() == EcorePackage.Literals.EDOUBLE && lhs.getType() != EcorePackage.Literals.ESTRING) {
			production.setType(rhs.getType());
			lhs.setRequiresCast(true);
			lhs.setCastTo(EcorePackage.Literals.EDOUBLE);
		} else if(rhs.getType() == EcorePackage.Literals.ESTRING) {
			production.setType(rhs.getType());
			lhs.setRequiresCast(true);
			lhs.setCastTo(EcorePackage.Literals.ESTRING);
		}
	}
	
	private void setUnaryTypeAndCast(ArithmeticExpressionUnary production, ArithmeticExpression operand) {
		production.setType(operand.getType());
		operand.setRequiresCast(false);
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
				literal.setType(EcorePackage.Literals.EDOUBLE);
				return literal;
			}else {
				IntegerLiteral literal = factory.createIntegerLiteral();
				literal.setValue(((org.emoflon.cep.grapel.IntegerLiteral) gLiteral).getValue());
				literal.setType(EcorePackage.Literals.EINT);
				return literal;
			}
			
		}else if(gLiteral instanceof org.emoflon.cep.grapel.StringLiteral) {
			StringLiteral literal = factory.createStringLiteral();
			literal.setValue(((org.emoflon.cep.grapel.StringLiteral) gLiteral).getValue());
			literal.setType(EcorePackage.Literals.ESTRING);
			return literal;
		}else {
			BooleanLiteral literal = factory.createBooleanLiteral();
			literal.setValue(((org.emoflon.cep.grapel.BooleanLiteral)gLiteral).isValue());
			literal.setType(EcorePackage.Literals.EBOOLEAN);
			return literal;
		}
	}
	
	private ArithmeticValue transform(org.emoflon.cep.grapel.EventPatternNodeAttributeExpression gExpression) {
		ArithmeticValueExpression expression = factory.createArithmeticValueExpression();
		expression.setNodeExpression(transform(gExpression.getNodeExpression()));
		//Set type if simple:
		if(expression.getNodeExpression() instanceof EventNodeExpression) {
			EventNodeExpression ene = (EventNodeExpression)expression.getNodeExpression();
			if(ene.getEventAttribute() instanceof SimpleAttribute) {
				SimpleAttribute sa = (SimpleAttribute)ene.getEventAttribute();
				expression.setType(sa.getType());
			}
		}
		
		if(gExpression.getField() == null) {
			return expression;
		}
		AttributeExpressionLiteral ael = factory.createAttributeExpressionLiteral();
		expression.setAttributeExpression(ael);
		expression.setType(gExpression.getField().getEAttributeType());
		ael.setAttribute(gExpression.getField());
		ael.setClass(gExpression.getField().getEContainingClass());
		createVirtualField(ael, expression.getNodeExpression());
		
		return expression;
	}
	
	private void createVirtualField(AttributeExpression expr, EventPatternNodeExpression root) {
		if(expr instanceof AttributeExpressionProduction)
			throw new RuntimeException("Nested attribute expressions not yet supported!");
		
		AttributeExpressionLiteral ael = (AttributeExpressionLiteral) expr;
		EventPatternNode epn = root.getEventPatternNode();
		String eventName = "";
		if(epn instanceof EventNode) {
			eventName = ((EventNode)epn).getType().getName();
		}else {
			eventName = ((IBeXPatternNode)epn).getType().getName();
		}
		
		Event event = events.get(eventName);
		Map<String, EventAttribute> localNonVFields = nonVirtualFields.get(event);
		Map<String, VirtualEventAttribute> localVirtualFields = virtualFields.get(event);
		if(localVirtualFields == null) {
			localVirtualFields = Collections.synchronizedMap(new LinkedHashMap<>());
			virtualFields.put(event, localVirtualFields);
		}
		String vFieldName = toCamelCase(expr, root);
		VirtualEventAttribute vea = localVirtualFields.get(vFieldName);
		if(vea == null) {
			vea = factory.createVirtualEventAttribute();
			vea.setName(vFieldName);
			vea.setType(ael.getAttribute().getEAttributeType());
			event.getAttributes().add(vea);
			localVirtualFields.put(vFieldName, vea);
			String baseAtrName = "";
			if(root instanceof EventNodeExpression) {
				EventAttribute eAtr = ((EventNodeExpression)root).getEventAttribute();
				if(eAtr != null)
					baseAtrName = eAtr.getName();
			} else {
				IBeXNode node = ((IBeXPatternNodeExpression)root).getPatternAttribute();
				if(node != null)
					baseAtrName = node.getName();
			}
			vea.setBaseAttribute((ComplexAttribute) localNonVFields.get(baseAtrName));
			vea.setAttribute(ael.getAttribute());
		}
		ael.setVirtualAttribute(vea);
	}
	
	private String toCamelCase(AttributeExpression expr, EventPatternNodeExpression root) {
		if(expr instanceof AttributeExpressionProduction)
			throw new RuntimeException("Nested attribute expressions not yet supported!");
		
		AttributeExpressionLiteral ael = (AttributeExpressionLiteral) expr;
		StringBuilder sb = new StringBuilder();
		EventPatternNode epn = root.getEventPatternNode();
		String eventName = "";
		if(epn instanceof EventNode) {
			eventName = ((EventNode)epn).getType().getName();
		}else {
			eventName = ((IBeXPatternNode)epn).getType().getName();
		}
		sb.append(eventName);
		
		if(root instanceof EventNodeExpression) {
			EventAttribute eAtr = ((EventNodeExpression)root).getEventAttribute();
			if(eAtr != null)
				sb.append(eAtr.getName());
		} else {
			IBeXNode node = ((IBeXPatternNodeExpression)root).getPatternAttribute();
			if(node != null)
				sb.append(node.getName());
		}
		sb.append(ael.getAttribute().getName());
		return sb.toString();
	}
	
	private ArithmeticExpressionOperator transform(org.emoflon.cep.grapel.ArithmeticOperator gOperator) {
		switch(gOperator) {
		case DIVIDE:
			return ArithmeticExpressionOperator.DIVIDE;
		case MINUS:
			return ArithmeticExpressionOperator.SUBTRACT;
		case MULTIPLY:
			return ArithmeticExpressionOperator.MULTIPLY;
		case PLUS:
			return ArithmeticExpressionOperator.ADD;
		case POW:
			return ArithmeticExpressionOperator.EXPONENTIATE;
		default:
			return null;
		}
	}
	
	private ArithmeticExpressionUnaryOperator transform(org.emoflon.cep.grapel.UnaryOperator gOperator) {
		switch(gOperator) {
		case ABS:
			return ArithmeticExpressionUnaryOperator.ABS;
		case COS:
			return ArithmeticExpressionUnaryOperator.COS;
		case SIN:
			return ArithmeticExpressionUnaryOperator.SIN;
		case SQRT:
			return ArithmeticExpressionUnaryOperator.SQRT;
		default:
			return null;
		}
	}
	
	private ReturnStatement transform(org.emoflon.cep.grapel.ReturnStatement gReturn) {
		ReturnStatement returnState = null;
		if(gReturn instanceof org.emoflon.cep.grapel.SpawnStatement) {
			org.emoflon.cep.grapel.SpawnStatement spawn = (org.emoflon.cep.grapel.SpawnStatement)gReturn;
			returnState = factory.createSpawnStatement();
			((SpawnStatement)returnState).setReturnType((gEvent2Events.get(spawn.getReturnArg())));
		} else {
			org.emoflon.cep.grapel.ApplyStatement apply = (org.emoflon.cep.grapel.ApplyStatement)gReturn;
			returnState = factory.createApplyStatement();
			((ApplyStatement)returnState).setReturnType((RuleEvent)(events.get(apply.getReturnArg().getName())));
		}
		
		if(gReturn instanceof org.emoflon.cep.grapel.SpawnStatement) {
			Event returnType = ((SpawnStatement)returnState).getReturnType();
			Map<EventAttribute, Integer> baseExpressionIdx = new HashMap<>();
			
			for(int i = 0; i<returnType.getAttributes().size(); i++) {
				EventAttribute ea = returnType.getAttributes().get(i);
				
				if(!(ea instanceof VirtualEventAttribute)) {
					org.emoflon.cep.grapel.AttributeExpression gae = gReturn.getReturnParams().get(i);
					ArithmeticExpression ae = transform(gae);
					returnState.getParameters().add(ae);
					baseExpressionIdx.put(ea, i);
				} else {
					VirtualEventAttribute vea = (VirtualEventAttribute)ea;
					ArithmeticExpressionLiteral baseExpression = (ArithmeticExpressionLiteral)returnState.getParameters().get(baseExpressionIdx.get(vea.getBaseAttribute()));
					ArithmeticValueExpression baseValueExpression = (ArithmeticValueExpression)baseExpression.getValue();
					EventPatternNodeExpression baseEpne = baseValueExpression.getNodeExpression();
					
					ArithmeticExpressionLiteral virtualExpression = factory.createArithmeticExpressionLiteral();
					ArithmeticValueExpression virtualValueExpression = factory.createArithmeticValueExpression();
					virtualExpression.setValue(virtualValueExpression);
					
					if(baseEpne instanceof EventNodeExpression) {
						EventNodeExpression ene = (EventNodeExpression)baseEpne;
						EventPatternNode baseNode = ene.getEventPatternNode();
						EventAttribute baseAttribute = ene.getEventAttribute();
						
						EventNodeExpression virtualEne = factory.createEventNodeExpression();
						virtualValueExpression.setNodeExpression(virtualEne);
						virtualEne.setEventPatternNode(baseNode);
						virtualEne.setEventAttribute(baseAttribute);
					} else {
						IBeXPatternNodeExpression ibexPne = (IBeXPatternNodeExpression)baseEpne;
						EventPatternNode baseNode = ibexPne.getEventPatternNode();
						IBeXNode baseAttribute = ibexPne.getPatternAttribute();
						
						IBeXPatternNodeExpression virtualPne = factory.createIBeXPatternNodeExpression();
						virtualValueExpression.setNodeExpression(virtualPne);
						virtualPne.setEventPatternNode(baseNode);
						virtualPne.setPatternAttribute(baseAttribute);
					}
					
					AttributeExpressionLiteral virtualAel = factory.createAttributeExpressionLiteral();
					virtualValueExpression.setAttributeExpression(virtualAel);

					virtualValueExpression.setType(vea.getAttribute().getEAttributeType());
					virtualExpression.setType(vea.getAttribute().getEAttributeType());
					virtualValueExpression.setType(vea.getAttribute().getEAttributeType());
					virtualAel.setType(vea.getAttribute().getEAttributeType());
					virtualAel.setAttribute(vea.getAttribute());
					virtualAel.setClass(vea.getAttribute().getEContainingClass());
					
					String vFieldName = toCamelCase(virtualAel, baseEpne);
					EventPatternNode epn = baseEpne.getEventPatternNode();
					String eventName = "";
					if(epn instanceof EventNode) {
						eventName = ((EventNode)epn).getType().getName();
					}else {
						eventName = ((IBeXPatternNode)epn).getType().getName();
					}
					Map<String, VirtualEventAttribute> vFields = virtualFields.get(events.get(eventName));
					if(vFields == null || !vFields.containsKey(vFieldName))
						createVirtualField(virtualAel, baseEpne);
					else {
						virtualAel.setVirtualAttribute(vFields.get(vFieldName));
					}
					
					returnState.getParameters().add(virtualExpression);
				}
			}
			
			for(int i = 0; i<returnState.getParameters().size(); i++) {
				ArithmeticExpression ae = returnState.getParameters().get(i);
				EventAttribute ea = ((SpawnStatement)returnState).getReturnType().getAttributes().get(i);
				EDataType eaDataType = null;
				if(ea instanceof SimpleAttribute) {
					eaDataType = ((SimpleAttribute)ea).getType();
				} else if(ea instanceof VirtualEventAttribute) {
					eaDataType = ((VirtualEventAttribute)ea).getType();
				} else {
					continue;
				}
				
				if(eaDataType != ae.getType()) {
					if(eaDataType == EcorePackage.Literals.EINT && ae.getType() == EcorePackage.Literals.ELONG) {
						ae.setRequiresCast(false);
					} else if(eaDataType == EcorePackage.Literals.ELONG && ae.getType() == EcorePackage.Literals.EINT) {
						ae.setRequiresCast(false);
					} else {
						ae.setRequiresCast(true);
						ae.setCastTo(eaDataType);
					}

				}
			}
		} else {
			ApplyStatement applyState = (ApplyStatement)returnState;
			RuleEvent returnType = applyState.getReturnType();
			
			// calculate return rule parameters
			for(int i = 0; i<returnType.getAttributes().size(); i++) {
				org.emoflon.cep.grapel.AttributeExpression gae = gReturn.getReturnParams().get(i);
				ArithmeticExpression ae = transform(gae);
				returnState.getParameters().add(ae);
			}
			
			for(int i = 0; i<returnState.getParameters().size(); i++) {
				ArithmeticExpression ae = returnState.getParameters().get(i);
				EventAttribute ea = applyState.getReturnType().getAttributes().get(i);
				EDataType eaDataType = null;
				if(ea instanceof SimpleAttribute) {
					eaDataType = ((SimpleAttribute)ea).getType();
				} else if(ea instanceof VirtualEventAttribute) {
					eaDataType = ((VirtualEventAttribute)ea).getType();
				} else {
					continue;
				}
				
				if(eaDataType != ae.getType()) {
					if(eaDataType == EcorePackage.Literals.EINT && ae.getType() == EcorePackage.Literals.ELONG) {
						ae.setRequiresCast(false);
					} else if(eaDataType == EcorePackage.Literals.ELONG && ae.getType() == EcorePackage.Literals.EINT) {
						ae.setRequiresCast(false);
					} else {
						ae.setRequiresCast(true);
						ae.setCastTo(eaDataType);
					}

				}
			}
			
			org.emoflon.cep.grapel.ApplyStatement gApplyStatement = (org.emoflon.cep.grapel.ApplyStatement)gReturn;
			IBeXPatternNode matchNode = (IBeXPatternNode)gNode2Nodes.get(gApplyStatement.getMatch());
			applyState.setMatchEvent((IBeXPatternNode)matchNode);
			//TODO match event attributes
			for(IBeXNode eventAttribute : matchNode.getType().getSignatureNodes()) {
				
				ArithmeticExpressionLiteral literal = factory.createArithmeticExpressionLiteral();
				ArithmeticValueExpression valExpr = factory.createArithmeticValueExpression();
				literal.setValue(valExpr);
				
				IBeXPatternNodeExpression expr = factory.createIBeXPatternNodeExpression();
				valExpr.setNodeExpression(expr);
				expr.setEventPatternNode(matchNode);
				expr.setPatternAttribute(eventAttribute);
				returnState.getParameters().add(literal);
			}
			
		}
		
		
		return returnState;
	}
	
}
