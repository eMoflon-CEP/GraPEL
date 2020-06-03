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
import GrapeLModel.AttributeExpressionProduction
import java.util.Map
import GrapeLModel.AttributeConstraint
import java.util.HashMap
import GrapeLModel.EventNode
import java.util.Collection
import GrapeLModel.EventPatternNode
import GrapeLModel.IBeXPatternNode

class EventPatternTemplate {
	
	String eventPatternName;
	ModelManager model;
	EventPattern pattern;
	Map<AttributeConstraint, String> checkActions = new HashMap
	
	new(String eventPatternName, ModelManager model) {
		this.eventPatternName = eventPatternName;
		this.model = model;
		this.pattern = model.getEventPattern(eventPatternName);
	}
	
	def String generate() {
		return '''monitor «eventPatternName» {
	constant string eventChannel := "channel1";
	
	action onload() {
		monitor.subscribe(eventChannel);
		
		on all p1Match() as p1 {
			send e1(p1.airport, "found!") to eventChannel;
		}
		
		«sendAction»
	}
}
'''
	}
	
	def String getCheckAction(AttributeConstraint constraint) {
		val name = "checkAction"+checkActions.size+1
return '''
action «name»(«FOR param : constraint.params SEPARATOR ', '»«eventPatternNode2param(param)»«ENDFOR») returns boolean {
	return true;
}
'''
	}
	
	def String getSendAction() {
		val returnStatement = pattern.returnStatement;
return '''
action sendAction(«FOR param : returnStatement.parameters.flatMap[param | param.params] SEPARATOR ', '»«eventPatternNode2param(param)»«ENDFOR») {
	send «returnStatement.returnType.name»(«FOR param : returnStatement.parameters SEPARATOR ', '»«arithmeticExpr2Apama(param)»«ENDFOR») to eventChannel;
}
'''		
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
				return "-"
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
				return str.value
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
		return nodeExpression2Apama(expr.nodeExpression)+"."+attributeExpression2Apama(expr.attributeExpression)
	}
	
	def String nodeExpression2Apama(EventPatternNodeExpression expr) {
		return expr.eventPatternNode.name
	}
	
	def String attributeExpression2Apama(AttributeExpression expr) {
		if(expr instanceof AttributeExpressionLiteral) {
			val literal = expr as  AttributeExpressionLiteral
			return literal.attribute.name
		}else {
			val production = expr as AttributeExpressionProduction
			return production.attribute.name + "." + attributeExpression2Apama(production.child)
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
}