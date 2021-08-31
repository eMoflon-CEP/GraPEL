package org.emoflon.cep.generator

import GrapeLModel.EventPattern
import GrapeLModel.ArithmeticExpression
import GrapeLModel.ArithmeticExpressionLiteral
import GrapeLModel.ArithmeticExpressionProduction
import GrapeLModel.ArithmeticExpressionOperator
import GrapeLModel.ArithmeticValue
import GrapeLModel.ArithmeticValueLiteral
import GrapeLModel.IntegerLiteral
import GrapeLModel.DoubleLiteral
import GrapeLModel.StringLiteral
import GrapeLModel.BooleanLiteral
import GrapeLModel.ArithmeticValueExpression
import GrapeLModel.EventPatternNodeExpression
import GrapeLModel.AttributeExpression
import GrapeLModel.AttributeExpressionLiteral
import GrapeLModel.AttributeConstraint
import GrapeLModel.EventNode
import GrapeLModel.EventPatternNode
import GrapeLModel.IBeXPatternNode
import GrapeLModel.AttributeConstraintLiteral
import GrapeLModel.AttributeConstraintExpression
import GrapeLModel.AttributeConstraintProduction
import GrapeLModel.AttributeConstraintOperator
import GrapeLModel.AttributeConstraintRelation
import GrapeLModel.Context
import GrapeLModel.ContextConstraint
import GrapeLModel.NodeContextConstraint
import GrapeLModel.ContextRelation
import GrapeLModel.RelationalConstraint
import GrapeLModel.RelationalConstraintLiteral
import GrapeLModel.RelationalConstraintProduction
import GrapeLModel.RelationalConstraintOperator
import GrapeLModel.EventNodeExpression
import GrapeLModel.IBeXPatternNodeExpression
import GrapeLModel.ArithmeticExpressionUnary
import GrapeLModel.ArithmeticExpressionUnaryOperator
import GrapeLModel.MatchVanishedConstraint
import GrapeLModel.AttributeConstraintUnary
import GrapeLModel.AttributeConstraintUnaryOperator
import org.eclipse.emf.ecore.EcorePackage
import GrapeLModel.RelationalConstraintUnary
import GrapeLModel.SpawnStatement
import GrapeLModel.ApplyStatement
import org.eclipse.emf.ecore.EClassifier
import GrapeLModel.EnumLiteral

/**
 * Template for GrapeL Apama event pattern generation
 */
class EventPatternTemplate extends AbstractTemplate{
	
	/**
	 * Name of the event pattern
	 */
	String eventPatternName;
	/**
	 * GrapeL event pattern representation
	 */
	EventPattern pattern;
	/**
	 * Model manager utility for GrapeL model
	 */
	ModelManager model;

	// Constant string elements
	String contextConstraint = "contextCheck";
	String attributeConstraint = "attributeCheck";
	String sendActionName = "sendAction";
	
	/**
	 * Constructor for an event pattern template
	 * @param eventPatternName the name of the event pattern
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 * @param model the manager that includes the utility for model access
	 */
	new(String eventPatternName, ImportManager imports, NSManager names, PathManager paths, ModelManager model) {
		super(imports, names, paths)
		this.eventPatternName = eventPatternName
		this.pattern = model.getEventPattern(eventPatternName)
		this.model = model
	}
	
	override String generate() {
		return '''monitor «eventPatternName» {
	constant string eventChannel := "channel1";
	
	action onload() {
		monitor.subscribe(eventChannel);
		
		on «getRelationalConstraint(pattern.relationalConstraint)» {
			«getRelationalConstraintBody(pattern.context, pattern.attributeConstraint)»
		}
		
	}
	
	«IF pattern.context !== null»«getContextConstraint(pattern.context)»«ENDIF»
					
	«IF pattern.attributeConstraint !== null»«getAttributeConstraint(pattern.attributeConstraint)»«ENDIF»
			
	«if(pattern.returnStatement instanceof SpawnStatement) getSendAction(pattern.returnStatement as SpawnStatement) 
	else getSendAction(pattern.returnStatement as ApplyStatement)»
}
'''
	}
	
