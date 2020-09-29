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
import GrapeLModel.ReturnStatement
import GrapeLModel.RelationalConstraintLiteral
import GrapeLModel.RelationalExpression
import GrapeLModel.RelationalConstraintProduction
import GrapeLModel.RelationalConstraintOperator
import GrapeLModel.RelationalExpressionLiteral
import GrapeLModel.RelationalExpressionProduction
import GrapeLModel.RelationalExpressionOperator
import GrapeLModel.EventNodeExpression
import GrapeLModel.IBeXPatternNodeExpression

class EventPatternTemplate extends AbstractTemplate{
	
	String eventPatternName;
	EventPattern pattern;
	String contextConstraint = "contextCheck";
	String attributeConstraint = "attributeCheck";
	String sendActionName = "sendAction";
	
	new(String eventPatternName, ImportManager imports, NSManager names, PathManager paths, ModelManager model) {
		super(imports, names, paths)
		this.eventPatternName = eventPatternName;
		this.pattern = model.getEventPattern(eventPatternName);
	}
	
	override String generate() {
		return '''monitor «eventPatternName» {
	constant string eventChannel := "channel1";
	
	action onload() {
		monitor.subscribe(eventChannel);
		
		on all «getRelationalConstraint(pattern.relationalConstraint)» {
			«getRelationalConstraintBody(pattern.context, pattern.attributeConstraint)»
		}
		
	}
	
	«IF pattern.context !== null»«getContextConstraint(pattern.context)»«ENDIF»
					
	«IF pattern.attributeConstraint !== null»«getAttributeConstraint(pattern.attributeConstraint)»«ENDIF»
			
	«getSendAction(pattern.returnStatement)»
}
'''
	}
	
	def String getRelationalConstraint(RelationalConstraint constraint) {
		if(constraint instanceof RelationalConstraintLiteral) {
			val literal = constraint as RelationalConstraintLiteral;
			return relationalExpr2Apama(literal.relationalExpression)
		} else {
			val production = constraint as RelationalConstraintProduction
			return '''«getRelationalConstraint(constraint)» «relationalConstraintOp2Apama(production.op)» «getRelationalConstraint(constraint)»'''
		}
	}
	
	def String relationalConstraintOp2Apama(RelationalConstraintOperator op) {
		switch(op) {
			case AND: {
				return "and"
			}
			case OR: {
				return "or"
			}
			
		}
	}
	
	def String relationalExpr2Apama(RelationalExpression expr) {
		if(expr instanceof RelationalExpressionLiteral) {
			val literal = expr as RelationalExpressionLiteral;
			if(literal.eventPatternNode instanceof EventNode) {
				val node = literal.eventPatternNode as EventNode;
				return '''«node.type.name»() as «node.name»'''
			}else {
				val node = literal.eventPatternNode as IBeXPatternNode;
				return '''«node.type.name»() as «node.name»'''
			}
		} else {
			val production = expr as RelationalExpressionProduction
			return '''«relationalExpr2Apama(production.lhs)» «relationalExprOp2Apama(production.op)» «relationalExpr2Apama(production.rhs)»'''
		}
	}
	
	def String relationalExprOp2Apama(RelationalExpressionOperator op) {
		switch(op) {
			case AND: {
				return "&&"
			}
			case FOLLOWS: {
				return "->"
			}
			case OR: {
				return "||"
			}
			
		}
	}
	
	def String getRelationalConstraintBody(Context context, AttributeConstraint constraint) {
		if(context === null && constraint === null) {
			return '''«sendActionName»(«getSendActionParams(pattern.returnStatement)»);'''
		} else {
			return '''if(«IF context !== null»«contextConstraint»(«getContextConstraintParams(context)»)«ENDIF»«IF context !== null && constraint !== null» and «ENDIF»«IF constraint !== null»«attributeConstraint»(«getAttributeConstraintParams(constraint)»)«ENDIF») {
	«sendActionName»(«getSendActionParams(pattern.returnStatement)»);
}'''
		}
		
	}
	
	def String getContextConstraintParams(Context context) {
		return '''«FOR param : context.params.map[param | param.name].toSet SEPARATOR ', '»«param»«ENDFOR»'''
	}

	def String getContextConstraint(Context context) {	
		return '''
action «contextConstraint»(«FOR param : context.params.map[param | eventPatternNode2param(param)] SEPARATOR ', '»«param»«ENDFOR») returns boolean {
	return «FOR constraint : context.contextConstraints.map[constraint | contextConstraint2Apama(constraint)] SEPARATOR ' AND\n'»«constraint»«ENDFOR»;
}
'''
	}
	
	def String getAttributeConstraintParams(AttributeConstraint constraint) {
		return '''«FOR param : constraint.params.map[param | param.name].toSet SEPARATOR ', '»«param»«ENDFOR»'''
	}
	
	def String getAttributeConstraint(AttributeConstraint constraint) {
		return '''
action «attributeConstraint»(«FOR param : constraint.params.map[param | eventPatternNode2param(param)] SEPARATOR ', '»«param»«ENDFOR») returns boolean {
	return «attributeConstraint2Apama(constraint)»;
}
'''
	}
	
	def String getSendActionParams(ReturnStatement returnStatement) {
		return '''«FOR param : returnStatement.parameters.flatMap[param | param.params].map[param | param.name].toSet SEPARATOR ', '»«param»«ENDFOR»'''
	}
	
