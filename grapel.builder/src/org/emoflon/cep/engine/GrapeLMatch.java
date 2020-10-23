package org.emoflon.cep.engine;

import java.util.LinkedHashMap;
import java.util.Map;

import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.common.operational.SimpleMatch;

public class GrapeLMatch extends SimpleMatch {
	
	protected Map<String, Object> ruleParameters;
	
	public GrapeLMatch(final String patternName) {
		super(patternName);
		this.ruleParameters = new LinkedHashMap<>();
	}

	public GrapeLMatch(final IMatch match) {
		super(match);
		this.ruleParameters = new LinkedHashMap<>();
	}
	
	public void addRuleParameter(String name, Object value) {
		ruleParameters.put(name, value);
	}
	
	public Object getRuleParameter(String name) {
		return ruleParameters.get(name);
	}
}
