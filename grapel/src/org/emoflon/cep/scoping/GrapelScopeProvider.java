/*
 * generated by Xtext 2.20.0
 */
package org.emoflon.cep.scoping;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.emoflon.cep.grapel.AttributeConstraint;
import org.emoflon.cep.grapel.AttributeExpression;
import org.emoflon.cep.grapel.EditorGTFile;
import org.emoflon.cep.grapel.Event;
import org.emoflon.cep.grapel.EventAttribute;
import org.emoflon.cep.grapel.EventPattern;
import org.emoflon.cep.grapel.EventPatternContextConstraint;
import org.emoflon.cep.grapel.EventPatternNode;
import org.emoflon.cep.grapel.EventPatternNodeAttributeExpression;
import org.emoflon.cep.grapel.EventPatternNodeExpression;
import org.emoflon.cep.grapel.GrapelPackage;
import org.emoflon.cep.grapel.MatchEventState;
import org.emoflon.cep.grapel.RelationalConstraint;
import org.emoflon.cep.grapel.impl.EventPatternImpl;
import org.emoflon.ibex.gt.editor.gT.EditorEnumExpression;
import org.emoflon.ibex.gt.editor.gT.EditorNode;
import org.emoflon.ibex.gt.editor.gT.EditorPattern;
import org.emoflon.ibex.gt.editor.utils.GTEditorModelUtils;
import org.emoflon.ibex.gt.editor.utils.GTEditorPatternUtils;
import org.emoflon.ibex.gt.editor.utils.GTEnumExpressionHelper;

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
public class GrapelScopeProvider extends AbstractGrapelScopeProvider {
	@Override
	public IScope getScope(EObject context, EReference reference) {	
		// Events
	    if (isEvent(context, reference)) {
	    	return getScopeForEvents((Event)context);
	    }
	    // EventAttributes
	    if (isEventAttribute(context, reference)) {
	    	return getScopeForEventAttributes((EventAttribute)context);
		}
	    // EventPatterns
	    if (isEventPattern(context, reference)) {
	    	return getScopeForEventPatterns((EventPattern)context, reference);
		}
	    // EventPatternReturnTypes
	    if (isEventPatternReturnType(context, reference)) {
	    	return getScopeForEventPatternReturnTypes((EventPattern)context, reference);
		}
	    // EventPatternNodes
	    if (isEventPatternNode(context, reference)) {
	    	return getScopeForEventPatternNodes((EventPatternNode)context);
		}
	    // EventPatternContextConstraints
	    if (isEventPatternContextConstraint(context, reference)) {
	    	return getScopeForEventPatternContextConstraintNodes((EventPatternContextConstraint)context);
		}
	    // EventPatternNodeExpression
	    if (isEventPatternNodeExpression(context, reference)) {
	    	return getScopeForEventPatternNodeExpressions((EventPatternNodeExpression)context, reference);
		}
	    // EventPatternNodeAttributeExpressionField
	    if (isEventPatternNodeAttributeExpressionAttributeField(context, reference)) {
	    	return getScopeForEventPatternNodeAttributeExpressionAttributeFields((EventPatternNodeAttributeExpression)context, reference);
		}
	    // EventPatternRelationalConstraint
	    if (isRelationalConstraint(context)) {
	    	return getScopeRelationalConstraints((RelationalConstraint)context);
		}
	    // MatchEventState
	    if(context instanceof MatchEventState) {
	    	return getScopeForMatchEventState((MatchEventState)context);
	    }
	    // AttributeConstraint
	    if (isGrapelAttributeConstraint(context, reference)) {
	    	return getScopeForGrapelAttributeConstraints((AttributeConstraint)context, reference);
		}
	    // AttributeExpression
	    if (isGrapelAttributeExpression(context, reference)) {
	    	return getScopeForGrapelAttributeExpressions((AttributeExpression)context, reference);
		}
	    return super.getScope(context, reference);
	}
	
