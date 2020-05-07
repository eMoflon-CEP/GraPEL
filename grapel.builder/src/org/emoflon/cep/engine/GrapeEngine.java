package org.emoflon.cep.engine;

import java.util.function.Supplier;

import org.emoflon.ibex.gt.api.GraphTransformationAPI;
import com.apama.engine.beans.interfaces.EngineClientInterface;

public class GrapeEngine {
	
	final protected GraphTransformationAPI eMoflonAPI;
	
	final protected ApamaCorrelator correlator;
	protected TypeRegistry registry = new TypeRegistry();
	protected EngineClientInterface engineClient;
	
	public GrapeEngine(final GraphTransformationAPI eMoflonAPI, final ApamaCorrelator correlator) {
		this.eMoflonAPI = eMoflonAPI;
		this.correlator = correlator;
	}

	public void init(Supplier<EngineClientInterface> engineClientFactory) throws Exception {
		correlator.runCorrelator();
		engineClient = engineClientFactory.get();
		if(engineClient == null)
			throw new RuntimeException("Could not connect engine client to Apama Correlator!");
	}
	
	public EngineClientInterface getEngineClient() {
		return engineClient;
	}
	
	public TypeRegistry getTypeRegistry() {
		return registry;
	}
}

