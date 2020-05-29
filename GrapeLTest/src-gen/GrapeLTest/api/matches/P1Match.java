package GrapeLTest.api.matches;

import Flights.Airport;
import GrapeLTest.api.rules.P1Pattern;
import org.emoflon.ibex.common.operational.IMatch;
import org.emoflon.ibex.gt.api.GraphTransformationMatch;

/**
 * A match for the pattern <code>p1()</code>.
 */
public class P1Match extends GraphTransformationMatch<P1Match, P1Pattern> {
	private Airport varAirport;

	/**
	 * Creates a new match for the pattern <code>p1()</code>.
	 * 
	 * @param pattern
	 *            the pattern
	 * @param match
	 *            the untyped match
	 */
	public P1Match(final P1Pattern pattern, final IMatch match) {
		super(pattern, match);
		varAirport = (Airport) match.get("airport");
	}

	/**
	 * Returns the airport.
	 *
	 * @return the airport
	 */
	public Airport getAirport() {
		return varAirport;
	}

	@Override
	public String toString() {
		String s = "match {" + System.lineSeparator();
		s += "	airport --> " + varAirport + System.lineSeparator();
		s += "} for " + getPattern();
		return s;
	}
}
