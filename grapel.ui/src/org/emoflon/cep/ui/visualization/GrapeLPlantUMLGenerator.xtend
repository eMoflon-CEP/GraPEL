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
import org.emoflon.cep.grapel.RelationalConstraint
import org.emoflon.cep.grapel.BinaryRelationalConstraint
import org.emoflon.cep.grapel.UnaryRelationalConstraint
import org.emoflon.cep.grapel.RelationalNodeExpression
import org.emoflon.cep.grapel.UnaryRelationalOperator
import org.emoflon.cep.grapel.BinaryRelationalOperator
import java.util.Map.Entry
import java.util.AbstractMap.SimpleEntry
import org.emoflon.ibex.gt.editor.ui.visualization.GTPlantUMLGenerator
import org.emoflon.cep.grapel.ReturnSpawn
import org.emoflon.cep.grapel.ReturnApply
import org.emoflon.cep.grapel.SpawnStatement
import org.emoflon.cep.grapel.ApplyStatement
import org.emoflon.cep.grapel.EditorGTFile
import org.emoflon.ibex.gt.editor.gT.EditorApplicationCondition
import java.util.HashSet
import org.emoflon.ibex.gt.editor.ui.visualization.GTVisualizationUtils
import org.emoflon.cep.grapel.EventPatternNodeAttributeExpression

class GrapeLPlantUMLGenerator {
	static val ContextColor = 'Black'
	static val CreateColor = 'DarkGreen'
	static val DeleteColor = 'Crimson'
	static val LocalNodeColor = 'Gray'
	static val AtrConstrColor = 'White'
	
	static val ComplexFieldColor = 'Blue'
	static val SimpleFieldColor = 'Gray'
	static val EventNodeColor = '644011'
	static val PatternNodeColor = 'Black'
	static val RelationPackageColor = 'F9dAAF'
	static val RelationNodeColor = 'F2B42B'
	
	static def String visualizeDependencies(EditorGTFile file) {
		return '''
			«commonLayoutSettings»
			skinparam class {
				HeaderBackgroundColor<<EventPattern>> White
				BorderColor<<EventPattern>> Black
				HeaderBackgroundColor<<Event>> #634917
				BorderColor<<Event>> #634917
				HeaderBackgroundColor<<Pattern>> #595959
				BorderColor<<Pattern>> #595959
				FontColor<<EventPattern>> Black
				FontColor White
			}
			
			namespace Event_Patterns {
			«IF file.eventPatterns !== null && !file.eventPatterns.empty»
			«FOR eventPattern : file.eventPatterns»
				class «eventPattern.name» <<EventPattern>> {
					EVENT_PATTERN
				}
				«FOR node : eventPattern.nodes»
				«IF node.type instanceof Event»
				"«eventPattern.name»" -[#000000]-> "Events.«nodeTypeName(node)»" : depends_on_«nodeTypeName(node)»
				«ELSE»
				"«eventPattern.name»" -[#000000]-> "Patterns.«nodeTypeName(node)»" : depends_on_«nodeTypeName(node)»
				«ENDIF»

				«ENDFOR»
				«IF eventPattern.returnType instanceof ReturnSpawn»
				"«eventPattern.name»" #-[#629157]-# "Events.«(eventPattern.returnType as ReturnSpawn).returnType.name»" : spawns_«(eventPattern.returnType as ReturnSpawn).returnType.name»
				«ENDIF»
			«ENDFOR»
			«ENDIF»
			}
			
			namespace Events {
			«IF file.events !== null && !file.events.empty»
			«FOR event : file.events»
				class «event.name» <<Event>> {
					EVENT
				}
			«ENDFOR»
			«ENDIF»
			}

			namespace Patterns {
			«IF file.patterns !== null && !file.patterns.empty»
			«FOR pattern : file.patterns»
				class «pattern.name» <<Pattern>> {
					PATTERN
				}
				«FOR subpattern : pattern.conditions.flatMap[cond | cond.conditions].filter[cond | cond instanceof EditorApplicationCondition].map[cond | cond as EditorApplicationCondition].map[cond | cond.pattern]»
				"Patterns.«pattern.name»" -[#000000]-> "Patterns.«subpattern.name»" : depends_on_«subpattern.name»
				«ENDFOR»
«««				TODO: Nested Conditions!
			«ENDFOR»
			«ENDIF»
			}
		'''
	}
	
