package experimental;

import org.emoflon.cep.engine.ApamaCorrelator;
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
	public ApamaCorrelator configureApamaCorrelator() throws Exception {
		ApamaCorrelator correlator = new ApamaCorrelator(CORRELATOR_LOCATION);
		correlator.setDebug(true);
		return correlator;
	}

	@Override
	public EngineClientInterface configureEngineClient() {
		EngineClientInterface engineClient = null;
		try {
			engineClient = EngineClientFactory.createEngineClient(CORRELATOR_HOSTNAME, CORRELATOR_PORT, GrapeLTestGrapeEngineAPI.class.getSimpleName());
		} catch (EngineException e) {
			e.printStackTrace();
		}
		return engineClient;
	}

}