	/**
	 * @param constraint to be transformed to EPL code
	 * @return Apama EPL code for the the relation constraint
	 */
	def String getRelationalConstraint(RelationalConstraint constraint) {
		if(constraint instanceof RelationalConstraintLiteral) {
			val literal = constraint as RelationalConstraintLiteral;
			if(literal.eventPatternNode instanceof EventNode) {
				val node = literal.eventPatternNode as EventNode;
				return '''«node.type.name»() as «node.name»'''
			}else {
				val node = literal.eventPatternNode as IBeXPatternNode;
				return '''«node.type.name»() as «node.name»'''
			}
		} else if(constraint instanceof RelationalConstraintUnary) {
			switch(constraint.operator) {
				case ALL: return '''all «getRelationalConstraint(constraint.operand)»'''
				case BRACKET: return '''(«getRelationalConstraint(constraint.operand)»)'''
				case NOT: return ''' not «getRelationalConstraint(constraint.operand)»'''
			}
		} else {
			val production = constraint as RelationalConstraintProduction
			return '''«getRelationalConstraint(production.lhs)» «relationalConstraintOp2Apama(production.op)» «getRelationalConstraint(production.rhs)»'''
		}
	}
	
	/**
	 * @param op specifying the relational constraint operation
	 * @return the Apama EPL equivalent for the relational constraint operator
	 */
	def String relationalConstraintOp2Apama(RelationalConstraintOperator op) {
		switch(op) {
			case AND: return 'and'
			case OR: return 'or'
			case FOLLOWS: return '->'
		}
	}
	
	/**
	 * @param context for the event pattern
	 * @param constraint the attribute constraint which should be nested in the body
	 * @return the Apama EPL code for the relational constraint body
	 */
	def String getRelationalConstraintBody(Context context, AttributeConstraint constraint) {
		if(context === null && constraint === null) {
			return '''«sendActionName»(«if(pattern.returnStatement instanceof SpawnStatement) getSendActionParams(pattern.returnStatement as SpawnStatement) 
			else getSendActionParams(pattern.returnStatement as ApplyStatement)»);'''
		} else {
			return '''if(«IF context !== null»«contextConstraint»(«getContextConstraintParams(context)»)«ENDIF»«IF context !== null && constraint !== null» and «ENDIF»«IF constraint !== null»«attributeConstraint»(«getAttributeConstraintParams(constraint)»)«ENDIF») {
	«sendActionName»(«if(pattern.returnStatement instanceof SpawnStatement) getSendActionParams(pattern.returnStatement as SpawnStatement) 
	else getSendActionParams(pattern.returnStatement as ApplyStatement)»);
}'''
		}
		
	}
	
	/**
	 * @param context of the event pattern
	 * @return the context constraint parameters divided by a comma as a separator
	 */
	def String getContextConstraintParams(Context context) {
		return '''«FOR param : context.params.map[param | param.name].toSet SEPARATOR ', '»«param»«ENDFOR»'''
	}
	
	/**
	 * @param context of the event pattern
	 * @return the Apama EPL code for the contextCheck action
	 */
	def String getContextConstraint(Context context) {	
		return '''
action «contextConstraint»(«FOR param : context.params.map[param | eventPatternNode2param(param)] SEPARATOR ', '»«param»«ENDFOR») returns boolean {
	return «FOR constraint : context.contextConstraints.map[constraint | contextConstraint2Apama(constraint)] SEPARATOR ' and\n'»«constraint»«ENDFOR»;
}
'''
	}
	
	/**
	 * @param constraint the attribute constraint to generate the parameter list for
	 * @return the attribute constraint parameters divided by a comma as a separator
	 */
	def String getAttributeConstraintParams(AttributeConstraint constraint) {
		return '''«FOR param : constraint.params.map[param | param.name].toSet SEPARATOR ', '»«param»«ENDFOR»'''
	}
	
	/**
	 * @param constraint the attribute constraint
	 * @return the Apama EPL code for the attributeCheck action
	 */
	def String getAttributeConstraint(AttributeConstraint constraint) {
		return '''
action «attributeConstraint»(«FOR param : constraint.params.map[param | eventPatternNode2param(param)] SEPARATOR ', '»«param»«ENDFOR») returns boolean {
	return «attributeConstraint2Apama(constraint)»;
}
'''
	}
	
	/**
	 * @param returnStatement the return statement of the event pattern
	 * @return the send action parameters for the return type pattern divided by a comma as a separator
	 */
	def String getSendActionParams(SpawnStatement returnStatement) {
		return '''«FOR param : returnStatement.parameters.map[param | arithmeticExpr2Apama(param, true)] SEPARATOR ', '»«param»«ENDFOR»'''
	}
	
	/**
	 * @param returnStatement the apply statement of the event pattern
	 * @return the send action parameters for the apply type pattern divided by a comma as a separator
	 */
	def String getSendActionParams(ApplyStatement returnStatement) {
		return '''«FOR param : returnStatement.parameters.map[param | arithmeticExpr2Apama(param, true)] SEPARATOR ', '»«param»«ENDFOR»'''
	}
	
