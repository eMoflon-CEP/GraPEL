package org.emoflon.cep.ui.visualization

import org.emoflon.cep.grapel.Event
import org.emoflon.cep.grapel.EventPattern
import org.emoflon.cep.grapel.EventAttribute
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EClass
import org.emoflon.ibex.gt.editor.gT.EditorPattern
import java.util.HashMap
import org.emoflon.cep.grapel.EventPatternNode
import org.eclipse.emf.ecore.EObject
import org.emoflon.ibex.gt.editor.gT.EditorNode
import org.emoflon.cep.grapel.EventPatternContextConstraint
import org.emoflon.cep.grapel.RelationalConstraint
import java.util.Map
import org.emoflon.cep.grapel.BinaryRelationalConstraint
import org.emoflon.cep.grapel.UnaryRelationalConstraint
import org.emoflon.cep.grapel.RelationalNodeExpression
import org.emoflon.cep.grapel.UnaryRelationalOperator
import org.emoflon.cep.grapel.BinaryRelationalOperator
import java.util.List
import java.util.LinkedList
import java.util.Map.Entry
import java.util.AbstractMap.SimpleEntry
import org.emoflon.ibex.gt.editor.ui.visualization.GTPlantUMLGenerator
import org.emoflon.ibex.gt.editor.utils.GTFlattener
import org.emoflon.ibex.gt.editor.ui.visualization.GTVisualizationUtils

class GrapeLPlantUMLGenerator {
	static val ContextColor = 'Black'
	static val CreateColor = 'DarkGreen'
	static val DeleteColor = 'Crimson'
	static val LocalNodeColor = 'Gray'
	static val AtrConstrColor = 'White'
	static int MAX_STR_LENGTH = 100
	
	static val ComplexFieldColor = 'Blue'
	static val SimpleFieldColor = 'Gray'
	static val EventNodeColor = '644011'
	static val PatternNodeColor = 'Black'
	static val RelationPackageColor = 'F9dAAF'
	static val RelationNodeColor = 'F2B42B'
	
	static def String visualizeEvent(Event event) {
		'''
			«commonLayoutSettings»
			
			skinparam class {
				HeaderBackgroundColor<<ComplexField>> «ComplexFieldColor»
				HeaderBackgroundColor<<SimpleField>> «SimpleFieldColor»
				BorderColor<<ComplexField>> «ComplexFieldColor»
				BorderColor<<SimpleField>> «SimpleFieldColor»
«««				FontColor<<ATR_CONSTR>> «ContextColor»
				FontColor White
			}
			
			namespace «event.name» {
				«FOR field : event.attributes.filter[field | !(field.type instanceof EDataType)]»
				class "«field.name» : «field.type.name»" <<«fieldSkin(field)»>> {
					«FOR atr : (field.type as EClass).EAllAttributes»
					+ «atr.name» : «atr.EType.name»
					«ENDFOR»
				}
				«ENDFOR»
				class Primitive-Fields <<SimpleField>>{
				«FOR field : event.attributes.filter[field | (field.type instanceof EDataType)]»
				+ «field.name» : «field.type.name»
				«ENDFOR»
				}
			}
			
		'''
	}
	
