package org.emoflon.grapel.testsuite.flights;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Flights.Flight;
import FlightsGrapeL.grapel.Flight.eventhandler.ConnectingFlightNotReachableEventHandler;
import FlightsGrapeL.grapel.Flight.eventhandler.FlightDelayedEventHandler;
import FlightsGrapeL.grapel.Flight.eventhandler.TravelHasConnectingFlightEventHandler;

public class FlightConnectingTest extends FlightAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void modelIncludes2ConnectingFlights() {
		TravelHasConnectingFlightEventHandler tcfHandler = api.getTravelHasConnectingFlightEventHandler();
		
		api.update();
		assertEquals(2, tcfHandler.getAllEvents().size());
	}
	
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
	public void delayFirstFlight() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		api.update();
		assertEquals(0, delayedHandler.getAllEvents().size());
		assertEquals(0, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,299999);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
		cfHandler.getAllEvents().forEach(event -> System.out.println(event));
		assertEquals(2, cfHandler.getAllEvents().size());
	}
	
	@Test
	public void delayFirstFlightMultipleTimes() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		for(int i = 0; i<4;i++) {
			changeFlightArrival(flight,300000);
			api.update();
			assertEquals(i+1, delayedHandler.getAllEvents().size());
			assertEquals(2*(i+1), cfHandler.getAllEvents().size());
		}
	}
	
	@Test
	public void delayAndHurryFirstFlightMultipleTimes() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		ConnectingFlightNotReachableEventHandler cfHandler = api.getConnectingFlightNotReachableEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		for(int i = 0; i<4;i++) {
			changeFlightArrival(flight,300000);
			api.update();
			assertEquals(i+1, delayedHandler.getAllEvents().size());
			assertEquals(2*(i+1), cfHandler.getAllEvents().size());
			
			changeFlightArrival(flight,-300000);
			api.update();
			assertEquals(i+1, delayedHandler.getAllEvents().size());
			assertEquals(2*(i+1), cfHandler.getAllEvents().size());
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
		
		changeFlightArrival(flight,289999);
		api.update();
		assertEquals(2, delayedHandler.getAllEvents().size());
		assertEquals(2, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,800000);
		api.update();
		assertEquals(3, delayedHandler.getAllEvents().size());
		assertEquals(4, cfHandler.getAllEvents().size());
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
		assertEquals(2, delayedHandler.getAllEvents().size());
		assertEquals(2, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,-400000);
		api.update();
		assertEquals(2, delayedHandler.getAllEvents().size());
		assertEquals(2, cfHandler.getAllEvents().size());
		
		changeFlightArrival(flight,1500000);
		api.update();
		assertEquals(3, delayedHandler.getAllEvents().size());
		assertEquals(4, cfHandler.getAllEvents().size());
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