	/**
	 * @param returnStatement the return statement of the event pattern
	 * @return the Apama EPL code for the sendAction of the return type pattern
	 */
	def String getSendAction(SpawnStatement returnStatement) {
		return '''
action «sendActionName»(«FOR param : model.getFields(returnStatement.returnType.name) SEPARATOR ', '»«ModelManager.asApamaType(param)» «param.name»«ENDFOR») {
	send «returnStatement.returnType.name»(«FOR param : model.getFields(returnStatement.returnType.name) SEPARATOR ', '»«param.name»«ENDFOR») to eventChannel;
}
'''		
	}
	
//	TODO:!
	/**
	 * @param returnStatement the apply statement of the event pattern
	 * @return the Apama EPL code for the sendAction of the apply type pattern
	 */
	def String getSendAction(ApplyStatement returnStatement) {
		return '''
action «sendActionName»(«FOR param : model.getApplicationEventFields(returnStatement.returnType.name) SEPARATOR ', '»«ModelManager.asApamaType(param)» «param.name»«ENDFOR») {
	send «returnStatement.returnType.name»Application(«FOR param : model.getApplicationEventFields(returnStatement.returnType.name) SEPARATOR ', '»«param.name»«ENDFOR») to eventChannel;
}
'''		
	}
	
	/**
	 * @param constraint the context constraint of the event pattern
	 * @return the Apama EPL code for the context constraint
	 */
	def String contextConstraint2Apama(ContextConstraint constraint) {
		if(!(constraint instanceof NodeContextConstraint)) {
			return "";
		}else {
			val nodeContext = constraint as NodeContextConstraint
			return '''«nodeExpression2Apama(nodeContext.lhs)» «contextRelation2Apama(constraint.relation)» «nodeExpression2Apama(nodeContext.rhs)»'''
		}
			
	}
	
	/**
	 * @param op specifying the context relation
	 * @return the Apama EPL equivalent for the context relation
	 */
	def String contextRelation2Apama(ContextRelation op) {
		switch(op) {
			case EQUAL: {
				return "="
			}
			case UNEQUAL: {
				return "!="
			}
			
		}
	}
	
	/**
	 * @param constraint the attribute constraint
	 * @return the Apama EPL code for the attribute constraint
	 */
	def String attributeConstraint2Apama(AttributeConstraint constraint) {
		if(constraint instanceof AttributeConstraintLiteral) {
			val literal = constraint as AttributeConstraintLiteral;
			return attributeConstrExpr2Apama(literal.constraintExpression)
		} else if(constraint instanceof MatchVanishedConstraint) {
			val mvc = constraint as MatchVanishedConstraint
			return '''«mvc.eventPatternNode.name».vanished'''
		} else if(constraint instanceof AttributeConstraintUnary) {
			val acu = constraint as AttributeConstraintUnary
			return'''«IF acu.operator == AttributeConstraintUnaryOperator.NOT»not «attributeConstraint2Apama(acu.operand)»«ELSE»(«attributeConstraint2Apama(acu.operand)»)«ENDIF»'''
		}
		else {
			val production = constraint as AttributeConstraintProduction
			return '''«attributeConstraint2Apama(production.lhs)» «attributeConstrOp2Apama(production.op)» «attributeConstraint2Apama(production.rhs)»'''
		}
	}
	
	/**
	 * @param op specifying the attribute constraint operator
	 * @return the Apama EPL equivalent for the attribute constraint operator
	 */
	def String attributeConstrOp2Apama(AttributeConstraintOperator op) {
		switch(op) {
			case AND: {
				return 'and'
			}
			case OR: {
				return 'or'
			}
			
		}
	}
	
	/**
	 * @param expr the attribute expression
	 * @return the Apama EPL code for the attribute expression
	 */
	def String attributeConstrExpr2Apama(AttributeConstraintExpression expr) {
		return '''«arithmeticExpr2Apama(expr.lhs, false)»«IF expr.rhs !== null» «attributeConstraintRelation2Apama(expr.op)» «arithmeticExpr2Apama(expr.rhs, false)»«ENDIF»'''
	}
	
	/**
	 * @param op specifying the attribute relation
	 * @return the Apama EPL equivalent for the attribute relation
	 */
	def String attributeConstraintRelation2Apama(AttributeConstraintRelation op) {
		switch(op) {
			case EQUAL: {
				return "="
			}
			case GREATER: {
				return ">"
			}
			case GREATER_OR_EQUAL: {
				return ">="
			}
			case SMALLER: {
				return "<"
			}
			case SMALLER_OR_EQUAL: {
				return "<="
			}
			case UNEQUAL: {
				return "!="
			}
			
		}
	}
	
