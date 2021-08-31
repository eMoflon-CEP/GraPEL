package org.emoflon.cep.engine;

import org.emoflon.ibex.gt.api.GraphTransformationMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;

/**
 * eMoflon pattern match event container including the pattern and the concrete match w/ additional parameters
 * 
 * @param <M> the graph transformation match type
 * @param <P> the pattern corresponding to the match
 */
public abstract class EMoflonEvent <M extends GraphTransformationMatch<M, P>, P extends GraphTransformationPattern<M, P>> extends Event {
	
	/**
	 * Pattern of the event
	 */
	protected P pattern;
	/**
	 * Concrete match
	 */
	protected M match;
	/**
	 * Determines, if the match is appearing or disappearing
	 */
	protected boolean vanished;

	/**
	 * @param pattern for which a match is found
	 * @param match concrete match, which is producing this event
	 * @param vanished to indicate, if the match is appearing or disappearing
	 */
	public EMoflonEvent(final P pattern, final M match, boolean vanished) {
		this.pattern = pattern;
		this.match = match;
		this.vanished = vanished;
		assignFields();
	}
	
	/**
	 * @param apamaEvent representing the event
	 * @param registry which includes the mapping of ID/object for fields per type
	 * @param pattern of the event
	 */
	public EMoflonEvent(final com.apama.event.Event apamaEvent, final TypeRegistry registry, final P pattern) {
		super(apamaEvent, registry);
		this.pattern = pattern;
		assignMatch();
	}

	/**
	 * Assign pattern fields to event fields
	 */
	public abstract void assignFields();
	
	/**
	 * Assign event to match
	 */
	public abstract void assignMatch();
	
	/**
	 * @return the concrete match
	 */
	public M getMatch() {
		return match;
	}
	
	/**
	 * @return the pattern of the event
	 */
	public P getPattern() {
		return pattern;
	}
}