	private IScope getScopeForMatchEventState(MatchEventState context) {
		Collection<EObject> scope = new HashSet<>();
		scope.addAll(GTEditorPatternUtils.getContainer(context, EventPatternImpl.class).getNodes().stream()
				.filter(node -> (node.getType() instanceof EditorPattern))
				.collect(Collectors.toList()));
		return Scopes.scopeFor(scope);
	}
	
	private IScope getScopeForEventPatternNodeExpressions(EventPatternNodeExpression context, EReference reference) {
		Collection<EObject> scope = new HashSet<>();
		
		if(reference == GrapelPackage.Literals.EVENT_PATTERN_NODE_EXPRESSION__PATTERN_NODE) {
			EventPattern ePattern = GTEditorPatternUtils.getContainer(context, EventPatternImpl.class);
			scope.addAll(ePattern.getNodes());
		}
		
		if(reference == GrapelPackage.Literals.EVENT_PATTERN_NODE_EXPRESSION__ATTRIBUTE) {
			if(context.getPatternNode() == null)
				return super.getScope(context, reference);
			if(context.getPatternNode().getType() instanceof Event) {
				Event event = (Event)context.getPatternNode().getType();
				scope.addAll(event.getAttributes());
			}else {
				EditorPattern pattern = (EditorPattern) context.getPatternNode().getType();
				scope.addAll(pattern.getNodes());
			}
		}
		
		
		return Scopes.scopeFor(scope);
	}

	private boolean isEventPatternNodeExpression(EObject context, EReference reference) {
		return (context instanceof EventPatternNodeExpression);
	}

	private IScope getScopeForGrapelAttributeExpressions(AttributeExpression context, EReference reference) {
		return Scopes.scopeFor(GTEditorPatternUtils.getContainer(context, EventPatternImpl.class).getNodes());
	}

	private boolean isGrapelAttributeExpression(EObject context, EReference reference) {
		 return (context instanceof AttributeExpression);
	}

	private boolean isEventPatternReturnType(EObject context, EReference reference) {
		 return (context instanceof EventPattern && 
				 (reference == GrapelPackage.Literals.EVENT_PATTERN__RETURN_TYPE || reference == GrapelPackage.Literals.EVENT_PATTERN__RETURN_STATEMENT));
	}
	
	protected IScope getScopeForEventPatternReturnTypes(EventPattern context, EReference reference) {
		return Scopes.scopeFor(getGTFile(context).getEvents());
	}

	private IScope getScopeForGrapelAttributeConstraints(AttributeConstraint context, EReference reference) {
		return Scopes.scopeFor(GTEditorPatternUtils.getContainer(context, EventPatternImpl.class).getNodes());
	}

	private boolean isGrapelAttributeConstraint(EObject context, EReference reference) {
		return (context instanceof AttributeConstraint);
	}

	private IScope getScopeRelationalConstraints(RelationalConstraint context) {
		EventPattern ePattern = GTEditorPatternUtils.getContainer(context, EventPatternImpl.class);
		Collection<EObject> scope = new HashSet<>();
		scope.addAll(ePattern.getNodes());
		return Scopes.scopeFor(scope);
	}

	private boolean isRelationalConstraint(EObject context) {
		return (context instanceof RelationalConstraint);
	}

	private IScope getScopeForEventPatternNodeAttributeExpressionAttributeFields(
			EventPatternNodeAttributeExpression context, EReference reference) {
		Collection<EObject> scope = new HashSet<>();

		if(context.getNodeExpression() == null)
			return super.getScope(context, reference);
		if(context.getNodeExpression().getPatternNode() == null)
			return super.getScope(context, reference);
		if(context.getNodeExpression().getAttribute() == null)
			return super.getScope(context, reference);
		if(context.getNodeExpression().getAttribute().eContainer() == null)
			return super.getScope(context, reference);
		
		if(context.getNodeExpression().getPatternNode().getType() instanceof Event) {
			EventAttribute attribute = (EventAttribute)context.getNodeExpression().getAttribute();
			
			if(attribute.getType() instanceof EDataType)
				return super.getScope(context, reference);
			
			EClass clazz = (EClass)attribute.getType();
			scope.addAll(clazz.getEAllAttributes());
		}else {
			EditorNode node = (EditorNode) context.getNodeExpression().getAttribute();
			scope.addAll(node.getType().getEAllAttributes());
		}
		return Scopes.scopeFor(scope);
	}