	/**
	 * This method is only an internal method for arithmetic expression to Apama after check if cast is required after conversion
	 * @param expr the arithmetic expression
	 * @param isAssigment indicating if the expression is an assignment
	 * @return the Apama EPL code for the arithmetic expression
	 */
	def String arithmeticExpr2ApamaInternal(ArithmeticExpression expr, boolean isAssignment) {
		if(expr instanceof ArithmeticExpressionLiteral) {
			val literal = expr as ArithmeticExpressionLiteral
				return  arithmeticVal2Apama(literal.value)
		}else if(expr instanceof ArithmeticExpressionProduction){
			val production = expr as ArithmeticExpressionProduction
			return '''«arithmeticExpr2Apama(production.lhs, isAssignment)» «arithmeticOp2Apama(production.op)» «arithmeticExpr2Apama(production.rhs, isAssignment)»«IF production.op == ArithmeticExpressionOperator.EXPONENTIATE»)«ENDIF»'''
		} else {
			val unary = expr as ArithmeticExpressionUnary
			return unaryExpression2Apama(unary, isAssignment);
		}
	}
	
	/**
	 * @param expr the arithmetic expression
	 * @param isAssigment indicating if the expression is an assignment
	 * @return the Apama EPL code for the arithmetic expression
	 */
	def String arithmeticExpr2Apama(ArithmeticExpression expr, boolean isAssignment) {
		if(expr.requiresCast && expr instanceof ArithmeticExpressionProduction)
			return '''«castTo('''(«arithmeticExpr2ApamaInternal(expr, isAssignment)»)''', expr.type, expr.castTo, isAssignment)»'''
		else if(expr.requiresCast && (expr instanceof ArithmeticExpressionLiteral || expr instanceof ArithmeticExpressionUnary))
			return '''«castTo(arithmeticExpr2ApamaInternal(expr, isAssignment), expr.type, expr.castTo, isAssignment)»'''
		else return arithmeticExpr2ApamaInternal(expr, isAssignment)
	}
	
	/**
	 * @param expr to be type casted
	 * @param from the type the expression has before cats
	 * @param to the type the expression should have after casting
	 * @param isAssignment indicating if the expression is an assignment
	 * @return the Apama EPL code for type casting the expression
	 */
	def String castTo(String expr, EClassifier from, EClassifier to, boolean isAssignment) {
		if(isAssignment && from == EcorePackage.Literals.ESTRING && to != EcorePackage.Literals.ESTRING) {
			return '''«ModelManager.dataTypeAsApamaType(to)».parse(«expr»)'''
		}else {
			if((from == EcorePackage.Literals.EDOUBLE || from == EcorePackage.Literals.EFLOAT) && (to == EcorePackage.Literals.EINT || to == EcorePackage.Literals.ELONG)) {
				return '''«expr».floor()'''
			} else {
				return '''«expr».to«ModelManager.dataTypeAsApamaType(to).toFirstUpper»()'''
			}
		}
	}
	
	/**
	 * @param expr the unary expression
	 * @param isAssigment indicating if the expression is an assignment
	 * @return the Apama EPL code for the unary expression
	 */
	def String unaryExpression2Apama(ArithmeticExpressionUnary expr, boolean isAssignment) {
		if(expr.operator == ArithmeticExpressionUnaryOperator.BRACKETS)
			return '''«IF expr.isNegative»-«ENDIF»(«arithmeticExpr2Apama(expr.operand, isAssignment)»)'''
		else
			return '''«IF expr.isNegative»-«ENDIF»(«arithmeticExpr2Apama(expr.operand, isAssignment)»).«arithmeticUnaryOp2Apama(expr.operator)»'''
	}
	
	/**
	 * @param eventPatternNode node to be mapped with its type
	 * @return the Apama EPL code to represent a event pattern node with its type
	 */
	def String eventPatternNode2param(EventPatternNode eventPatternNode) {
		if(eventPatternNode instanceof EventNode) {
			val node = eventPatternNode as EventNode
			return '''«node.type.name» «node.name»'''
		} else {
			val node = eventPatternNode as IBeXPatternNode
			return '''«node.type.name» «node.name»'''
		}
	}

