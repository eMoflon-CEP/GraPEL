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
	
	public abstract void subscribeToPattern(Consumer<M> appearing, Consumer<M> disappearing);
	
	public abstract E matchToEvent(M match);
	
	public void sendAppearingMatchToApama(M match) {
		//TODO
		sendEvent(matchToEvent(match));
	}
	
	public void sendDisappearingMatchToApama(M match) {
		//TODO
		sendEvent(matchToEvent(match));
	}

}
