package experimental;

import org.emoflon.cep.engine.EMoflonEvent;
import org.emoflon.cep.engine.TypeRegistry;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.common.operational.SimpleMatch;

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
	
	public EventP1Match(final com.apama.event.Event apamaEvent, final TypeRegistry registry, final P1Pattern pattern) {
		super(apamaEvent, registry, pattern);
	}
	
	public Airport getAirport() {
		return match.getAirport();
	}

	@Override
	public void assignFields() {
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

	@Override
	public void assignMatch() {
		IMatch iMatch = new SimpleMatch(pattern.getPatternName());
		iMatch.put("Airport", fields.get("Airport"));
		P1Match match = new P1Match(pattern, iMatch);
		this.match = match;
	}

}
