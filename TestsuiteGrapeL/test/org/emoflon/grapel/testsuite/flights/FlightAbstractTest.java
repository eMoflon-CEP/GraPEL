package org.emoflon.grapel.testsuite.flights;

import java.util.LinkedList;

import org.emoflon.flight.model.util.LongDateHelper;
import org.emoflon.grapel.testsuite.GrapeLAppTestCase;

import Flights.Flight;
import Flights.FlightContainer;
import Flights.FlightModel;
import FlightsGrapeL.grapel.Flight.FlightGrapeLAPI;
import FlightsGrapeL.grapel.Flight.FlightGrapeLHiPEEngine;
import FlightsGrapeL.grapel.Flight.eventhandler.ModelContainerEventHandler;

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
	protected FlightModel getModel() {
		ModelContainerEventHandler containerHandler = api.getModelContainerEventHandler();
		LinkedList<FlightModel> models = new LinkedList<FlightModel>();

		api.update();
		containerHandler.getAllEvents().forEach(event -> models.add(event.getModel()));
		FlightModel model = models.get(0);
		
		return model;
	}
	protected Flight findFlight(FlightContainer flights, String ID) {
		for (Flight flight : flights.getFlights())
			if (flight.getID().equals(ID))
				return flight;
		System.out.println("No matching flight");
		return null;
	}
	protected void delayFlight(Flight flight) {
		changeFlightArrival(flight, 1000);
	}
	protected void hurryFlight(Flight flight) {
		changeFlightArrival(flight, -1000);
	}
	protected void changeFlightArrival(Flight flight, long duration) {
		flight.setArrival(LongDateHelper.createTimeStamp(flight.getArrival(), duration));
	}
}
