package org.emoflon.cep.engine;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;

import com.apama.EngineException;

public abstract class EMoflonEventHandler <E extends EMoflonEvent<M,P>, M extends GraphTransformationMatch<M, P>, P extends GraphTransformationPattern<M, P>> extends EventHandler<E> {
	
	protected P pattern;
	public boolean paused = false;
	protected Queue<M> appearingQueue = new LinkedBlockingQueue<>();
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
	
	public abstract P getPattern();
	
	protected abstract void subscribeToPattern(Consumer<M> appearing, Consumer<M> disappearing);
	
	protected abstract E matchToEvent(M match, boolean vanished);
	
	protected void pauseSubsciptions() {
		paused = true;
	}
	
	protected void continueSubscriptions() {
		while(!disappearingQueue.isEmpty()) {
			sendEvent(matchToEvent(disappearingQueue.poll(), true));
		}
		
		while(!appearingQueue.isEmpty()) {
			sendEvent(matchToEvent(appearingQueue.poll(), false));
		}
		
		paused = false;
	}
	
	protected void sendAppearingMatchToApama(M match) {
		if(!paused)
			sendEvent(matchToEvent(match, false));
		else
			appearingQueue.add(match);
	}
	
	protected void sendDisappearingMatchToApama(M match) {
		if(!paused)
			sendEvent(matchToEvent(match, true));
		else
			disappearingQueue.add(match);
	}

}
