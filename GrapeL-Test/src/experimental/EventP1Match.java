package experimental;

import org.emoflon.cep.engine.EMoflonEvent;

import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import Flights.Airport;
import GrapeLTest.api.matches.P1Match;
import GrapeLTest.api.rules.P1Pattern;

public class EventP1Match extends EMoflonEvent<P1Match, P1Pattern> {
	
	final public static String EVENT_NAME = "p1Match";
	final public static EventType EVENT_TYPE = createEventType();

	public EventP1Match(P1Pattern pattern, P1Match match) {
		super(pattern, match);
	}
	
	public Airport getAirport() {
		return match.getAirport();
	}

	@Override
	public void assignFields(P1Match match) {
		fields.put("Airport", match.getAirport());
	}

	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
	
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		type.addField("vanished", FieldTypes.BOOLEAN);
		type.addField("Airport", FieldTypes.INTEGER);
		return type;
	}

}
