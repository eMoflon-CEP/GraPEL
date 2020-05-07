package org.emoflon.cep.engine;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;

import com.apama.event.parser.EventType;

public abstract class EMoflonEvent <M extends GraphTransformationMatch<M, P>, P extends GraphTransformationPattern<M, P>> extends Event {
	
	protected P pattern;
	protected M match;

	public EMoflonEvent(final EventType type, final P pattern, final M match) {
		super(type);
	}

}
