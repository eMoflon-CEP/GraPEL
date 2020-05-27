package org.emoflon.cep.engine;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;

public abstract class EMoflonEvent <M extends GraphTransformationMatch<M, P>, P extends GraphTransformationPattern<M, P>> extends Event {
	
	protected P pattern;
	protected M match;

	public EMoflonEvent(final P pattern, final M match) {
		this.pattern = pattern;
		assignFields(match);
	}

	public abstract void assignFields(final M match);
}