	/**
	 * @param op the arithmetic expression operator
	 * @return the Apama EPL equivalent for the arithmetic expression operator
	 */
	def String arithmeticOp2Apama(ArithmeticExpressionOperator op) {
		switch(op) {
			case DIVIDE: {
				return '/'
			}
			case SUBTRACT: {
				return '-'
			}
			case MULTIPLY: {
				return '*'
			}
			case ADD: {
				return '+'
			}
			case EXPONENTIATE: {
				return '.pow('
			}
			
		}
	}
	
	/**
	 * @param op the unary arithmetic expression operator
	 * @return the Apama EPL equivalent for the arithmetic unary expression operator
	 */
	def String arithmeticUnaryOp2Apama(ArithmeticExpressionUnaryOperator op) {
		switch(op) {
			case ABS: {
				return 'abs()'
			}
			case BRACKETS: {
				throw new RuntimeException("Brackets are not a proper operator.");
			}
			case COS: {
				return 'cos()'
			}
			case SIN: {
				return 'sin()'
			}
			case SQRT: {
				return 'sqrt()'
			}
		}
	}
	
	/**
	 * @param value the arithmetic value to be converted to a literal
	 * @return the arithmetic value as a literal string 
	 */
	def String arithmeticVal2Apama(ArithmeticValue value) {
		if(value instanceof ArithmeticValueLiteral) {
			val literal = value as ArithmeticValueLiteral
			if(literal instanceof IntegerLiteral) {
				return literal.value+""
			}else if(literal instanceof DoubleLiteral) {
				return literal.value+""
			}else if(literal instanceof StringLiteral) {
				return '''"«literal.value»"'''
			}else if(literal instanceof EnumLiteral) {
				return '''"«literal.fqInstanceName»"'''
			}else {
				val bool = literal as BooleanLiteral
				return (bool.value)?"true":"false"
			}
		}else {
			val expr = value as ArithmeticValueExpression;
				return arithmeticValExpr2Apama(expr)
		}
	}
	
	/**
	 * @param expr the arithmetic value expression
	 * @return the Apama EPL code to represent the arithmetic value expression
	 */
	def String arithmeticValExpr2Apama(ArithmeticValueExpression expr) {
		if(expr.attributeExpression === null) {
			return nodeExpression2Apama(expr.nodeExpression)
		} else {
			return  '''«expr.nodeExpression.eventPatternNode.name».«attributeExpression2Apama(expr.attributeExpression)»'''
		}
	}
	
	/**
	 * @param expr the event pattern node expression to be converted
	 * @return the Apama EPL code to represent the event pattern node expression
	 */
	def String nodeExpression2Apama(EventPatternNodeExpression expr) {
		if(expr instanceof EventNodeExpression) {
			val enExpr = expr as EventNodeExpression
			return nodeExpression2Apama(enExpr)
		} else {
			val ptExpr = expr as IBeXPatternNodeExpression
			return nodeExpression2Apama(ptExpr)
		}
	}
	
	/**
	 * @param expr the event node expression to be converted
	 * @return the Apama EPL code to represent the event node expression
	 */
	def String nodeExpression2Apama(EventNodeExpression expr) {
		return '''«expr.eventPatternNode.name»«IF expr.eventAttribute !== null».«expr.eventAttribute.name»«ENDIF»'''
	}
	
	/**
	 * @param expr the IBEX pattern node expression to be converted
	 * @return the Apama EPL code to represent the IBEX pattern node expression
	 */
	def String nodeExpression2Apama(IBeXPatternNodeExpression expr) {
		return '''«expr.eventPatternNode.name»«IF expr.patternAttribute !== null».«expr.patternAttribute.name»«ENDIF»'''
	}
	
	/**
	 * @param expr the attribute expression to be converted
	 * @return the Apama EPL code to represent the attribute expression
	 * @throws RuntimeException if the attribute expression is not supported
	 */
	def String attributeExpression2Apama(AttributeExpression expr) {
		if(expr instanceof AttributeExpressionLiteral) {
			val literal = expr as  AttributeExpressionLiteral
			return literal.virtualAttribute.name
		}else {
			throw new RuntimeException("Nested attribute expressions not yet supported!");
		}
	}
	
	/**
	 * Returns the code for the Maintainance monitor synchronization monitor
	 */
	static def String getSyncPattern() {
				return '''monitor Maintainance {
	constant string eventChannel := "channel1";
	
	action onload() {
		monitor.subscribe(eventChannel);
		
		on all RequestSynchronizationEvent() as request  {
			send UpdateEvent(request.id) to eventChannel;
		}	
	}
}
'''
	}
	
	override getPath() {
		paths.getEventPatternMonitorLocation(eventPatternName)
	}
	
}