	private boolean isEventPatternNodeAttributeExpressionAttributeField(EObject context, EReference reference) {
		return (context instanceof EventPatternNodeAttributeExpression 
				&& reference == GrapelPackage.Literals.EVENT_PATTERN_NODE_ATTRIBUTE_EXPRESSION__FIELD);
	}

	private IScope getScopeForEventPatternContextConstraintNodes(EventPatternContextConstraint context) {
		return Scopes.scopeFor(((EventPattern)context.eContainer()).getNodes());
	}

	private boolean isEventPatternContextConstraint(EObject context, EReference reference) {
		return (context instanceof EventPatternContextConstraint);
	}

	protected boolean isEvent(EObject context, EReference reference) {
	    return (context instanceof Event);
	}
	
	protected IScope getScopeForEvents(Event context) {
		Collection<EObject> scope = new HashSet<>();
		scope.addAll(GTEditorModelUtils.getClasses(getGTFile(context)));
		scope.addAll(GTEditorModelUtils.getDatatypes(getGTFile(context)));
		return Scopes.scopeFor(scope);
	}
	
	protected boolean isEventAttribute(EObject context, EReference reference) {
	    return (context instanceof EventAttribute && reference == GrapelPackage.Literals.EVENT_ATTRIBUTE__TYPE);
	}
	
	protected IScope getScopeForEventAttributes(EventAttribute context) {
		Collection<EObject> scope = new HashSet<>();
		scope.addAll(GTEditorModelUtils.getClasses(getGTFile(context)));
		scope.addAll(GTEditorModelUtils.getDatatypes(getGTFile(context)));
		return Scopes.scopeFor(scope);
	}
	
	protected boolean isEventPattern(EObject context, EReference reference) {
	    return (context instanceof EventPattern && reference != GrapelPackage.Literals.EVENT_PATTERN__RETURN_TYPE);
	}
	
	protected IScope getScopeForEventPatterns(EventPattern context, EReference reference) {
		return Scopes.scopeFor(context.getNodes());
	}
	
	protected boolean isEventPatternNode(EObject context, EReference reference) {
	    return (context instanceof EventPatternNode && reference == GrapelPackage.Literals.EVENT_PATTERN_NODE__TYPE);
	}
	
	protected IScope getScopeForEventPatternNodes(EventPatternNode context) {
		Collection<EObject> scope = new HashSet<>();
		scope.addAll(getGTFile(context).getPatterns());
		scope.addAll(getGTFile(context).getEvents());
		return Scopes.scopeFor(scope);
	}
	
	@Override
	public IScope getScopeForEnumLiterals(EditorEnumExpression enumExpression) {
		EEnum type = (EEnum)GTEnumExpressionHelper.getEnumDataType(enumExpression);
		if (type != null && type instanceof EEnum) {
			return Scopes.scopeFor(type.getELiterals());
		} else {
			EditorGTFile gtFile = getGTFile(enumExpression);
			return Scopes.scopeFor(GTEditorModelUtils.getEnums(gtFile).stream()
					.flatMap(e -> e.getELiterals().stream())
					.collect(Collectors.toList()));
		}

	}
	
	public static EditorGTFile getGTFile(EObject node) {
		EObject current = node;
		while(!(current instanceof EditorGTFile)) {
			if(node.eContainer() == null)
				return null;
			
			current = current.eContainer();
		}
		return (EditorGTFile)current;
	}
}
