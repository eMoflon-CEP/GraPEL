package org.emoflon.cep.engine;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;

public abstract class EMoflonEvent <M extends GraphTransformationMatch<M, P>, P extends GraphTransformationPattern<M, P>> extends Event {
	
	protected P pattern;
	protected M match;

	public EMoflonEvent(final P pattern, final M match) {
		this.pattern = pattern;
		this.match = match;
		assignFields();
	}
	
	public EMoflonEvent(final com.apama.event.Event apamaEvent, final TypeRegistry registry, final P pattern) {
		super(apamaEvent, registry);
		this.pattern = pattern;
		assignMatch();
	}

	public abstract void assignFields();
	
	public abstract void assignMatch();
	
	public M getMatch() {
		return match;
	}
	
	public P getPattern() {
		return pattern;
	}
}
