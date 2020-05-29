package experimental;

import java.util.Arrays;
import java.util.List;

import org.emoflon.cep.engine.ApamaCorrelator;
import org.emoflon.cep.engine.Event;
import org.emoflon.cep.engine.EventHandler;
import org.emoflon.cep.engine.GrapeEngineAPI;

import com.apama.EngineException;
import com.apama.engine.beans.EngineClientFactory;
import com.apama.engine.beans.interfaces.EngineClientInterface;

import GrapeLTest.api.GrapeLTestApp;

public abstract class GrapeLTestGrapeEngineAPI extends GrapeEngineAPI{
	
	final public static String CORRELATOR_LOCATION = "C:\\SoftwareAG\\Apama\\bin\\correlator.exe";
	final public static int CORRELATOR_PORT = 15903;
	final public static String CORRELATOR_HOSTNAME = "localhost";
	
	public GrapeLTestGrapeEngineAPI(final GrapeLTestApp app) {
		super(app);
	}
	
	@Override
	public void registerMetamodels() {
		((GrapeLTestApp)eMoflonApp).registerMetaModels();
	}

	@Override
	protected ApamaCorrelator configureApamaCorrelator() throws Exception {
		ApamaCorrelator correlator = new ApamaCorrelator(CORRELATOR_LOCATION);
		correlator.setDebug(true);
		return correlator;
	}

	@Override
	protected EngineClientInterface configureEngineClient() {
		EngineClientInterface engineClient = null;
		try {
			engineClient = EngineClientFactory.createEngineClient(CORRELATOR_HOSTNAME, CORRELATOR_PORT, GrapeLTestGrapeEngineAPI.class.getSimpleName());
		} catch (EngineException e) {
			e.printStackTrace();
		}
		return engineClient;
	}
	
	@Override
	protected List<String> getMonitorScriptFiles() {
		return Arrays.asList("src/experimental/monitor/PatternTestEvent.mon");
	}
	
	@Override
	protected List<EventHandler<? extends Event>> createEventHandler() {
		return Arrays.asList(new E1EventHandler(grapeEngine), new P1MatchEventHandler(grapeEngine));
	}
	
	public E1EventHandler getE1EventHandler() {
		return (E1EventHandler) getEventHandler(E1EventHandler.HANDLER_NAME);
	}
	
	public P1MatchEventHandler getP1MatchEventHandler() {
		return (P1MatchEventHandler) getEventHandler(P1MatchEventHandler.HANDLER_NAME);
	}

}
