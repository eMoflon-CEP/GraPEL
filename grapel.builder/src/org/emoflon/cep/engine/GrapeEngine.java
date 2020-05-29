package org.emoflon.cep.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.emoflon.cep.util.IOUtils;
import org.emoflon.ibex.gt.api.GraphTransformationAPI;

import com.apama.EngineException;
import com.apama.engine.MonitorScript;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.parser.EventParser;

public class GrapeEngine {
	
	final protected GraphTransformationAPI eMoflonAPI;
	
	final protected ApamaCorrelator correlator;
	protected TypeRegistry registry = new TypeRegistry();
	protected EngineClientInterface engineClient;
	protected Map<String, EventHandler<? extends Event>> eventHandler = new HashMap<>();
	protected EventParser parser = new EventParser();
	
	protected Map<String, Collection<? extends Event>> events = new HashMap<>();
	protected Map<String, Collection<? extends Event>> recentEvents = new HashMap<>();
	
	public GrapeEngine(final GraphTransformationAPI eMoflonAPI, final ApamaCorrelator correlator) {
		this.eMoflonAPI = eMoflonAPI;
		this.correlator = correlator;
	}

	protected void init(Supplier<EngineClientInterface> engineClientFactory) throws Exception {
		correlator.runCorrelator();
		engineClient = engineClientFactory.get();
		if(engineClient == null)
			throw new RuntimeException("Could not connect engine client to Apama Correlator!");
	}
	
	protected void injectMonitorScript(String monitorFilePath) throws EngineException, IOException {
		MonitorScript script = new MonitorScript(IOUtils.loadTextFile(monitorFilePath));
		engineClient.injectMonitorScript(script);
	}
	
	public void update() {
		eventHandler.values().forEach(handler -> handler.clearRecentEvents());
		eMoflonAPI.updateMatches();
		//TODO synchronize with Apama by using a synchroneous request (send -> wait -> receive)
		eventHandler.forEach((name, handler) -> {
			events.replace(name, handler.getAllEvents());
			recentEvents.replace(name, handler.getNewEvents());
		});
	}
	
	public Map<String, Collection<? extends Event>> getAllEvents() {
		return events;
	}
	
	public Map<String, Collection<? extends Event>> getNewEvents() {
		return recentEvents;
	}
	
	public GraphTransformationAPI getEMoflonAPI() {
		return eMoflonAPI;
	}
	
	protected EngineClientInterface getEngineClient() {
		return engineClient;
	}
	
	protected TypeRegistry getTypeRegistry() {
		return registry;
	}
	
	protected void addEventHandler(final EventHandler<? extends Event> handler) {
		handler.setEventParser(parser);
		eventHandler.put(handler.getHandlerName(), handler);
	}
	
	protected EventHandler<? extends Event> getEventHandler(final String handler) {
		return eventHandler.get(handler);
	}
	
	public void dispose() throws EngineException, InterruptedException {
		eMoflonAPI.terminate();
		engineClient.removeAllConsumers();
		engineClient.disconnect();
		correlator.disposeCorrelator();
	}
}

