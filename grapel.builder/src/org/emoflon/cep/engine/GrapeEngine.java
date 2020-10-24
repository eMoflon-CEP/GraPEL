package org.emoflon.cep.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.emoflon.cep.util.IOUtils;
import org.emoflon.ibex.gt.api.GraphTransformationAPI;

import com.apama.EngineException;
//import com.apama.engine.EngineInfo;
//import com.apama.engine.EngineStatus;
import com.apama.engine.MonitorScript;
import com.apama.engine.beans.interfaces.ConsumerOperationsInterface;
import com.apama.engine.beans.interfaces.EngineClientInterface;
import com.apama.event.IEventListener;
import com.apama.event.parser.EventParser;
import com.apama.event.parser.EventType;
import com.apama.event.parser.FieldTypes;

public class GrapeEngine implements IEventListener{
	
	final static int SYNC_TIMEOUT = 5000;
	
	final protected GraphTransformationAPI eMoflonAPI;
	
	final protected ApamaCorrelator correlator;
	protected ConsumerOperationsInterface eventConsumer;
	private Thread currentThread = Thread.currentThread();
	private boolean waitingForSync = false;
	
	protected TypeRegistry registry = new TypeRegistry();
	protected EngineClientInterface engineClient;
	protected Map<String, EventHandler<? extends Event>> eventHandler = new HashMap<>();
	protected EventParser parser = new EventParser();
	protected Map<String, EventType> eventTypes = new HashMap<>();
	
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
		
		engineClient.injectMonitorScript(new MonitorScript(
				"event RequestSynchronizationEvent {\r\n" + 
				"	integer id;\r\n" + 
				"}")
		);
		EventType syncType = new EventType("RequestSynchronizationEvent");
		syncType.addField("id", FieldTypes.INTEGER);
		eventTypes.put(syncType.getName(), syncType);
		parser.registerEventType(syncType);
		
		engineClient.injectMonitorScript(new MonitorScript(
				"event UpdateEvent {\r\n" + 
				"	integer id;\r\n" + 
				"}")
		);
		EventType updateType = new EventType("UpdateEvent");
		updateType.addField("id", FieldTypes.INTEGER);
		eventTypes.put(updateType.getName(), updateType);
		parser.registerEventType(updateType);
		
		eventConsumer = engineClient.addConsumer("GrapeEngineSynchEvent", "channel1");
		eventConsumer.addEventListener(this);
		
//		engineClient.startStatusPollingThread();
//		engineClient.startInspectPollingThread();
	}
	
	public void setApplyAutomatically(boolean applyAutomatically) {
		eventHandler.values().stream()
		.filter(handler->(handler instanceof EMoflonRuleEventHandler))
		.map(handler -> (EMoflonRuleEventHandler<?,?,?,?>)handler)
		.forEach(handler -> handler.setApplyAutomatically(applyAutomatically));
	}
	
	protected void injectMonitorScript(String monitorFilePath) throws EngineException, IOException {
		MonitorScript script = new MonitorScript(IOUtils.loadTextFile(monitorFilePath));
		engineClient.injectMonitorScript(script);
	}
	
	@SuppressWarnings("static-access")
	public void update() {
		eventHandler.values().forEach(handler -> handler.clearRecentEvents());
		eMoflonAPI.updateMatches();
		
		try {
			engineClient.flushEvents();
		} catch (EngineException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		com.apama.event.Event syncEvent = new com.apama.event.Event(eventTypes.get("RequestSynchronizationEvent"));
		syncEvent.setField("id", 0);
		try {
			engineClient.sendEvents(syncEvent);
			waitingForSync = true;
//			System.out.println("Waiting for sync..");
			currentThread.sleep(SYNC_TIMEOUT);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
//			System.out.println("Got sync!");
		}
		
//		EngineStatus status = engineClient.getStatus();
//		System.out.println(status);
//		EngineInfo info = engineClient.getEngineInfo();
//		System.out.println(info);
				
		eventHandler.forEach((name, handler) -> {
			events.replace(name, handler.getAllEvents());
			recentEvents.replace(name, handler.getNewEvents());
		});
		
		// Pause eMoflon -> Apama subscriptions
		eventHandler.values().stream().filter(handler->(handler instanceof EMoflonEventHandler))
		.map(handler -> (EMoflonEventHandler<?,?,?>)handler)
		.forEach(handler -> handler.pauseSubsciptions());
		
		// Apply all rule events
		eventHandler.values().stream().filter(handler->(handler instanceof EMoflonRuleEventHandler))
		.map(handler -> (EMoflonRuleEventHandler<?,?,?,?>)handler)
		.forEach(handler -> handler.applyAutmatically());
		
		// Unpause eMoflon -> Apama subscriptions
		eventHandler.values().stream().filter(handler->(handler instanceof EMoflonEventHandler))
		.map(handler -> (EMoflonEventHandler<?,?,?>)handler)
		.forEach(handler -> handler.continueSubscriptions());
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
		eventTypes.put(handler.getHandlerName(), handler.getEventType());
		eventHandler.put(handler.getHandlerName(), handler);
	}
	
	protected EventHandler<? extends Event> getEventHandler(final String handler) {
		return eventHandler.get(handler);
	}
	
	public void dispose() throws EngineException, InterruptedException {
//		engineClient.stopStatusPollingThread();
//		engineClient.stopInspectPollingThread();
		eMoflonAPI.terminate();
		engineClient.removeAllConsumers();
		engineClient.disconnect();
		correlator.disposeCorrelator();
	}

	@Override
	public void handleEvent(com.apama.event.Event arg0) {
		if(!waitingForSync)
			return;
		
		arg0.setEventParser(parser);
		if(!arg0.getEventType().getName().equals("UpdateEvent"))
			return;
		
		waitingForSync = false;
		currentThread.interrupt();
	}

	@Override
	public void handleEvents(com.apama.event.Event[] arg0) {
		if(!waitingForSync)
			return;
		
		for(com.apama.event.Event event :  arg0) {
			handleEvent(event);
		}
		
	}
}

