package org.emoflon.cep.engine;

import java.util.function.Consumer;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;

import com.apama.EngineException;

public abstract class EMoflonEventHandler <E extends EMoflonEvent<M,P>, M extends GraphTransformationMatch<M, P>, P extends GraphTransformationPattern<M, P>> extends EventHandler<E> {
	
	protected P pattern;

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
	
	protected abstract E matchToEvent(M match);
	
	protected void sendAppearingMatchToApama(M match) {
		//TODO
		sendEvent(matchToEvent(match));
	}
	
	protected void sendDisappearingMatchToApama(M match) {
		//TODO
		sendEvent(matchToEvent(match));
	}

}
