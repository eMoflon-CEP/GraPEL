package org.emoflon.cep.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationRule;

import com.apama.EngineException;
import com.apama.event.parser.EventType;

public abstract class EMoflonRuleEventHandler <R extends EMoflonEvent<M,P>, E extends EMoflonEvent<M,P>, M extends GraphTransformationMatch<M, P>, P extends GraphTransformationRule<M, P>> extends EMoflonEventHandler<E, M, P> {

	protected EventType apamaRuleEventType;
	protected boolean applyAutomatically = false;
	protected Collection<R> ruleEvents = Collections.synchronizedList(new LinkedList<>());
	protected Collection<R> lastRuleEvents = Collections.synchronizedList(new LinkedList<>());
	protected Set<Consumer<R>> ruleSubscriber = new LinkedHashSet<>();
	protected Queue<R> eventQueue = new LinkedBlockingQueue<>();
	
	public EMoflonRuleEventHandler(GrapeEngine engine) {
		super(engine);
	}

	@Override
	public void init() throws EngineException {
		super.init();
		apamaRuleEventType = getRuleEventType();
	}
	
	public void setApplyAutomatically(boolean applyAutomatically) {
		this.applyAutomatically = applyAutomatically;
	}
	
	protected abstract R convertRuleEvent(final com.apama.event.Event apamaEvent);
	
	public void applyAutmatically() {
		if(!applyAutomatically)
			return;
		
		while(!eventQueue.isEmpty()) {
			engine.getEMoflonAPI().updateMatches();
			R event = eventQueue.poll();
			if(pattern.findMatches().contains(event.getMatch())) {
				apply(event.getMatch(), event);
			}
		}
	}
	
	protected abstract M apply(M match, R event);
	
	@Override
	public void handleEvent(com.apama.event.Event arg0) {
		if(arg0.getEventType().getName().equals(apamaEventType.getName())) {
			super.handleEvent(arg0);
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
	
	public boolean subscribeRuleEvents(Consumer<R> consumer) {
		return ruleSubscriber.add(consumer);
	}
	
	public boolean unsubscribeRuleEvents(Consumer<R> consumer) {
		return ruleSubscriber.remove(consumer);
	}
	
	public abstract EventType getRuleEventType();
	

}