	static def String visualizeEvent(Event event) {
		'''
			«commonLayoutSettings»
			
			skinparam class {
				HeaderBackgroundColor<<ComplexField>> «ComplexFieldColor»
				HeaderBackgroundColor<<SimpleField>> «SimpleFieldColor»
				BorderColor<<ComplexField>> «ComplexFieldColor»
				BorderColor<<SimpleField>> «SimpleFieldColor»
				FontColor White
			}
			
			«formatEvent(event)»
			
		'''
	}
	
	static def String visualizeEventPattern(EventPattern eventPattern) {
		val node2NodeName = new HashMap
		val contextConstraintNodes = new HashMap
		
		for(constraint : eventPattern.contextConstraints) {
			var lhsNodes = contextConstraintNodes.get(constraint.lhs.patternNode)
			if(lhsNodes == null){
				lhsNodes = new HashSet<EObject>
				contextConstraintNodes.put(constraint.lhs.patternNode, lhsNodes)
			}
			lhsNodes.add(constraint.lhs.attribute)
			
			var rhsNodes = contextConstraintNodes.get(constraint.rhs.patternNode)
			if(rhsNodes == null){
				rhsNodes = new HashSet<EObject>
				contextConstraintNodes.put(constraint.rhs.patternNode, rhsNodes)
			}
			rhsNodes.add(constraint.rhs.attribute)
		}

		for(node : eventPattern.nodes.filter[node | node.type instanceof Event]) {
			node2NodeName.put(node.name, '''Events.«(node.type as Event).name»''')
		}

		for(node : eventPattern.nodes.filter[node | node.type instanceof EditorPattern]) {
			node2NodeName.put(node.name, '''Graph_Patterns.«(node.type as EditorPattern).name»''')
		}
		'''
			«commonLayoutSettings»
			
			skinparam class {
				«GTPlantUMLGenerator.patternLayoutContent»
				HeaderBackgroundColor<<EventNode>> «EventNodeColor»
				HeaderBackgroundColor<<PatternNode>> «PatternNodeColor»
				HeaderBackgroundColor<<RelationPackage>> «RelationPackageColor»
				HeaderBackgroundColor<<RelationNode>> «RelationNodeColor»
				HeaderBackgroundColor<<ComplexField>> «ComplexFieldColor»
				HeaderBackgroundColor<<SimpleField>> «SimpleFieldColor»
				BorderColor<<ComplexField>> «ComplexFieldColor»
				BorderColor<<SimpleField>> «SimpleFieldColor»
				BorderColor<<EventNode>> «EventNodeColor»
				BorderColor<<PatternNode>> «PatternNodeColor»
				BorderColor<<RelationPackage>> «RelationPackageColor»
				BorderColor<<RelationNode>> «RelationNodeColor»
				FontColor<<RelationNode>> Black
			}
			
			namespace «eventPattern.name» {
				«FOR node : eventPattern.nodes.filter[node | node.type instanceof Event]»
				namespace «node.name» #ccb19a {
				«IF contextConstraintNodes.containsKey(node)»
						«FOR constraintNode : contextConstraintNodes.get(node)»
						class "«nodeAttributeName(node, constraintNode)» : «nodeAttributeType(node, constraintNode)»" <<EventNode>>
						«ENDFOR»
				«ENDIF»
				«IF eventPattern.returnStatement.returnParams
				.filter[param | param instanceof EventPatternNodeAttributeExpression]
				.map[param | param as EventPatternNodeAttributeExpression]
				.filter[param | param.nodeExpression.patternNode.equals(node)].exists[true]»
						«FOR param : eventPattern.returnStatement.returnParams.filter[param | param instanceof EventPatternNodeAttributeExpression]
						.map[param | param as EventPatternNodeAttributeExpression].filter[param | param.nodeExpression.patternNode.equals(node)]
						.filter[param | !nodeAttributeTypePrimitive(param.nodeExpression.patternNode, param.nodeExpression.attribute)]
						.filter[param | param.field === null]»
						class "«nodeAttributeName(param.nodeExpression.patternNode, param.nodeExpression.attribute)» : «nodeAttributeType(param.nodeExpression.patternNode, param.nodeExpression.attribute)»" <<EventNode>>
						«ENDFOR»
				«ENDIF»
				}
				«ENDFOR»
				«FOR node : eventPattern.nodes.filter[node | node.type instanceof EditorPattern]»
				namespace «node.name» #c9c9c9 {
				«IF contextConstraintNodes.containsKey(node)»
						«FOR constraintNode : contextConstraintNodes.get(node)»
						class "«nodeAttributeName(node, constraintNode)» : «nodeAttributeType(node, constraintNode)»" <<PatternNode>>
						«ENDFOR»
				«ENDIF»
				«IF eventPattern.returnStatement.returnParams
				.filter[param | param instanceof EventPatternNodeAttributeExpression]
				.map[param | param as EventPatternNodeAttributeExpression]
				.filter[param | param.nodeExpression.patternNode.equals(node)].exists[true]»
						«FOR param : eventPattern.returnStatement.returnParams.filter[param | param instanceof EventPatternNodeAttributeExpression]
						.map[param | param as EventPatternNodeAttributeExpression].filter[param | param.nodeExpression.patternNode.equals(node)]
						.filter[param | !nodeAttributeTypePrimitive(param.nodeExpression.patternNode, param.nodeExpression.attribute)]
						.filter[param | param.field === null]»
						class "«nodeAttributeName(param.nodeExpression.patternNode, param.nodeExpression.attribute)» : «nodeAttributeType(param.nodeExpression.patternNode, param.nodeExpression.attribute)»" <<PatternNode>>
						«ENDFOR»
				«ENDIF»
				}
				«ENDFOR»
				
				package Relational_Constraint #fff3d0{
					«formatRelationalConstraint(eventPattern, eventPattern.relationalConstraint).value»
				}
			}
			
			«IF !eventPattern.nodes.filter[node | node.type instanceof EditorPattern].map[node | node.type as EditorPattern].empty»
			namespace Graph_Patterns #c9c9c9{
			«FOR pattern : eventPattern.nodes.filter[node | node.type instanceof EditorPattern].map[node | node.type as EditorPattern].toSet»
«««				«formatPattern(pattern, "Graph_Patterns")»
				«GTPlantUMLGenerator.visualizePattern(pattern, "Graph_Patterns", true)»
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
			"«eventPattern.name».«node.name»" #.[#335bb0].# "Events.«(node.type as Event).name»"
			«ENDFOR»
			«FOR node : eventPattern.nodes.filter[node | node.type instanceof EditorPattern]»
			"«eventPattern.name».«node.name»" #.[#335bb0].# "Graph_Patterns.«(node.type as EditorPattern).name»"
			«ENDFOR»
			
			«FOR contextConstraint : eventPattern.contextConstraints»
			"«eventPattern.name».«contextConstraint.lhs.patternNode.name».«nodeAttributeName(contextConstraint.lhs.patternNode, contextConstraint.lhs.attribute)» : «nodeAttributeType(contextConstraint.lhs.patternNode, contextConstraint.lhs.attribute)
			»" #-[#335bb0]-# "«
			eventPattern.name».«contextConstraint.rhs.patternNode.name».«nodeAttributeName(contextConstraint.rhs.patternNode, contextConstraint.rhs.attribute)» : «nodeAttributeType(contextConstraint.rhs.patternNode, contextConstraint.rhs.attribute)
			»" : =«contextConstraint.relation»
			«ENDFOR»
			
			«IF eventPattern.returnType instanceof ReturnSpawn»
			«formatSpawnType(eventPattern, eventPattern.returnType as ReturnSpawn, eventPattern.returnStatement as SpawnStatement)»
			«ELSE»
			«formatApplyType(eventPattern, eventPattern.returnType as ReturnApply, eventPattern.returnStatement as ApplyStatement)»
			«ENDIF»
			
«««			TODO: Visualize attribute constraints
		'''
	}
	

	
	private static def String fieldSkin(EventAttribute field) {
		if(field.type instanceof EDataType) {
			return "SimpleField"
		} else {
			return "ComplexField" 
		}
	}
	
