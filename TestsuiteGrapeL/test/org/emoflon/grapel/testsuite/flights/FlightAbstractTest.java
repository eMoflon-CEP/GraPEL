package org.emoflon.grapel.testsuite.flights;

import org.emoflon.grapel.testsuite.GrapeLAppTestCase;

import FlightsGrapeL.grapel.Flight.FlightGrapeLAPI;
import FlightsGrapeL.grapel.Flight.FlightGrapeLHiPEEngine;

public abstract class FlightAbstractTest extends GrapeLAppTestCase<FlightGrapeLAPI> {
	FlightGrapeLAPI api;
	@Override
	protected String getTestName() {
		return "Flights";
	}
	@Override
	protected FlightGrapeLAPI init(String modelInstanceFileName) {
		FlightGrapeLAPI api = new FlightGrapeLHiPEEngine();
		initAPI(api,modelInstanceFileName);
		return api;
	}
}
