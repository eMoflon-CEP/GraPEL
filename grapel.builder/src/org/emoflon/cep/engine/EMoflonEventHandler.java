package org.emoflon.cep.engine;

import java.util.function.Consumer;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;

import com.apama.EngineException;

public abstract class EMoflonEventHandler <E extends EMoflonEvent<M,P>, M extends GraphTransformationMatch<M, P>, P extends GraphTransformationPattern<M, P>> extends EventHandler<E> {

	public EMoflonEventHandler(GrapeEngine engine) {
		super(engine);
	}
	
	@Override
	public void init() throws EngineException {
		super.init();
		subscribeToPattern(this::sendMatchToApama);
	}
	
	public abstract void subscribeToPattern(Consumer<GraphTransformationMatch<M,P>> matchConsumer);
	
	public abstract E matchToEvent(GraphTransformationMatch<M,P> match);
	
	public void sendMatchToApama(GraphTransformationMatch<M,P> match) {
		sendEvent(matchToEvent(match));
	}

}
