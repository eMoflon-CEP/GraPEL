package org.emoflon.cep.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationRule;

import com.apama.EngineException;
import com.apama.engine.MonitorScript;
import com.apama.event.parser.EventType;

/**
 * Event handler for eMoflon rule events of the type R
 *
 * @param <R> defining the rule event type
 * @param <E> defining the event type
 * @param <M> the graph transformation match type
 * @param <P> the pattern corresponding to the match
 */
public abstract class EMoflonRuleEventHandler <R extends EMoflonEvent<M,P>, E extends EMoflonEvent<M,P>, M extends GraphTransformationMatch<M, P>, P extends GraphTransformationRule<M, P>> extends EMoflonEventHandler<E, M, P> {

	/**
	 * Apama type of the rule event 
	 */
	protected EventType apamaRuleEventType;
	/**
	 * Automatic application of rules on event
	 */
	protected boolean applyAutomatically = false;
	/**
	 * All rule events
	 */
	protected Collection<R> ruleEvents = Collections.synchronizedList(new LinkedList<>());
	/**
	 * Recent rule events
	 */
	protected Collection<R> lastRuleEvents = Collections.synchronizedList(new LinkedList<>());
	/**
	 * Set of subscribers to rule event
	 */
	protected Set<Consumer<R>> ruleSubscriber = new LinkedHashSet<>();
	/**
	 * Queue with events waiting for rule application
	 */
	protected Queue<R> eventQueue = new LinkedBlockingQueue<>();
	
	public EMoflonRuleEventHandler(GrapeEngine engine) {
		super(engine);
	}

	@Override
	public void init() throws EngineException {
		apamaEventType = getEventType();
		apamaRuleEventType = getRuleEventType();
		parser.registerEventType(apamaEventType);
		parser.registerEventType(apamaRuleEventType);
		
		MonitorScript script = new MonitorScript(loadEPLDescription());
		engineClient.injectMonitorScript(script);
		eventConsumer = engineClient.addConsumer(getHandlerName(), getChannelNames());
		eventConsumer.addEventListener(this);
		
		pattern = getPattern();
		subscribeToPattern(this::sendAppearingMatchToApama, this::sendDisappearingMatchToApama);
	}
	
	/**
	 * @param applyAutomatically to enable/disable automatic rule application
	 */
	public void setApplyAutomatically(boolean applyAutomatically) {
		this.applyAutomatically = applyAutomatically;
	}
	
	/**
	 * Creates a Grape rule event representation for a match
	 * @param apamaEvent to be converted
	 * @return the Grape rule event representing the match
	 */
	protected abstract R convertRuleEvent(final com.apama.event.Event apamaEvent);
	
	/**
	 * If enabled, applies the rule automatically for each rule event in the event queue
	 */
	public void applyAutmatically() {
		if(!applyAutomatically)
			return;
		
		while(!eventQueue.isEmpty()) {
			engine.getEMoflonAPI().updateMatches();
			R event = eventQueue.poll();
			Collection<M> matches = pattern.findMatches(false);
			if(matches.contains(event.getMatch())) {
				apply(event.getMatch(), event);
			}
		}
	}
	
	/**
	 * @param match that is found for rule application
	 * @param event the rule event, that triggered rule application
	 * @return the result of the rule application
	 */
	protected abstract Optional<M> apply(M match, R event);
	
	@Override
	public void handleEvent(com.apama.event.Event arg0) {
		arg0.setEventParser(parser);
		if(arg0.getEventType().getName().equals(apamaEventType.getName())) {
			E event = convertEvent(arg0);
			events.add(event);
			lastEvents.add(event);
			subscriber.forEach(sub -> sub.accept(event));
		} else if(arg0.getEventType().getName().equals(apamaRuleEventType.getName())) {
			R event = convertRuleEvent(arg0);
			ruleEvents.add(event);
			lastRuleEvents.add(event);
			ruleSubscriber.forEach(sub -> sub.accept(event));
			// stash event
			eventQueue.add(event);
		}
	}
	
	@Override
	protected void clearRecentEvents() {
		super.clearRecentEvents();
		lastRuleEvents.clear();
	}
	
	 /**
	 * Adds consumer to the rule subscriber list of the event handler 
	 * @param consumer to be added to the subscribers of the event handler for the rule
	 * @return if the consumer was not already present as subscriber
	 */
	public boolean subscribeRuleEvents(Consumer<R> consumer) {
		return ruleSubscriber.add(consumer);
	}
	
	/**
	 * Removes rule consumer from subscribers of the event handler
	 * @param consumer to be removed
	 * @return if the consumer was present as a subscriber
	 */
	public boolean unsubscribeRuleEvents(Consumer<R> consumer) {
		return ruleSubscriber.remove(consumer);
	}
	
	/**
	 * @return the event type for rule events handled by the event handler
	 */
	public abstract EventType getRuleEventType();
	

}
