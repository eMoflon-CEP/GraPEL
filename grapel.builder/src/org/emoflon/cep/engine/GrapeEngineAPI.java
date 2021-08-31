package org.emoflon.cep.engine;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.emoflon.ibex.gt.api.GraphTransformationAPI;
import org.emoflon.ibex.gt.api.GraphTransformationApp;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.EngineClientInterface;

/**
 * API to access the Grape engine
 */
public abstract class GrapeEngineAPI {
	
	// API, APP, engine
	protected GraphTransformationAPI eMoflonAPI;
	protected GraphTransformationApp<? extends GraphTransformationAPI> eMoflonApp;
	protected GrapeEngine grapeEngine;
	
	public GrapeEngineAPI(GraphTransformationApp<? extends GraphTransformationAPI> app) {
		this.eMoflonApp = app;
		registerMetamodels();
	}
	
	public abstract void registerMetamodels();
	
	/**
	 * Initializes the eMoflon API by loading the model
	 * @param modelUri where the model is located
	 */
	public void initEMoflonAPI(URI modelUri) {
		eMoflonApp.loadModel(modelUri);
		eMoflonAPI = eMoflonApp.initAPI();
	}
	
	/**
	 * Initializes the Grape engine by configuring correlator and engine, adding the event handlers and injecting monitor scripts into the correlator 
	 * 
	 * @throws Exception if correlator or engine client configuration fails, the initialization of the handler fails
	 * 		or the monitor script files cannot be injected into the Apama engine  
	 */
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
	
	/**
	 * @return the configured Apama correlator
	 * @throws Exception if the correlator configuration fails
	 */
	protected abstract ApamaCorrelator configureApamaCorrelator() throws Exception;
	
	/**
	 * @return the configured engine client interface
	 */
	protected abstract EngineClientInterface configureEngineClient();
	
	/**
	 * @return path to the monitor script files
	 */
	protected abstract List<String> getMonitorScriptFiles();
	
	/**
	 * @return a list of all event handlers, that are created by the engine
	 */
	protected abstract List<EventHandler<?>> createEventHandler();
	
	/**
	 * @param eventType specifying the type of event
	 * @return the event handler for a given event type
	 */
	public EventHandler<?> getEventHandler(final String eventType) {
		return grapeEngine.getEventHandler(eventType);
	}
	
	/**
	 * Calls update on the grape engine
	 */
	public void update() {
		grapeEngine.update();
	}
	
	/**
	 * @param autoApply to enable/disable automatic application of rules 
	 */
	public void setRuleAutoApply(boolean autoApply) {
		grapeEngine.setApplyAutomatically(autoApply);
	}
	
	/**
	 * @return all received events
	 */
	public Map<String, Collection<? extends Event>> getAllEvents() {
		return grapeEngine.getAllEvents();
	} 
	
	/**
	 * @return all newly received events
	 */
	public Map<String, Collection<? extends Event>> getNewEvents() {
		return grapeEngine.getNewEvents();
	}
	
	/**
	 * Disposes the Grape engine
	 */
	public void dispose() {
		try {
			grapeEngine.dispose();
		} catch (EngineException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