	private static def String formatEvent(Event event) {
		return '''
			namespace «event.name» #FFFFFF{
				«IF !event.attributes.filter[field | !(field.type instanceof EDataType)].empty»
				«FOR field : event.attributes.filter[field | !(field.type instanceof EDataType)]»
				class "«field.name»: «field.type.name»" <<«fieldSkin(field)»>> {
					«FOR atr : (field.type as EClass).EAllAttributes»
					+ «atr.name»: «atr.EType.name»
					«ENDFOR»
				}
				«ENDFOR»
				«ENDIF»
				«IF !event.attributes.filter[field | (field.type instanceof EDataType)].empty»
				class Primitive-Fields <<SimpleField>>{
				«FOR field : event.attributes.filter[field | (field.type instanceof EDataType)]»
				+ «field.name»: «field.type.name»
				«ENDFOR»
				}
				«ENDIF»
			}
		'''
	}
	
	private static def String formatSpawnType(EventPattern eventPattern, ReturnSpawn spawnType, SpawnStatement statement) {
		val param2Field = new HashMap
		for(var i = 0; i<spawnType.returnType.attributes.size(); i++) {
			param2Field.put(statement.returnParams.get(i), spawnType.returnType.attributes.get(i))
		}
		return '''namespace Spawned_Event #629157 {
			«formatEvent(spawnType.returnType)»
			"«eventPattern.name»" -[#000000]-> "Spawned_Event.«spawnType.returnType.name»" : Spawns 
			«FOR param : statement.returnParams.filter[param | param instanceof EventPatternNodeAttributeExpression]
			.map[param | param as EventPatternNodeAttributeExpression].filter[param | param.field === null]
			.filter[param | !nodeAttributeTypePrimitive(param.nodeExpression.patternNode, param.nodeExpression.attribute)]»
			"«eventPattern.name».«param.nodeExpression.patternNode.name».«nodeAttributeName(param.nodeExpression.patternNode, param.nodeExpression.attribute)» : «nodeAttributeType(param.nodeExpression.patternNode, param.nodeExpression.attribute)
			»" #.[#335bb0].# "Spawned_Event.«spawnType.returnType.name».«param2Field.get(param).name»: «param2Field.get(param).type.name»"
			«ENDFOR»
			«IF statement.returnParams.filter[param | param instanceof EventPatternNodeAttributeExpression]
			.map[param | param as EventPatternNodeAttributeExpression].filter[param | param.field === null]
			.filter[param | nodeAttributeTypePrimitive(param.nodeExpression.patternNode, param.nodeExpression.attribute)].exists[true] || 
			statement.returnParams.filter[param | param instanceof EventPatternNodeAttributeExpression]
			.map[param | param as EventPatternNodeAttributeExpression].filter[param | param.field !== null].exists[true] ||
			statement.returnParams.filter[param | !(param instanceof EventPatternNodeAttributeExpression)].exists[true]»
			class "Field Assignments" <<Conditions>> {
				«FOR param : statement.returnParams.filter[param | param instanceof EventPatternNodeAttributeExpression]
				.map[param | param as EventPatternNodeAttributeExpression].filter[param | param.field === null]
				.filter[param | nodeAttributeTypePrimitive(param.nodeExpression.patternNode, param.nodeExpression.attribute)]»
				+ «param2Field.get(param).name» := «param.nodeExpression.patternNode.name».«nodeAttributeName(param.nodeExpression.patternNode, param.nodeExpression.attribute)»
				«ENDFOR»
				«FOR param : statement.returnParams.filter[param | param instanceof EventPatternNodeAttributeExpression]
				.map[param | param as EventPatternNodeAttributeExpression].filter[param | param.field !== null]»
				+ «param2Field.get(param).name» := «param.nodeExpression.patternNode.name».«nodeAttributeName(param.nodeExpression.patternNode, param.nodeExpression.attribute)».«param.field.name»
				«ENDFOR»
			}
			«ENDIF»
		}'''
	}
	
