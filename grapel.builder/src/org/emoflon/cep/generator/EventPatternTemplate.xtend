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

class EventPatternTemplate extends AbstractTemplate{
	
	String eventPatternName;
	EventPattern pattern;
	ModelManager model;
	String contextConstraint = "contextCheck";
	String attributeConstraint = "attributeCheck";
	String sendActionName = "sendAction";
	
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
	
	def String relationalConstraintOp2Apama(RelationalConstraintOperator op) {
		switch(op) {
			case AND: return 'and'
			case OR: return 'or'
			case FOLLOWS: return '->'
		}
	}
	
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
	
	def String getContextConstraintParams(Context context) {
		return '''«FOR param : context.params.map[param | param.name].toSet SEPARATOR ', '»«param»«ENDFOR»'''
	}

	def String getContextConstraint(Context context) {	
		return '''
action «contextConstraint»(«FOR param : context.params.map[param | eventPatternNode2param(param)] SEPARATOR ', '»«param»«ENDFOR») returns boolean {
	return «FOR constraint : context.contextConstraints.map[constraint | contextConstraint2Apama(constraint)] SEPARATOR ' and\n'»«constraint»«ENDFOR»;
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
	
	def String getSendActionParams(SpawnStatement returnStatement) {
		return '''«FOR param : returnStatement.parameters.map[param | arithmeticExpr2Apama(param, true)] SEPARATOR ', '»«param»«ENDFOR»'''
	}

	def String getSendActionParams(ApplyStatement returnStatement) {
		return '''«FOR param : returnStatement.parameters.map[param | arithmeticExpr2Apama(param, true)] SEPARATOR ', '»«param»«ENDFOR»'''
	}
	
	def String getSendAction(SpawnStatement returnStatement) {
		return '''
action «sendActionName»(«FOR param : model.getFields(returnStatement.returnType.name) SEPARATOR ', '»«ModelManager.asApamaType(param)» «param.name»«ENDFOR») {
	send «returnStatement.returnType.name»(«FOR param : model.getFields(returnStatement.returnType.name) SEPARATOR ', '»«param.name»«ENDFOR») to eventChannel;
}
'''		
	}
	
//	TODO:!
	def String getSendAction(ApplyStatement returnStatement) {
		return '''
action «sendActionName»(«FOR param : model.getApplicationEventFields(returnStatement.returnType.name) SEPARATOR ', '»«ModelManager.asApamaType(param)» «param.name»«ENDFOR») {
	send «returnStatement.returnType.name»Application(«FOR param : model.getApplicationEventFields(returnStatement.returnType.name) SEPARATOR ', '»«param.name»«ENDFOR») to eventChannel;
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
	
	def String attributeConstrExpr2Apama(AttributeConstraintExpression expr) {
		return '''«arithmeticExpr2Apama(expr.lhs, false)»«IF expr.rhs !== null» «attributeConstraintRelation2Apama(expr.op)» «arithmeticExpr2Apama(expr.rhs, false)»«ENDIF»'''
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
	
	def String arithmeticExpr2Apama(ArithmeticExpression expr, boolean isAssignment) {
		if(expr.requiresCast && expr instanceof ArithmeticExpressionProduction)
			return '''«castTo('''(«arithmeticExpr2ApamaInternal(expr, isAssignment)»)''', expr.type, expr.castTo, isAssignment)»'''
		else if(expr.requiresCast && (expr instanceof ArithmeticExpressionLiteral || expr instanceof ArithmeticExpressionUnary))
			return '''«castTo(arithmeticExpr2ApamaInternal(expr, isAssignment), expr.type, expr.castTo, isAssignment)»'''
		else return arithmeticExpr2ApamaInternal(expr, isAssignment)
	}
	
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
	
	def String unaryExpression2Apama(ArithmeticExpressionUnary expr, boolean isAssignment) {
		if(expr.operator == ArithmeticExpressionUnaryOperator.BRACKETS)
			return '''«IF expr.isNegative»-«ENDIF»(«arithmeticExpr2Apama(expr.operand, isAssignment)»)'''
		else
			return '''«IF expr.isNegative»-«ENDIF»(«arithmeticExpr2Apama(expr.operand, isAssignment)»).«arithmeticUnaryOp2Apama(expr.operator)»'''
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
	
	def String arithmeticValExpr2Apama(ArithmeticValueExpression expr) {
		if(expr.attributeExpression === null) {
			return nodeExpression2Apama(expr.nodeExpression)
		} else {
			return  '''«expr.nodeExpression.eventPatternNode.name».«attributeExpression2Apama(expr.attributeExpression)»'''
		}
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
			return literal.virtualAttribute.name
		}else {
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