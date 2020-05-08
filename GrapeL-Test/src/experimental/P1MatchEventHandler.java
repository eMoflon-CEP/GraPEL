package experimental;

import java.util.function.Consumer;

import org.emoflon.cep.engine.EMoflonEventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;

import com.apama.event.Event;
import com.apama.event.parser.EventType;

import GrapeLTest.api.matches.P1Match;
import GrapeLTest.api.rules.P1Pattern;

public class P1MatchEventHandler extends EMoflonEventHandler<EventP1Match, P1Match, P1Pattern>{

	public P1MatchEventHandler(GrapeEngine engine) {
		super(engine);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void subscribeToPattern(Consumer<GraphTransformationMatch<P1Match, P1Pattern>> matchConsumer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EventP1Match matchToEvent(GraphTransformationMatch<P1Match, P1Pattern> match) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHandlerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getChannelNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String loadEPLDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventType createEventType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventP1Match convertEvent(Event apamaEvent) {
		// TODO Auto-generated method stub
		return null;
	}

}
