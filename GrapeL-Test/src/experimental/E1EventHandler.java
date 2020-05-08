package experimental;

import java.io.IOException;

import org.emoflon.cep.engine.EventHandler;
import org.emoflon.cep.engine.GrapeEngine;
import org.emoflon.cep.util.IOUtils;

import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

import experimental.EventE1;

public class E1EventHandler extends EventHandler<EventE1>{
	
	final public static String EVENT_NAME = "e1";
	final public static String HANDLER_NAME = "E1EventHandler";
	final public static String[] CHANNELS = {"channel1"};
	final public static String EPL_PATH = "some path..";

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
	public EventType createEventType() {
		EventType type = new EventType(EVENT_NAME);
		type.addField("Airport", FieldTypes.INTEGER);
		type.addField("string", FieldTypes.STRING);
		return type;
	}

	@Override
	public EventE1 convertEvent(com.apama.event.Event apamaEvent) {
		return new EventE1(apamaEvent);
	}

}
