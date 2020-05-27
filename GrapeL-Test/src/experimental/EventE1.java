package experimental;

import org.emoflon.cep.engine.Event;

import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import Flights.Airport;

public class EventE1 extends Event{
	
	final public static String EVENT_NAME = "e1";
	final public static EventType EVENT_TYPE = createEventType();
	
	public EventE1(final com.apama.event.Event apamaEvent) {
		super(apamaEvent);
	}
	
	public Airport getAirport() {
		return (Airport) fields.get("Airport");
	}
	
	public String getString() {
		return (String) fields.get("string");
	}

	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
	
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		type.addField("Airport", FieldTypes.INTEGER);
		type.addField("string", FieldTypes.STRING);
		return type;
	}

}