	def String getSendAction(ReturnStatement returnStatement) {
		return '''
action «sendActionName»(«FOR param : returnStatement.parameters.flatMap[param | param.params].map[param | eventPatternNode2param(param)].toSet SEPARATOR ', '»«param»«ENDFOR») {
	send «returnStatement.returnType.name»(«FOR param : returnStatement.parameters.map[param | arithmeticExpr2Apama(param)].toSet SEPARATOR ', '»«param»«ENDFOR») to eventChannel;
}
'''		
	}
	
	def String contextConstraint2Apama(ContextConstraint constraint) {
		if(!(constraint instanceof NodeContextConstraint)) {
			return "";
		}else {
			val nodeContext = constraint as NodeContextConstraint
			return '''«nodeExpression2Apama(nodeContext.lhs)» «contextRelation2Apama(constraint.relation)» «nodeExpression2Apama(nodeContext.rhs)»'''
		}
			
	}
	
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
	
	def String attributeConstraint2Apama(AttributeConstraint constraint) {
		if(constraint instanceof AttributeConstraintLiteral) {
			val literal = constraint as AttributeConstraintLiteral;
			return attributeConstrExpr2Apama(literal.constraintExpression)
		}else {
			val production = constraint as AttributeConstraintProduction
			return '''«attributeConstraint2Apama(production.lhs)» «attributeConstrOp2Apama(production.op)» «attributeConstraint2Apama(production.rhs)»'''
		}
	}
	
	def String attributeConstrOp2Apama(AttributeConstraintOperator op) {
		switch(op) {
			case AND: {
				return "&&"
			}
			case OR: {
				return "||"
			}
			
		}
	}
	
	def String attributeConstrExpr2Apama(AttributeConstraintExpression expr) {
		return '''«arithmeticExpr2Apama(expr.lhs)» «attributeConstraintRelation2Apama(expr.op)» «arithmeticExpr2Apama(expr.rhs)»'''
	}
	
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
	
	def String arithmeticExpr2Apama(ArithmeticExpression expr) {
		if(expr instanceof ArithmeticExpressionLiteral) {
			val literal = expr as ArithmeticExpressionLiteral;
			return arithmeticVal2Apama(literal.value)
		}else {
			val production = expr as ArithmeticExpressionProduction;
			return arithmeticExpr2Apama(production.lhs) + " "+ arithmeticOp2Apama(production.op) + " " + arithmeticExpr2Apama(production.rhs)
		}
	}
	
	def String eventPatternNode2param(EventPatternNode eventPatternNode) {
		if(eventPatternNode instanceof EventNode) {
			val node = eventPatternNode as EventNode
			return '''«node.type.name» «node.name»'''
		} else {
			val node = eventPatternNode as IBeXPatternNode
			return '''«node.type.name» «node.name»'''
		}
	}

	
	def String arithmeticOp2Apama(ArithmeticExpressionOperator op) {
		switch(op) {
			case DIVIDE: {
				return "/"
			}
			case MINUS: {
				return "-"
			}
			case MULTIPLY: {
				return "*"
			}
			case PLUS: {
				return "+"
			}
			
		}
	}
	
	def String arithmeticVal2Apama(ArithmeticValue value) {
		if(value instanceof ArithmeticValueLiteral) {
			val literal = value as ArithmeticValueLiteral
			if(literal instanceof IntegerLiteral) {
				val integer = literal as IntegerLiteral
				return integer.value+""
			}else if(literal instanceof DoubleLiteral) {
				val longfloat = literal as DoubleLiteral
				return longfloat.value+""
			}else if(literal instanceof StringLiteral) {
				val str = literal as StringLiteral
				return '''"«str.value»"'''
			}else {
				val bool = literal as BooleanLiteral
				return (bool.value)?"true":"false"
			}
		}else {
			val expr = value as ArithmeticValueExpression;
			return arithmeticValExpr2Apama(expr)
		}
	}
	
	def String arithmeticValExpr2Apama(ArithmeticValueExpression expr) {
		if(expr.attributeExpression === null) {
			return nodeExpression2Apama(expr.nodeExpression)
		} else {
			return  '''«expr.nodeExpression.eventPatternNode.name».«attributeExpression2Apama(expr.attributeExpression)»'''
		}
//		return '''«nodeExpression2Apama(expr.nodeExpression)»«IF expr.attributeExpression !== null».«attributeExpression2Apama(expr.attributeExpression)»«ENDIF»'''
	}
	
	def String nodeExpression2Apama(EventPatternNodeExpression expr) {
		if(expr instanceof EventNodeExpression) {
			val enExpr = expr as EventNodeExpression
			return nodeExpression2Apama(enExpr)
		} else {
			val ptExpr = expr as IBeXPatternNodeExpression
			return nodeExpression2Apama(ptExpr)
		}
	}
	
	def String nodeExpression2Apama(EventNodeExpression expr) {
		return '''«expr.eventPatternNode.name»«IF expr.eventAttribute !== null».«expr.eventAttribute.name»«ENDIF»'''
	}
	
	def String nodeExpression2Apama(IBeXPatternNodeExpression expr) {
		return '''«expr.eventPatternNode.name»«IF expr.patternAttribute !== null».«expr.patternAttribute.name»«ENDIF»'''
	}
	
	def String attributeExpression2Apama(AttributeExpression expr) {
		if(expr instanceof AttributeExpressionLiteral) {
			val literal = expr as  AttributeExpressionLiteral
//			return literal.attribute.name
			return literal.virtualAttribute.name
		}else {
//			val production = expr as AttributeExpressionProduction
//			return production.attribute.name + "." + attributeExpression2Apama(production.child)
			throw new RuntimeException("Nested attribute expressions not yet supported!");
		}
	}
	
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