	private static def String formatApplyType(EventPattern eventPattern, ReturnApply applyType, ApplyStatement statement) {
		return '''namespace Applied_Rule #ffea08 {
«««			«formatPattern(applyType.returnType, "Applied_Rule")»
			«GTPlantUMLGenerator.visualizePattern(applyType.returnType, "Applied_Rule", true)»
«««			TODO: Reevaluate the necessity of an edge that signals apply..
			"«eventPattern.name»" -[#000000]-> "Applied_Rule.«applyType.returnType.name»" : Applies 
			"«eventPattern.name».«statement.match.name»" #.[#335bb0].# "Applied_Rule.«applyType.returnType.name»"
			«FOR param : statement.returnParams»
«««			TODO: visualize parameter dependencies
			
			«ENDFOR»
		}'''
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
	
	private static def boolean nodeAttributeTypePrimitive(EventPatternNode node, EObject atr) {
		if(node.type instanceof Event) {
			val eventAtr = atr as EventAttribute
			return eventAtr.type instanceof EDataType
		}else {
			val patternNode = atr as EditorNode
			return patternNode.type instanceof EDataType
		}
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
				val name = '''«IF constraint.negated»NOT_«ENDIF»Bracket(«operand.key»)'''
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
			"«name»" #.[#000000].# "«eventPattern.name».«name»"
			'''
			val result = new SimpleEntry(name, expr)
			return result
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