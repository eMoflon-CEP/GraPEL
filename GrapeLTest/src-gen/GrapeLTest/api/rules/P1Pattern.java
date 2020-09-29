package GrapeLTest.api.rules;

import Flights.Airport;
import GrapeLTest.api.GrapeLTestAPI;
import GrapeLTest.api.matches.P1Match;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.gt.api.GraphTransformationPattern;
import org.emoflon.ibex.gt.engine.GraphTransformationInterpreter;

/**
 * The pattern [org.emoflon.ibex.patternmodel.IBeXPatternModel.impl.IBeXNodeImpl@2a942ef2 (name: airport)] which does the following:
 * If this pattern is not self-explaining, you really should add some comment in the specification.
 */
@SuppressWarnings("unused")
public class P1Pattern extends GraphTransformationPattern<P1Match, P1Pattern> {
	private static String patternName = "p1";

	/**
	 * Creates a new pattern p1().
	 * 
	 * @param api
	 *            the API the pattern belongs to
	 * @param interpreter
	 *            the interpreter
	 */
	
	public P1Pattern(final GrapeLTestAPI api, final GraphTransformationInterpreter interpreter) {
		super(api, interpreter, patternName);
	}

	@Override
	protected P1Match convertMatch(final IMatch match) {
		return new P1Match(this, match);
	}

	@Override
	protected List<String> getParameterNames() {
		List<String> names = new ArrayList<String>();
		names.add("airport");
		return names;
	}

	/**
	 * Binds the node airport to the given object.
	 *
	 * @param object
	 *            the object to set
	 */
	public P1Pattern bindAirport(final Airport object) {
		parameters.put("airport", Objects.requireNonNull(object, "airport must not be null!"));
		return this;
	}
	
	@Override
	public String toString() {
		String s = "pattern " + patternName + " {" + System.lineSeparator();
		s += "	airport --> " + parameters.get("airport") + System.lineSeparator();
		s += "}";
		return s;
	}
}