	static def String visualizeEventPattern(EventPattern eventPattern) {
		val node2NodeName = new HashMap
//		for(node : eventPattern.nodes.filter[node | node.type instanceof Event]) {
//			node2NodeName.put(node.name, node.name+" : "+(node.type as Event).name)
//		}
//		for(node : eventPattern.nodes.filter[node | node.type instanceof EditorPattern]) {
//			node2NodeName.put(node.name, node.name+" : "+(node.type as EditorPattern).name)
//		}

		for(node : eventPattern.nodes.filter[node | node.type instanceof Event]) {
			node2NodeName.put(node.name, '''Events.«(node.type as Event).name»''')
		}

		for(node : eventPattern.nodes.filter[node | node.type instanceof EditorPattern]) {
			node2NodeName.put(node.name, '''Graph_Patterns.«(node.type as EditorPattern).name»''')
		}
		'''
			«commonLayoutSettings»
			
			skinparam class {
				HeaderBackgroundColor<<EventNode>> «EventNodeColor»
				HeaderBackgroundColor<<PatternNode>> «PatternNodeColor»
				HeaderBackgroundColor<<RelationPackage>> «RelationPackageColor»
				HeaderBackgroundColor<<RelationNode>> «RelationNodeColor»
				HeaderBackgroundColor<<ComplexField>> «ComplexFieldColor»
				HeaderBackgroundColor<<SimpleField>> «SimpleFieldColor»
				HeaderBackgroundColor<<CONTEXT>> «ContextColor»
				HeaderBackgroundColor<<CREATE>> «CreateColor»
				HeaderBackgroundColor<<DELETE>> «DeleteColor»
				HeaderBackgroundColor<<ATR_CONSTR>> «AtrConstrColor»
				HeaderBackgroundColor<<LOCAL_NODE>> «LocalNodeColor»
				BorderColor<<ComplexField>> «ComplexFieldColor»
				BorderColor<<SimpleField>> «SimpleFieldColor»
				BorderColor<<EventNode>> «EventNodeColor»
				BorderColor<<PatternNode>> «PatternNodeColor»
				BorderColor<<RelationPackage>> «RelationPackageColor»
				BorderColor<<RelationNode>> «RelationNodeColor»
				BorderColor<<CONTEXT>> «ContextColor»
				BorderColor<<CREATE>> «CreateColor»
				BorderColor<<DELETE>> «DeleteColor»
				BorderColor<<ATR_CONSTR>> «ContextColor»
				BorderColor<<LOCAL_NODE>> «ContextColor»
				FontColor<<RelationNode>> Black
				FontColor White
				FontColor<<ATR_CONSTR>> «ContextColor»
			}
			
			namespace «eventPattern.name» {
				«FOR node : eventPattern.nodes.filter[node | node.type instanceof Event]»
				class "«node.name» : «(node.type as Event).name»" <<EventNode>>
				«ENDFOR»
				«FOR node : eventPattern.nodes.filter[node | node.type instanceof EditorPattern]»
				class "«node.name» : «(node.type as EditorPattern).name»" <<PatternNode>>
				«ENDFOR»
				
«««				«FOR contextConstraint : eventPattern.contextConstraints»
«««				"«node2NodeName.get(contextConstraint.lhs.patternNode.name)»" -[#000000]- "«node2NodeName.get(contextConstraint.rhs.patternNode.name)»": «contextConstraintLabel(contextConstraint)»
«««				«ENDFOR»
				
				package Relational_Constraint #fff3d0{
					«formatRelationalConstraint(eventPattern, eventPattern.relationalConstraint).value»
				}
				
				
			}
			
			«IF !eventPattern.nodes.filter[node | node.type instanceof EditorPattern].map[node | node.type as EditorPattern].empty»
			namespace Graph_Patterns #c9c9c9{
			«FOR pattern : eventPattern.nodes.filter[node | node.type instanceof EditorPattern].map[node | node.type as EditorPattern].toSet»
				«formatPattern(pattern)»
			«ENDFOR»
			}
			«ENDIF»
			
			«IF !eventPattern.nodes.filter[node | node.type instanceof Event].map[node | node.type as Event].empty»
			namespace Events #ccb19a{
			«FOR event : eventPattern.nodes.filter[node | node.type instanceof Event].map[node | node.type as Event].toSet»
				«formatEvent(event)»
			«ENDFOR»
			}
			«ENDIF»
			
			«FOR node : eventPattern.nodes.filter[node | node.type instanceof Event]»
			"«eventPattern.name».«node.name» : «(node.type as Event).name»" #-[#335bb0]-# "Events.«(node.type as Event).name»"
			«ENDFOR»
			«FOR node : eventPattern.nodes.filter[node | node.type instanceof EditorPattern]»
			"«eventPattern.name».«node.name» : «(node.type as EditorPattern).name»" #-[#335bb0]-# "Graph_Patterns.«(node.type as EditorPattern).name»"
			«ENDFOR»
			
			«FOR contextConstraint : eventPattern.contextConstraints»
			"«node2NodeName.get(contextConstraint.lhs.patternNode.name)».«nodeAttributeName(contextConstraint.lhs.patternNode, contextConstraint.lhs.attribute)»: «nodeAttributeType(contextConstraint.lhs.patternNode, contextConstraint.lhs.attribute)»" *-[#a61e1e]-* "«node2NodeName.get(contextConstraint.rhs.patternNode.name)».«nodeAttributeName(contextConstraint.rhs.patternNode, contextConstraint.rhs.attribute)»: «nodeAttributeType(contextConstraint.rhs.patternNode, contextConstraint.rhs.attribute)»" : «contextConstraint.relation»
			«ENDFOR»
		'''
	}
	
	private static def String fieldSkin(EventAttribute field) {
		if(field.type instanceof EDataType) {
			return "SimpleField"
		} else {
			return "ComplexField" 
		}
	}
	
	private static def String contextConstraintLabel(EventPatternContextConstraint constraint) {
		val lhsName = constraint.lhs.patternNode.name
		val rhsName = constraint.rhs.patternNode.name
		val lhsAtr = nodeAttributeName(constraint.lhs.patternNode, constraint.lhs.attribute)
		val rhsAtr = nodeAttributeName(constraint.rhs.patternNode, constraint.rhs.attribute)
		return '''«lhsName».«lhsAtr»\n«constraint.relation.toString»\n«rhsName».«rhsAtr»
		'''
	}
	
