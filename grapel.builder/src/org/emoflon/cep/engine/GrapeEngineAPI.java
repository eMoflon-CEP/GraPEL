package org.emoflon.cep.engine;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.emoflon.ibex.gt.api.GraphTransformationAPI;
import org.emoflon.ibex.gt.api.GraphTransformationApp;

import com.apama.EngineException;
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
		for(EventHandler<? extends Event> handler : createEventHandler()) {
			grapeEngine.addEventHandler(handler);
			handler.init();
		}
		for(String mon : getMonitorScriptFiles()) {
			grapeEngine.injectMonitorScript(mon);
		}
		
	}
	
	protected abstract ApamaCorrelator configureApamaCorrelator() throws Exception;
	
	protected abstract EngineClientInterface configureEngineClient();
	
	protected abstract List<String> getMonitorScriptFiles();
	
	protected abstract List<EventHandler<?>> createEventHandler();
	
	public EventHandler<?> getEventHandler(final String eventType) {
		return grapeEngine.getEventHandler(eventType);
	}
	
	public void update() {
		grapeEngine.update();
	}
	
	public Map<String, Collection<? extends Event>> getAllEvents() {
		return grapeEngine.getAllEvents();
	} 
	
	public Map<String, Collection<? extends Event>> getNewEvents() {
		return grapeEngine.getNewEvents();
	}
	
	public void dispose() {
		try {
			grapeEngine.dispose();
		} catch (EngineException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
