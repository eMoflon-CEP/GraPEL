package org.emoflon.cep.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.emoflon.cep.util.IOUtils;
import org.emoflon.ibex.gt.api.GraphTransformationAPI;

import com.apama.EngineException;
import com.apama.engine.beans.interfaces.EngineClientInterface;

public class GrapeEngine {
	
	final protected GraphTransformationAPI eMoflonAPI;
	
	final protected ApamaCorrelator correlator;
	protected TypeRegistry registry = new TypeRegistry();
	protected EngineClientInterface engineClient;
	protected Map<String, EventHandler> eventHandler = new HashMap<>();
	
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
	
	public void injectMonitorScript(String monitorFilePath) throws EngineException, IOException {
		engineClient.injectMonitorScriptFromFile(IOUtils.loadTextFile(monitorFilePath));
	}
	
	public void update() {
		eMoflonAPI.updateMatches();
		//TODO synchronize with Apama by using a synchroneous request (send -> wait -> receive)
	}
	
	public GraphTransformationAPI getEMoflonAPI() {
		return eMoflonAPI;
	}
	
	public EngineClientInterface getEngineClient() {
		return engineClient;
	}
	
	public TypeRegistry getTypeRegistry() {
		return registry;
	}
	
	public void addEventHandler(final EventHandler handler) {
		eventHandler.put(handler.getHandlerName(), handler);
	}
	
	public EventHandler getEventHandler(final String handler) {
		return eventHandler.get(handler);
	}
	
	public void dispose() throws EngineException, InterruptedException {
		eMoflonAPI.terminate();
		engineClient.removeAllConsumers();
		engineClient.disconnect();
		correlator.disposeCorrelator();
	}
}

