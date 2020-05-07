package experimental;

import org.emoflon.cep.engine.Event;

import com.apama.event.parser.EventType;

import Flights.Airport;

public class EventE1 extends Event{

	public EventE1(EventType type) {
		super(type);
	}
	
	public Airport getAirport() {
		return (Airport) fields.get("Airport");
	}
	
	public String getString() {
		return (String) fields.get("string");
	}

}