	private static def Entry<String, String> formatRelationalConstraint(EventPattern eventPattern, RelationalConstraint constraint) {		
		if(constraint instanceof BinaryRelationalConstraint) {
			val left = formatRelationalConstraint(eventPattern, constraint.left);
			val right = formatRelationalConstraint(eventPattern, constraint.right);
			if(constraint.operator == BinaryRelationalOperator.FOLLOWS) {
				val expr = '''«left.value»
				«right.value»
				"«left.key»" -[#852424]-> "«right.key»"
				'''
				val result = new SimpleEntry(right.key, expr)
				return result
			} else if(constraint.operator == BinaryRelationalOperator.AND) {
				val name = '''AND(«left.key»,«right.key»)'''
				val expr = '''package «name» #FFFFFF{
					«left.value»
					«right.value»
				}'''
				val result = new SimpleEntry(name, expr)
				return result
			} else {
				val name = '''OR(«left.key»,«right.key»)'''
				val expr = '''package «name» #FFFFFF{
					«left.value»
					«right.value»
				}'''
				val result = new SimpleEntry(name, expr)
				return result
			}
		} else if (constraint instanceof UnaryRelationalConstraint) {
			if(constraint.operator == UnaryRelationalOperator.ALL) {
				val operand = formatRelationalConstraint(eventPattern, constraint.operand)
				val name = '''«IF constraint.negated»NOT_«ENDIF»ALL(«operand.key»)'''
				val expr = '''package «name» #FFFFFF{
					«operand.value»
				}'''
				val result = new SimpleEntry(name, expr)
				return result
			} else {
				val operand = formatRelationalConstraint(eventPattern,constraint.operand)
				val name = '''«IF constraint.negated»NOT_«ENDIF»(«operand.key»)'''
				val expr = '''package «name» #FFFFFF{
					«operand.value»
				}'''
				val result = new SimpleEntry(name, expr)
				return result
			}
		} else {
			val terminal = constraint as RelationalNodeExpression
			val name = terminal.node.name
			val expr = '''class «name» <<RelationNode>>
			"«name»" #-[#000000]-# "«eventPattern.name».«name» : «nodeTypeName(terminal.node)»"
			'''
			val result = new SimpleEntry(name, expr)
			return result
		}
	}
	
	private static def String formatPattern(EditorPattern pattern) {
		val nodeNamesInFlattenedPattern = pattern.nodes.map[it.name]
		return '''
		namespace «pattern.name» #FFFFFF{		
			«GTPlantUMLGenerator.visualizeGraph(pattern)»
			«GTPlantUMLGenerator.visualizeAttributeConstraints(pattern)»
			«GTPlantUMLGenerator.visualizeRate(pattern)»
		
								
«««		«FOR p : GTPlantUMLGenerator.getConditionPatterns(pattern)»
«««			«val flattenedConditionPattern = p»
«««			namespace «p.name» #EEEEEE {
«««				«GTPlantUMLGenerator.visualizeGraph(flattenedConditionPattern)»
«««			}
«««									
«««			«FOR node : flattenedConditionPattern.nodes»
«««				«IF nodeNamesInFlattenedPattern.contains(node.name)»
«««					"«pattern.name».«GTPlantUMLGenerator.nodeName(pattern, node.name)»" #--# "«p.name».«GTPlantUMLGenerator.nodeName(node)»"
«««				«ENDIF»
«««			«ENDFOR»
«««		«ENDFOR»
								
«««		«IF !pattern.conditions.isEmpty»
«««			class Conditions {
«««				«GTVisualizationUtils.getConditionString(pattern)»
«««			}
«««		«ENDIF»				
«««			class «pattern.name»_Signature {
«««				«GTVisualizationUtils.signature(pattern)»
«««			}
		}
		'''		
	}
	
	private static def String formatEvent(Event event) {
		return '''
			namespace «event.name» #FFFFFF{
				«FOR field : event.attributes.filter[field | !(field.type instanceof EDataType)]»
				class "«field.name»: «field.type.name»" <<«fieldSkin(field)»>> {
					«FOR atr : (field.type as EClass).EAllAttributes»
					+ «atr.name»: «atr.EType.name»
					«ENDFOR»
				}
				«ENDFOR»
				class Primitive-Fields <<SimpleField>>{
				«FOR field : event.attributes.filter[field | (field.type instanceof EDataType)]»
				+ «field.name»: «field.type.name»
				«ENDFOR»
				}
			}
		'''
	}
	
	private static def String nodeTypeName(EventPatternNode node) {
		if(node.type instanceof Event) {
			val type = node.type as Event
			return type.name
		}else {
			val type = node.type as EditorPattern
			return type.name
		}
	}
	
	private static def String nodeAttributeName(EventPatternNode node, EObject atr) {
		if(node.type instanceof Event) {
			val eventAtr = atr as EventAttribute
			return eventAtr.name
		}else {
			val patternNode = atr as EditorNode
			return patternNode.name
		}
	}
	
	private static def String nodeAttributeType(EventPatternNode node, EObject atr) {
		if(node.type instanceof Event) {
			val eventAtr = atr as EventAttribute
			return eventAtr.type.name
		}else {
			val patternNode = atr as EditorNode
			return patternNode.type.name
		}
	}
	
	/**
	 * Print the common settings for all visualizations.
	 */
	private static def String commonLayoutSettings() {
		'''
			hide empty members
			hide circle
			hide stereotype
			
			skinparam padding 2
			skinparam shadowing false
		'''
	}
}