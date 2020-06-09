package GrapeLTest.grapel.Test.events;
		
import org.emoflon.cep.engine.Event;
import org.emoflon.cep.engine.TypeRegistry;
		
import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import Flights.Airport;
		
public class E1Event extends Event{
			
	final public static String EVENT_NAME = "e1";
	final public static EventType EVENT_TYPE = createEventType();
			
	public E1Event(final com.apama.event.Event apamaEvent, final TypeRegistry registry) {
		super(apamaEvent, registry);
	}
	
	public Airport getAirport() {
		return (Airport) fields.get("airport");
	}
	public java.lang.String getStr() {
		return (java.lang.String) fields.get("str");
	}
		
	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
			
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		type.addField("airport", FieldTypes.INTEGER);
		type.addField("str", FieldTypes.STRING);
		return type;
	}
		
	@Override
	public boolean isComplexType(String fieldName) {
		if("airport".equals(fieldName)) {
			return true;
		}
		return false;
	}
		
	@Override
	public Class<?> getClassOfField(String fieldName) {
		if("airport".equals(fieldName)) {
			return Airport.class;
		}
		if("str".equals(fieldName)) {
			return java.lang.String.class;
		}
		return null;
	}
		
}

