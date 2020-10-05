package org.emoflon.grapel.testsuite.flights;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Flights.Flight;
import FlightsGrapeL.grapel.Flight.eventhandler.ConnectingFlightNotReachableEventHandler;
import FlightsGrapeL.grapel.Flight.eventhandler.FlightDelayedEventHandler;

public class FlightConnectingTest extends FlightAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
//	@Test
//	public void modelIncludes2ConnectingFlights() {
//		// Funktioniert in aktueller Implementierung nicht, da durch Pattern Matches generierte Events
//		// sich nicht in dem EventHandler wiederfinden lassen
//	}
	
	@Test
	public void hurryFirstFlightMultipleTimes() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		for(int i = 0; i<4;i++) {
			changeFlightArrival(flight,-1000);
			api.update();
			assertEquals(0, delayedHandler.getAllEvents().size());
			assertEquals(0, cfHandler.getAllEvents().size());
		}
	}
	
	@Test
	public void delayFirstFlightMultipleTimes() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		for(int i = 0; i<4;i++) {
			changeFlightArrival(flight,1000);
			api.update();
			assertEquals(1, delayedHandler.getAllEvents().size());
			assertEquals(1, cfHandler.getAllEvents().size());
		}
	}
	
	@Test
	public void delayAndHurryFirstFlightMultipleTimes() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		for(int i = 0; i<4;i++) {
			changeFlightArrival(flight,1000);
			api.update();
			assertEquals(1, delayedHandler.getAllEvents().size());
			assertEquals(1, cfHandler.getAllEvents().size());
			
			changeFlightArrival(flight,-1000);
			api.update();
			assertEquals(0, delayedHandler.getAllEvents().size());
			assertEquals(0, cfHandler.getAllEvents().size());
		}
	}
	
	@Test
	public void delayFirstFlightAdvance() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		changeFlightArrival(flight,10000);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
		assertEquals(0, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,300000);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
		assertEquals(1, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,800000);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
		assertEquals(2, cfHandler.getAllEvents().size());
	}
	
	@Test
	public void delayAndHurryFirstFlightAdvance() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		changeFlightArrival(flight,100000);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
		assertEquals(0, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,300000);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
		assertEquals(1, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,-400000);
		api.update();
		assertEquals(0, delayedHandler.getAllEvents().size());
		assertEquals(0, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,1500000);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
		assertEquals(2, cfHandler.getAllEvents().size());
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
