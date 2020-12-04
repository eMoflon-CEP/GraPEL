package org.emoflon.grapel.testsuite.flights;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Flights.Flight;
import Flights.Travel;
import FlightsGrapeL.grapel.Flight.eventhandler.ConnectingFlightNotReachableEventHandler;

public class FlightRuleTest extends FlightAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void tryRebookCFOnDelayedFlight() {
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		Flight cf = findFlight(getModel().getFlights(), "FRA->BER_1");
		Flight altCF = findFlight(getModel().getFlights(), "FRA->BER_2");
		
		api.update();
		assertEquals(0, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,300000);
		api.update();
		
		LinkedList<Travel> delayedTravels = new LinkedList<Travel>();
		assertEquals(2, cfHandler.getAllEvents().size());
		cfHandler.getAllEvents().forEach(event -> {
			delayedTravels.add(event.getTravel());
		});
		
		api.setRuleAutoApply(true);
		api.update();
		
		assertEquals(true, delayedTravels.get(0).getFlights().contains(altCF));
		assertEquals(true, delayedTravels.get(1).getFlights().contains(altCF));
		
		assertEquals(false, delayedTravels.get(0).getFlights().contains(cf));
		assertEquals(false, delayedTravels.get(1).getFlights().contains(cf));
		
		
		
	}
	
	@After
	public void dispose() {
		api.dispose();
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
