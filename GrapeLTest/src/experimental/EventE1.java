package experimental;

import org.emoflon.cep.engine.Event;
import org.emoflon.cep.engine.TypeRegistry;

import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import Flights.Airport;

public class EventE1 extends Event{
	
	final public static String EVENT_NAME = "e1";
	final public static EventType EVENT_TYPE = createEventType();
	
	public EventE1(final com.apama.event.Event apamaEvent, final TypeRegistry registry) {
		super(apamaEvent, registry);
	}
	
	public Airport getAirport() {
		return (Airport) fields.get("Airport");
	}
	
	public String getStr() {
		return (String) fields.get("str");
	}

	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
	
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		type.addField("Airport", FieldTypes.INTEGER);
		type.addField("str", FieldTypes.STRING);
		return type;
	}

	@Override
	public boolean isComplexType(String fieldName) {
		if(fieldName.equals("Airport")) {
			return true;
		}
		return false;
	}

	@Override
	public Class<?> getClassOfField(String fieldName) {
		if(fieldName.equals("Airport")) {
			return Flights.Airport.class;
		}
		return null;
	}

}
