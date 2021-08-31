package org.emoflon.cep.engine;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;

import com.apama.EngineException;

/**
 * Event handler for eMoflon events of the type E
 *
 * @param <E> defining the event type
 * @param <M> the graph transformation match type
 * @param <P> the pattern corresponding to the match
 */
public abstract class EMoflonEventHandler <E extends EMoflonEvent<M,P>, M extends GraphTransformationMatch<M, P>, P extends GraphTransformationPattern<M, P>> extends EventHandler<E> {
	
	/**
	 * The pattern of the event
	 */
	protected P pattern;
	/**
	 * Enable/Disable subscribers
	 */
	public boolean paused = false;
	/**
	 * Appearing match events
	 */
	protected Queue<M> appearingQueue = new LinkedBlockingQueue<>();
	/**
	 * Disappearing match events
	 */
	protected Queue<M> disappearingQueue = new LinkedBlockingQueue<>();
	// todo queues

	public EMoflonEventHandler(GrapeEngine engine) {
		super(engine);
	}
	
	@Override
	public void init() throws EngineException {
		super.init();
		pattern = getPattern();
		subscribeToPattern(this::sendAppearingMatchToApama, this::sendDisappearingMatchToApama);
	}
	
	/**
	 * @return the pattern of the events handled by this handler
	 */
	public abstract P getPattern();
	
	/**
	 * Adds subscribers for appearing and disappearing match events 
	 * @param appearing consumer for appearing match events
	 * @param disappearing consumer for disappering match events
	 */
	protected abstract void subscribeToPattern(Consumer<M> appearing, Consumer<M> disappearing);
	
	/**
	 * Creates a Grape eMoflon event representation for a match
	 * @param match to be converted
	 * @param vanished showing, if the match is an appearing or disappearing match
	 * @return the Grape eMoflon event representing the match
	 */
	protected abstract E matchToEvent(M match, boolean vanished);
	
	/**
	 * Disables subscribers
	 */
	protected void pauseSubsciptions() {
		paused = true;
	}
	
	/**
	 * Empties disappearing and appearing queues by sending the converted match events to the engine and sets paused to false.
	 */
	protected void continueSubscriptions() {
		while(!disappearingQueue.isEmpty()) {
			sendEvent(matchToEvent(disappearingQueue.poll(), true));
		}
		
		while(!appearingQueue.isEmpty()) {
			sendEvent(matchToEvent(appearingQueue.poll(), false));
		}
		
		paused = false;
	}
	
	/**
	 * Converts appearing matches to match events and sends them to the engine, if not paused.
	 * If paused, the match is added to the queue.
	 * @param match which is appearing and should be handled by this events handler 
	 */
	protected void sendAppearingMatchToApama(M match) {
		if(!paused)
			sendEvent(matchToEvent(match, false));
		else
			appearingQueue.add(match);
	}
	
	/**
	 * Converts disappearing matches to match events and sends them to the engine, if not paused.
	 * If paused, the match is added to the queue.
	 * @param match which is disappearing and should be handled by this events handler 
	 */
	protected void sendDisappearingMatchToApama(M match) {
		if(!paused)
			sendEvent(matchToEvent(match, true));
		else
			disappearingQueue.add(match);
	}

}
