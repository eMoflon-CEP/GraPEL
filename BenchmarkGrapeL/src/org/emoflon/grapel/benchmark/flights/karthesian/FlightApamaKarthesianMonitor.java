package org.emoflon.grapel.benchmark.flights.karthesian;

import org.emoflon.grapel.benchmark.PatternMatchingEngine;
import org.emoflon.grapel.benchmark.flights.FlightMonitor;

import FlightGrapeLLoadApama.grapel.Flightapama.FlightapamaGrapeLAPI;
import FlightGrapeLLoadApama.grapel.Flightapama.FlightapamaGrapeLDemoclesEngine;
import FlightGrapeLLoadApama.grapel.Flightapama.FlightapamaGrapeLHiPEEngine;
import FlightGrapeLLoadApama.grapel.Flightapama.FlightapamaGrapeLViatraEngine;

public class FlightApamaKarthesianMonitor extends FlightMonitor<FlightapamaGrapeLAPI> {
	@Override
	protected String getBenchmarkName() {
		return "KarthesianLoad";
	}


	@Override
	public FlightapamaGrapeLAPI init(String modelInstanceFileName, PatternMatchingEngine engine) {
		FlightapamaGrapeLAPI api;
		switch (engine) {
			case HiPE: api = new FlightapamaGrapeLHiPEEngine(); break;
			case Viatra: api = new FlightapamaGrapeLViatraEngine(); break;
			case Democles: api = new FlightapamaGrapeLDemoclesEngine(); break;
			default: api = new FlightapamaGrapeLHiPEEngine(); break;
		}
		initAPI(api,modelInstanceFileName);
		this.api = api;
		return api;
	}
}
