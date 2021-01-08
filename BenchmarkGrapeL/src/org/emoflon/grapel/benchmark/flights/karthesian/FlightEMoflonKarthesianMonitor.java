package org.emoflon.grapel.benchmark.flights.karthesian;

import org.emoflon.grapel.benchmark.PatternMatchingEngine;
import org.emoflon.grapel.benchmark.flights.FlightMonitor;

import FlightGrapeLLoadEMoflon.grapel.Flightemoflon.FlightemoflonGrapeLAPI;
import FlightGrapeLLoadEMoflon.grapel.Flightemoflon.FlightemoflonGrapeLDemoclesEngine;
import FlightGrapeLLoadEMoflon.grapel.Flightemoflon.FlightemoflonGrapeLHiPEEngine;
import FlightGrapeLLoadEMoflon.grapel.Flightemoflon.FlightemoflonGrapeLViatraEngine;

public class FlightEMoflonKarthesianMonitor extends FlightMonitor<FlightemoflonGrapeLAPI> {
	@Override
	protected String getBenchmarkName() {
		return "KarthesianLoad";
	}


	@Override
	public FlightemoflonGrapeLAPI init(String modelInstanceFileName, PatternMatchingEngine engine) {
		FlightemoflonGrapeLAPI api;
		switch (engine) {
			case HiPE: api = new FlightemoflonGrapeLHiPEEngine(); break;
			case Viatra: api = new FlightemoflonGrapeLViatraEngine(); break;
			case Democles: api = new FlightemoflonGrapeLDemoclesEngine(); break;
			default: api = new FlightemoflonGrapeLHiPEEngine(); break;
		}
		initAPI(api,modelInstanceFileName);
		this.api = api;
		return api;
	}
}
