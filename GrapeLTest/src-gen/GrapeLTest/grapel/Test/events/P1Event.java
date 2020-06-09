package GrapeLTest.grapel.Test.events;
		
import org.emoflon.cep.engine.EMoflonEvent;
import org.emoflon.cep.engine.TypeRegistry;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.common.operational.SimpleMatch;
		
import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import GrapeLTest.api.rules.P1Pattern;
import GrapeLTest.api.matches.P1Match;
import Flights.Airport;
		
public class P1Event extends EMoflonEvent<P1Match, P1Pattern>{
			
	final public static String EVENT_NAME = "p1";
	final public static EventType EVENT_TYPE = createEventType();
	
	public P1Event(final P1Pattern pattern, final P1Match match) {
		super(pattern, match);
	}
			
	public P1Event(final com.apama.event.Event apamaEvent, final TypeRegistry registry, final P1Pattern pattern) {
		super(apamaEvent, registry, pattern);
	}
	
	public Airport getAirport() {
		return (Airport) fields.get("airport");
	}

	@Override
	public void assignFields() {
		fields.put("airport", match.getAirport());
		fields.put("p1airportID", match.getAirport().getID());
	}
		
	@Override
	public EventType getStaticEventType() {
		return EVENT_TYPE;
	}
			
	public static EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		type.addField("vanished", FieldTypes.BOOLEAN);
		type.addField("airport", FieldTypes.INTEGER);
		type.addField("p1airportID", FieldTypes.STRING);
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
		if("p1airportID".equals(fieldName)) {
			return java.lang.String.class;
		}
		return null;
	}
	
	@Override
	public void assignMatch() {
		IMatch iMatch = new SimpleMatch(pattern.getPatternName());
		iMatch.put("airport", fields.get("airport"));
		iMatch.put("p1airportID", fields.get("p1airportID"));
		P1Match match = new P1Match(pattern, iMatch);
		this.match = match;
	}	
}

