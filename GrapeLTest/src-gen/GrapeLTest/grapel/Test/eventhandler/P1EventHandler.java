package GrapeLTest.grapel.Test.eventhandler;

import java.io.IOException;
import java.util.function.Consumer;

import org.emoflon.cep.engine.EMoflonEventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.cep.util.IOUtils;

import com.apama.event.parser.EventType;

import GrapeLTest.api.GrapeLTestAPI;
import GrapeLTest.api.matches.P1Match;
import GrapeLTest.api.rules.P1Pattern;

import GrapeLTest.grapel.Test.events.P1Event;

public class P1EventHandler extends EMoflonEventHandler<P1Event, P1Match, P1Pattern>{
	
	
	final public static String HANDLER_NAME = "P1EventHandler";
	final public static String[] CHANNELS = {"channel1"};
	final public static String EPL_PATH = "D:/Eclipse Workspaces/emoflon-ibex-devel3/git/GraPEL/GrapeLTest/src-gen/GrapeLTest/grapel/Test/eventmonitors/P1Event.mon";

	public P1EventHandler(GrapeEngine engine) {
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
	public P1Event matchToEvent(P1Match match) {
		return new P1Event(pattern, match);
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
	public P1Event convertEvent(com.apama.event.Event apamaEvent) {
		return new P1Event(apamaEvent, registry, pattern);
	}

	@Override
	public EventType getEventType() {
		return P1Event.EVENT_TYPE;
	}


}

