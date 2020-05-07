package org.emoflon.cep.engine;

import org.eclipse.emf.common.util.URI;
import org.emoflon.ibex.gt.api.GraphTransformationAPI;
import org.emoflon.ibex.gt.api.GraphTransformationApp;

import com.apama.engine.beans.interfaces.EngineClientInterface;

public abstract class GrapeEngineAPI {
	
	protected GraphTransformationAPI eMoflonAPI;
	protected GraphTransformationApp<? extends GraphTransformationAPI> eMoflonApp;
	protected GrapeEngine grapeEngine;
	
	public GrapeEngineAPI(GraphTransformationApp<? extends GraphTransformationAPI> app) {
		this.eMoflonApp = app;
		registerMetamodels();
	}
	
	public abstract void registerMetamodels();
	
	public void initEMoflonAPI(URI modelUri) {
		eMoflonApp.loadModel(modelUri);
		eMoflonAPI = eMoflonApp.initAPI();
	}
	
	public void initGrapeEngine() throws Exception {
		ApamaCorrelator correlator = configureApamaCorrelator();
		grapeEngine = new GrapeEngine(eMoflonAPI, correlator);
		grapeEngine.init(this::configureEngineClient);
	}
	
	public abstract ApamaCorrelator configureApamaCorrelator() throws Exception;
	
	public abstract EngineClientInterface configureEngineClient();
}
