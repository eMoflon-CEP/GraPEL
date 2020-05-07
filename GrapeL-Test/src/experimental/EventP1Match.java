package experimental;

import org.emoflon.cep.engine.EMoflonEvent;

import com.apama.event.parser.EventType;

import Flights.Airport;
import GrapeLTest.api.matches.P1Match;
import GrapeLTest.api.rules.P1Pattern;

public class EventP1Match extends EMoflonEvent<P1Match, P1Pattern> {

	public EventP1Match(EventType type, P1Pattern pattern, P1Match match) {
		super(type, pattern, match);
	}
	
	public Airport getAirport() {
		return match.getAirport();
	}

}
