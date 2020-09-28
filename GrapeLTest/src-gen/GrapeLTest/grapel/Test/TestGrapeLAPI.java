package GrapeLTest.grapel.Test;

import java.util.Arrays;
import java.util.List;

import org.emoflon.cep.engine.ApamaCorrelator;
import org.emoflon.cep.engine.EventHandler;
import org.emoflon.cep.engine.GrapeEngineAPI;

import com.apama.EngineException;
import com.apama.engine.beans.EngineClientFactory;
import com.apama.engine.beans.interfaces.EngineClientInterface;

import GrapeLTest.api.GrapeLTestApp;

import GrapeLTest.grapel.Test.eventhandler.P1EventHandler;
import GrapeLTest.grapel.Test.eventhandler.E1EventHandler;

public abstract class TestGrapeLAPI extends GrapeEngineAPI{
	
	final public static String CORRELATOR_LOCATION = "C:\\SoftwareAG\\Apama\\bin\\correlator.exe";
	final public static int CORRELATOR_PORT = 15903;
	final public static String CORRELATOR_HOSTNAME = "localhost";
	
	public TestGrapeLAPI(final GrapeLTestApp app) {
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
			engineClient = EngineClientFactory.createEngineClient(CORRELATOR_HOSTNAME, CORRELATOR_PORT, TestGrapeLAPI.class.getSimpleName());
		} catch (EngineException e) {
			e.printStackTrace();
		}
		return engineClient;
	}
	
	@Override
	protected List<String> getMonitorScriptFiles() {
		return Arrays.asList(
			"C:/Users/sehmes/Coding/eclipse_workspaces/emoflon_devel/git/GraPEL/GrapeLTest/src-gen/GrapeLTest/grapel/Test/patternmonitors/Maintainance.mon", 
			"C:/Users/sehmes/Coding/eclipse_workspaces/emoflon_devel/git/GraPEL/GrapeLTest/src-gen/GrapeLTest/grapel/Test/patternmonitors/TestEvent.mon", 
			"C:/Users/sehmes/Coding/eclipse_workspaces/emoflon_devel/git/GraPEL/GrapeLTest/src-gen/GrapeLTest/grapel/Test/patternmonitors/TestEvent2.mon"
		);
	}
	
	@Override
	protected List<EventHandler<?>> createEventHandler() {
		return Arrays.asList(
			new P1EventHandler(grapeEngine), 
			new E1EventHandler(grapeEngine)
		);
	}
	
	public P1EventHandler getP1EventHandler() {
		return (P1EventHandler) getEventHandler(P1EventHandler.HANDLER_NAME);
	}
	public E1EventHandler getE1EventHandler() {
		return (E1EventHandler) getEventHandler(E1EventHandler.HANDLER_NAME);
	}
}
