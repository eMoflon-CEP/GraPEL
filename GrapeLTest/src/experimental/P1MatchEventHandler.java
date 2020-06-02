package experimental;

import java.io.IOException;
import java.util.function.Consumer;

import org.emoflon.cep.engine.EMoflonEventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.cep.util.IOUtils;

import com.apama.event.parser.EventType;

import GrapeLTest.api.GrapeLTestAPI;
import GrapeLTest.api.matches.P1Match;
import GrapeLTest.api.rules.P1Pattern;

public class P1MatchEventHandler extends EMoflonEventHandler<EventP1Match, P1Match, P1Pattern>{
	
	
	final public static String HANDLER_NAME = "P1MatchEventHandler";
	final public static String[] CHANNELS = {"channel1"};
	final public static String EPL_PATH = "src/experimental/event/EventP1Match.mon";

	public P1MatchEventHandler(GrapeEngine engine) {
		super(engine);
	}
	
	@Override
	public P1Pattern getPattern() {
		return ((GrapeLTestAPI)engine.getEMoflonAPI()).p1();
	}
	
	@Override
	public void subscribeToPattern(Consumer<P1Match> appearing, Consumer<P1Match> disappearing) {
		pattern.subscribeAppearing(appearing);
		pattern.subscribeDisappearing(disappearing);
	}

	@Override
	public EventP1Match matchToEvent(P1Match match) {
		return new EventP1Match(pattern, match);
	}

	@Override
	public String getHandlerName() {
		return HANDLER_NAME;
	}

	@Override
	public String[] getChannelNames() {
		return CHANNELS;
	}

	@Override
	public String loadEPLDescription() {
		try {
			return IOUtils.loadTextFile(EPL_PATH);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public EventP1Match convertEvent(com.apama.event.Event apamaEvent) {
		return new EventP1Match(apamaEvent, registry, pattern);
	}

	@Override
	public EventType getEventType() {
		return EventP1Match.EVENT_TYPE;
	}


}
