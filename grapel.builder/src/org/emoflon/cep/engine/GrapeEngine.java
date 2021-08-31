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

/**
 * Core Grape Engine controlling and handling GrapeL interaction between eMoflon and Apama
 */
public class GrapeEngine implements IEventListener{
	
	/**
	 * Waiting time after a synchronization event
	 */
	final static int SYNC_TIMEOUT = 5000;
	
	/**
	 * API for the graph transformation tool
	 */
	final protected GraphTransformationAPI eMoflonAPI;
	
	/**
	 * Apama correlator
	 */
	final protected ApamaCorrelator correlator;
	/**
	 * Interface for the event consumer
	 */
	protected ConsumerOperationsInterface eventConsumer;
	/**
	 * Engine thread 
	 */
	private Thread currentThread = Thread.currentThread();
	/**
	 * Lock indicating, if a sync has been received
	 */
	private boolean waitingForSync = false;
	
	/**
	 * Mapping between ID/object per type
	 */
	protected TypeRegistry registry = new TypeRegistry();
	/**
	 * Interface to the engine client
	 */
	protected EngineClientInterface engineClient;
	/**
	 * Map from event handler names to event handlers in the engine
	 */
	protected Map<String, EventHandler<? extends Event>> eventHandler = new HashMap<>();
	/**
	 * Event parser
	 */
	protected EventParser parser = new EventParser();
	/**
	 * Map from event type names to event types
	 */
	protected Map<String, EventType> eventTypes = new HashMap<>();
	
	/**
	 * Map from event name to all events
	 */
	protected Map<String, Collection<? extends Event>> events = new HashMap<>();
	/**
	 * Map from event name to recent events
	 */
	protected Map<String, Collection<? extends Event>> recentEvents = new HashMap<>();

	public GrapeEngine(final GraphTransformationAPI eMoflonAPI, final ApamaCorrelator correlator) {
		this.eMoflonAPI = eMoflonAPI;
		this.correlator = correlator;
	}

	/**
	 * Initializes the engine by starting the correlator, synchronizing the engine client and sending an update event 
	 * @param engineClientFactory to create the engine client
	 * @throws Exception if no engine client is provided, sending the events fails or the event consumer cannot be added
	 */
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
	
	/**
	 * @param applyAutomatically to enable/disable automatic rule application
	 */
	public void setApplyAutomatically(boolean applyAutomatically) {
		eventHandler.values().stream()
		.filter(handler->(handler instanceof EMoflonRuleEventHandler))
		.map(handler -> (EMoflonRuleEventHandler<?,?,?,?>)handler)
		.forEach(handler -> handler.setApplyAutomatically(applyAutomatically));
	}
	
	/**
	 * Injects a monitor script into the engine
	 * @param monitorFilePath to the monitor file
	 * @throws EngineException if injecting the monitor script fails
	 * @throws IOException if loading the monitor file fails
	 */
	protected void injectMonitorScript(String monitorFilePath) throws EngineException, IOException {
		MonitorScript script = new MonitorScript(IOUtils.loadTextFile(monitorFilePath));
		engineClient.injectMonitorScript(script);
	}
	
	/**
	 * Resets the recent events and trigger, updates the matches via eMoflon API call, handles mapping and communication with Apama and triggers possible rule applications
	 */
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
	
	/**
	 * @return a map containing all events
	 */
	public Map<String, Collection<? extends Event>> getAllEvents() {
		return events;
	}
	
	/**
	 * @return a map containing all recent events
	 */
	public Map<String, Collection<? extends Event>> getNewEvents() {
		return recentEvents;
	}
	
	/**
	 * @return the eMoflon API
	 */
	public GraphTransformationAPI getEMoflonAPI() {
		return eMoflonAPI;
	}
	
	/**
	 * @return the engine client
	 */
	protected EngineClientInterface getEngineClient() {
		return engineClient;
	}
	
	/**
	 * @return the mapping object/ID per type
	 */
	protected TypeRegistry getTypeRegistry() {
		return registry;
	}
	
	/**
	 * Adds an event handler to the event handlers to the Grape engine
	 * @param handler to be added
	 */
	protected void addEventHandler(final EventHandler<? extends Event> handler) {
		handler.setEventParser(parser);
		eventTypes.put(handler.getHandlerName(), handler.getEventType());
//		if(handler instanceof EMoflonRuleEventHandler) {
//			((EMoflonRuleEventHandler)handler)
//		}
		eventHandler.put(handler.getHandlerName(), handler);
	}
	
	/**
	 * Gets the event handler from the event handlers list of the engine by name
	 * @param handler name of event handler
	 * @return the event handler with the handler name
	 */
	protected EventHandler<? extends Event> getEventHandler(final String handler) {
		return eventHandler.get(handler);
	}
	
	/**
	 * Terminates the eMoflon API, removes the consumers and disconnects from the apama engine, and disposes the correlator
	 * @throws EngineException if removing the consumers or disconnecting the engine fails
	 * @throws InterruptedException if disposing the correlator is interrupted
	 */
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

