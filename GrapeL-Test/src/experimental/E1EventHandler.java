package experimental;

import java.io.IOException;

import org.emoflon.cep.engine.EventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.cep.util.IOUtils;

import com.apama.event.parser.EventType;

import experimental.EventE1;

public class E1EventHandler extends EventHandler<EventE1>{
	
	final public static String HANDLER_NAME = "E1EventHandler";
	final public static String[] CHANNELS = {"channel1"};
	final public static String EPL_PATH = "src/experimental/event/EventE1.mon";

	public E1EventHandler(GrapeEngine engine) {
		super(engine);
		// TODO Auto-generated constructor stub
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public EventType getEventType() {
		return EventE1.EVENT_TYPE;
	}

	@Override
	public EventE1 convertEvent(com.apama.event.Event apamaEvent) {
		return new EventE1(apamaEvent, registry);
	}

}
