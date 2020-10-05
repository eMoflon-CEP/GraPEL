/*
 * generated by Xtext 2.20.0
 */
package org.emoflon.cep.validation;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.validation.Check;
import org.emoflon.cep.grapel.ArithmeticOperator;
import org.emoflon.cep.grapel.AttributeExpression;
import org.emoflon.cep.grapel.AttributeExpressionLiteral;
import org.emoflon.cep.grapel.BinaryAttributeExpression;
import org.emoflon.cep.grapel.DoubleLiteral;
import org.emoflon.cep.grapel.EditorGTFile;
import org.emoflon.cep.grapel.Event;
import org.emoflon.cep.grapel.EventAttribute;
import org.emoflon.cep.grapel.EventPattern;
import org.emoflon.cep.grapel.EventPatternNode;
import org.emoflon.cep.grapel.EventPatternNodeAttributeExpression;
import org.emoflon.cep.grapel.EventPatternNodeExpression;
import org.emoflon.cep.grapel.GrapelPackage;
import org.emoflon.cep.grapel.IntegerLiteral;
import org.emoflon.cep.grapel.ReturnStatement;
import org.emoflon.cep.grapel.StringLiteral;
import org.emoflon.cep.grapel.UnaryAttributeExpression;
import org.emoflon.cep.grapel.UnaryOperator;
import org.emoflon.ibex.gt.editor.gT.EditorNode;

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class GrapelValidator extends AbstractGrapelValidator {
	
	// blacklisted names not liked by Apama or Java
	public static final Set<String> keywordBlacklist = Set.of("abstract", "action", "aggregate", "all", "and", "as", "assert", "at", "between", "boolean", "bounded", 
			"break", "by", "byte", "case", "catch", "char", "chunk", "class", "completed", "const", "constant", "context", "continue", "currentTime", "decimal", "day", 
			"days", "default", "dictionary", "die", "do", "double", "EAttribute", "EBoolean", "EDataType", "EClass", "EClassifier", "EDouble", "EFloat", "EInt", "else", 
			"emit", "enqueue", "enum", "EPackage", "EReference", "EString", "event", "every", "export", "extends", "false", "final", "finally",
			"find", "float", "for", "form", "goto", "group", "having", "hour", "hours", "if", "implements", "import", "in", "inputs", "instanceof", "int", "integer", "interface",
			"join", "key", "largest", "location", "log", "long", "millisecond", "milliseconds", "module", "msec", "minute", "minutes", "min", "monitor", "native", "new", "not", "null", 
			"on", "onBeginRecovery", "onConcludeRecovery", "ondie", "onload", "onunload", "optional", "or", "package", "parameters", "partition", "persistent", "print", "private",
			"protected", "public", "query", "requires", "retain", "return", "returns", "route", "rstream", "second", "seconds", "sec", "select", "send", "sequence", "short",
			"smallest", "spawn", "static", "stream", "streamsource", "strictfp", "string", "super", "switch", "synchronized", "then", "this", "throw", "throws", "to", "transient",
			"true", "try", "unbounded", "unique", "unmatched", "using", "var", "void", "volatile", "wait", "where", "while", "wildcard", "with", "within", "without", "xor", "#");

	public static final String CODE_PREFIX = "org.emoflon.cep.";
	
	// Errors for events
	public static final String EVENT_NAME_FORBIDDEN_MESSAGE = "Event cannot be named '%s'. Use a different name.";
	public static final String EVENT_NAME_MULTIPLE_DECLARATIONS_MESSAGE = "Event '%s' must not be declared multiple times.";
	
	// Errors for event_attributes
	public static final String EVENT_ATTRIBUTE_FORBIDDEN_TYPE = "Event attribute cannot be of type '%s'. Please use one of the following: EDouble, EInt, EBoolan, EString.";
	public static final String EVENT_ATTRIBUTE_INVALID = CODE_PREFIX +  "event_pattern.attribute.invalid";
	
	// Errors for event_patterns
	public static final String EVENT_PATTERN_NAME_FORBIDDEN_MESSAGE = "Event_pattern cannot be named '%s'. Use a different name.";
	public static final String EVENT_PATTERN_NAME_MULTIPLE_DECLARATIONS_MESSAGE = "Event_pattern '%s' must not be declared multiple times.";
	
	public static final String EVENT_PATTERN_INVALID_RETURN = CODE_PREFIX +  "event_pattern.returnStatement.invalid";
	public static final String SPAWNING_EVENT_PATTERN_EVENT_MISSMATCH_MESSAGE = "Event_pattern %s spawns a different event than indicated.";
	public static final String SPAWNING_EVENT_PATTERN_PARAMETER_NUMBER_MISSMATCH_MESSAGE = "Event_pattern %s spawns an event with the wrong number of parameters.";
	public static final String SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_MESSAGE = "Event_pattern %s spawns an event with wrong parameters.";
	public static final String SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_WARNING = "Event_pattern %s spawns an event without matching parameters. Will be cast automatically...";
	
	// Errors for event pattern nodes
	public static final String EVENT_PATTERN_NODE_NAME_FORBIDDEN_MESSAGE = "Event_pattern node cannot be named '%s'. Use a different name.";
	public static final String EVENT_PATTERN_NODE_NAME_MULTIPLE_DECLARATIONS_MESSAGE = "Event_pattern node '%s' must not be declared multiple times.";
	
	// Errors for arithmetic expressions
	public static final String ARITHMETIC_EXPRESSION_INVALID = CODE_PREFIX +  "event_pattern.arithmetic_expression.invalid";
	public static final String ARITHMETIC_EXPRESSION_FORBIDDEN_OPERATION = "Operation '%s' can not be used on data type '%s'.";

	@Check
	public void checkEvent(Event event) {
		checkEventNameValid(event);
		checkEventNameUnique(event);
	}
	
	@Check void checkEventAttribute(EventAttribute eventAttribute) {
		if(eventAttribute.getType() instanceof EDataType) {
			EDataType type  = (EDataType) eventAttribute.getType();
			if(!(type == EcorePackage.Literals.EDOUBLE || type == EcorePackage.Literals.EINT || type == EcorePackage.Literals.EBOOLEAN || type == EcorePackage.Literals.ESTRING)) {
				error(String.format(EVENT_ATTRIBUTE_FORBIDDEN_TYPE, type.getName()),
						GrapelPackage.Literals.EVENT_ATTRIBUTE__TYPE,
						EVENT_ATTRIBUTE_INVALID);
			}
		}
	}

	@Check
	public void checkEventPattern(EventPattern pattern) {
		checkEventPatternNameValid(pattern);
		checkEventPatternNameUnique(pattern);
	}
	
	@Check
	public void checkEventPatternNode(EventPatternNode node) {
		checkEventPatternNodeNameValid(node);
		checkEventPatternNodeNameUnique(node);
	}
	
	@Check
	public void checkReturnStatement(ReturnStatement statement) {
		EventPattern pattern = (EventPattern)statement.eContainer();
		
		if(!statement.getReturnArg().equals(pattern.getReturnType().getReturnType()))
			error(String.format(SPAWNING_EVENT_PATTERN_EVENT_MISSMATCH_MESSAGE, pattern.getName()),
					GrapelPackage.Literals.RETURN_STATEMENT__RETURN_ARG,
					EVENT_PATTERN_INVALID_RETURN);
		checkReturnStatementParameters(pattern,  statement);
	}
	
	@Check
	public void attributeExpressions(AttributeExpression expr) {
		checkStringExpressions(expr);
	}
	
	public void checkEventNameValid(Event event) {
		if(event.getName() == null)
			return;
		if(keywordBlacklist.contains(event.getName()))
			error(String.format(EVENT_NAME_FORBIDDEN_MESSAGE, event.getName()),
					GrapelPackage.Literals.EVENT__NAME,
					NAME_BLACKLISTED);
		// any style rules?
	}
	
	public void checkEventNameUnique(Event event) {
		EditorGTFile file = (EditorGTFile) event.eContainer();
		long count = file.getEvents().stream().filter(e -> e.getName() !=  null && e.getName().equals(event.getName())).count();
		if (count != 1)
			error(String.format(EVENT_NAME_MULTIPLE_DECLARATIONS_MESSAGE, event.getName()),
					GrapelPackage.Literals.EVENT__NAME,
					NAME_EXPECT_UNIQUE);
	}
	
	public void checkEventPatternNameValid(EventPattern pattern) {
		if(pattern.getName() == null)
			return;
		if(keywordBlacklist.contains(pattern.getName()))
			error(String.format(EVENT_PATTERN_NAME_FORBIDDEN_MESSAGE, pattern.getName()),
					GrapelPackage.Literals.EVENT_PATTERN__NAME,
					NAME_BLACKLISTED);
		// any style rules?
	}
	
	public void checkEventPatternNameUnique(EventPattern pattern) {
		EditorGTFile file = (EditorGTFile) pattern.eContainer();
		long count = file.getEventPatterns().stream().filter(p -> p.getName() !=  null && p.getName().equals(pattern.getName())).count();
		if (count != 1)
			error(String.format(EVENT_PATTERN_NAME_MULTIPLE_DECLARATIONS_MESSAGE, pattern.getName()),
					GrapelPackage.Literals.EVENT_PATTERN__NAME,
					NAME_EXPECT_UNIQUE);
	}
	
	public void checkEventPatternNodeNameValid(EventPatternNode node) {
		if(node.getName() == null)
			return;
		if(keywordBlacklist.contains(node.getName()))
			error(String.format(EVENT_PATTERN_NODE_NAME_FORBIDDEN_MESSAGE, node.getName()),
					GrapelPackage.Literals.EVENT_PATTERN_NODE__NAME,
					NAME_BLACKLISTED);
		// any style rules?
	}
	
	public void checkEventPatternNodeNameUnique(EventPatternNode node) {
		EventPattern pattern = (EventPattern) node.eContainer();
		long count = pattern.getNodes().stream().filter(p -> p.getName() !=  null && p.getName().equals(node.getName())).count();
		if (count != 1)
			error(String.format(EVENT_PATTERN_NODE_NAME_MULTIPLE_DECLARATIONS_MESSAGE, node.getName()),
					GrapelPackage.Literals.EVENT_PATTERN_NODE__NAME,
					NAME_EXPECT_UNIQUE);
	}
	
	public void checkReturnStatementParameters(EventPattern pattern, ReturnStatement statement) {
		if(statement.getReturnParams().size() != statement.getReturnArg().getAttributes().size()) {
			error(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_NUMBER_MISSMATCH_MESSAGE , pattern.getName()),
					GrapelPackage.Literals.RETURN_STATEMENT__RETURN_ARG,
					EVENT_PATTERN_INVALID_RETURN);
			return;
		}
		
		for(int i = 0; i < statement.getReturnParams().size(); i++) {
			EventAttribute expected = statement.getReturnArg().getAttributes().get(i);
			AttributeExpression given = statement.getReturnParams().get(i);
			if(expected.getType() instanceof EClass) {
				if(!(given instanceof EventPatternNodeAttributeExpression)) {
					error(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_MESSAGE , pattern.getName()),
							GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
							EVENT_PATTERN_INVALID_RETURN);
					continue;
				}
				
				EventPatternNodeAttributeExpression expr = (EventPatternNodeAttributeExpression) given;
				if(expr.getField() != null) {
					error(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_MESSAGE , pattern.getName()),
							GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
							EVENT_PATTERN_INVALID_RETURN);
					continue;
				}
				EventPatternNodeExpression nodeExpr = (EventPatternNodeExpression) expr.getNodeExpression();
				if(nodeExpr.getAttribute() instanceof EventAttribute) {
					if(expected != nodeExpr.getAttribute())
						error(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_MESSAGE , pattern.getName()),
								GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
								EVENT_PATTERN_INVALID_RETURN);
						continue;
				} else {
					EditorNode gtNode = (EditorNode)nodeExpr.getAttribute();
					if(expected.getType() != gtNode.getType())
						error(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_MESSAGE , pattern.getName()),
								GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
								EVENT_PATTERN_INVALID_RETURN);
						continue;
				}
			}else {
				if((given instanceof EventPatternNodeAttributeExpression)) {
					EventPatternNodeAttributeExpression expr = (EventPatternNodeAttributeExpression) given;
					if(expr.getField() == null) {
						error(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_MESSAGE , pattern.getName()),
								GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
								EVENT_PATTERN_INVALID_RETURN);
						continue;
					}
					EAttribute attribute = expr.getField();
					if(expected.getType() != attribute.getEType()) {
						warning(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_WARNING , pattern.getName()),
								GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
								EVENT_PATTERN_INVALID_RETURN);
						continue;
					}
				} else {
					EClassifier givenType = getTypeOfExpression(given);
					if(givenType == null) {
						error(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_WARNING , pattern.getName()),
								GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
								EVENT_PATTERN_INVALID_RETURN);
						continue;
					}
					if(!(givenType instanceof EDataType)) {
						error(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_WARNING , pattern.getName()),
								GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
								EVENT_PATTERN_INVALID_RETURN);
						continue;
					}
					if(givenType != expected.getType()) {
						warning(String.format(SPAWNING_EVENT_PATTERN_PARAMETER_MISSMATCH_WARNING , pattern.getName()),
								GrapelPackage.Literals.RETURN_STATEMENT__RETURN_PARAMS, i,
								EVENT_PATTERN_INVALID_RETURN);
					}
				}
			}
		}
			
		
	}
	
	public void checkStringExpressions(AttributeExpression expr) {
		if(expr instanceof AttributeExpressionLiteral) {
			return;
		}
		
		if(expr instanceof EventPatternNodeAttributeExpression) {
			return;
		}
		
		if(expr instanceof UnaryAttributeExpression) {
			UnaryAttributeExpression uexpr = (UnaryAttributeExpression)expr;
			EClassifier classifier = getTypeOfExpression(uexpr.getOperand());
			if(classifier == EcorePackage.Literals.ESTRING && uexpr.getOperator() != UnaryOperator.NONE) {
				error(String.format(ARITHMETIC_EXPRESSION_FORBIDDEN_OPERATION , uexpr.getOperator().getName(), classifier.getName()),
						GrapelPackage.Literals.UNARY_ATTRIBUTE_EXPRESSION__OPERATOR,
						ARITHMETIC_EXPRESSION_INVALID);
			}
			return;
		}
		
		BinaryAttributeExpression biexpr = (BinaryAttributeExpression)expr;
		EClassifier lhsType = getTypeOfExpression(biexpr.getLeft());
		EClassifier rhsType = getTypeOfExpression(biexpr.getRight());	
		if(lhsType != rhsType && (lhsType == EcorePackage.Literals.ESTRING || rhsType == EcorePackage.Literals.ESTRING)) {
			if(biexpr.getOperator() != ArithmeticOperator.PLUS) {
				error(String.format(ARITHMETIC_EXPRESSION_FORBIDDEN_OPERATION , biexpr.getOperator().getName(), EcorePackage.Literals.ESTRING.getName()),
						GrapelPackage.Literals.BINARY_ATTRIBUTE_EXPRESSION__OPERATOR,
						ARITHMETIC_EXPRESSION_INVALID);
			}
		}
		
		return;
	}
	
	public static EClassifier getTypeOfExpression(AttributeExpression expr) {
		if(expr instanceof AttributeExpressionLiteral) {
			AttributeExpressionLiteral literal = (AttributeExpressionLiteral)expr;
			if(literal instanceof DoubleLiteral) {
				return EcorePackage.Literals.EDOUBLE;
			} else if(literal instanceof IntegerLiteral) {
				return EcorePackage.Literals.EINT;
			} else if(literal instanceof StringLiteral) {
				return EcorePackage.Literals.ESTRING;
			} else {
				return EcorePackage.Literals.EBOOLEAN;
			}
		}
		
		if(expr instanceof EventPatternNodeAttributeExpression) {
			EventPatternNodeAttributeExpression epnExpr = (EventPatternNodeAttributeExpression) expr;
			return epnExpr.getField().getEType();
		}
		
		if(expr instanceof UnaryAttributeExpression) {
			UnaryAttributeExpression uexpr = (UnaryAttributeExpression)expr;
			return getTypeOfExpression(uexpr.getOperand());
		}
		
		BinaryAttributeExpression biexpr = (BinaryAttributeExpression)expr;
		EClassifier lhsType = getTypeOfExpression(biexpr.getLeft());
		EClassifier rhsType = getTypeOfExpression(biexpr.getRight());
		if(lhsType == rhsType)
			return lhsType;
		
		if(lhsType == EcorePackage.Literals.ESTRING || rhsType == EcorePackage.Literals.ESTRING) {
			return EcorePackage.Literals.ESTRING;
		}
		
		if(lhsType == EcorePackage.Literals.EDOUBLE || rhsType == EcorePackage.Literals.EDOUBLE) {
			return EcorePackage.Literals.EDOUBLE;
		}
		
		return null;
	}

}
