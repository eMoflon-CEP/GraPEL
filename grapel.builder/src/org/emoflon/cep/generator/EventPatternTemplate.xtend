package org.emoflon.cep.generator

class EventPatternTemplate {
	
	String eventPatternName;
	
	new(String eventPatternName) {
		this.eventPatternName = eventPatternName;
	}
	
	def String generate() {
		return '''monitor «eventPatternName» {
	constant string eventChannel := "channel1";
	
	action onload() {
		monitor.subscribe(eventChannel);
		
		on all p1Match() as p1 {
			send e1(p1.airport, "found!") to eventChannel;
		}
		
	}
}
'''
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