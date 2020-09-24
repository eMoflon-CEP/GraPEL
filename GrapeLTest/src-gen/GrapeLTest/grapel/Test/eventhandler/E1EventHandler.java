package GrapeLTest.grapel.Test.eventhandler;

import java.io.IOException;

import org.emoflon.cep.engine.EventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.cep.util.IOUtils;

import com.apama.event.parser.EventType;

import GrapeLTest.grapel.Test.events.E1Event;

public class E1EventHandler extends EventHandler<E1Event>{
	
	final public static String HANDLER_NAME = "E1EventHandler";
	final public static String[] CHANNELS = {"channel1"};
	final public static String EPL_PATH = "D:/Eclipse Workspaces/emoflon-ibex-devel3/git/GraPEL/GrapeLTest/src-gen/GrapeLTest/grapel/Test/eventmonitors/E1Event.mon";

	public E1EventHandler(GrapeEngine engine) {
		super(engine);
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
	public EventType getEventType() {
		return E1Event.EVENT_TYPE;
	}

	@Override
	public E1Event convertEvent(com.apama.event.Event apamaEvent) {
		return new E1Event(apamaEvent, registry);
	}

}

