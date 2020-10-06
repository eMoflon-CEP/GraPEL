package org.emoflon.grapel.testsuite.flights;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Flights.Flight;
import FlightsGrapeL.grapel.Flight.eventhandler.FlightDelayedEventHandler;

public class FlightDelayedTest extends FlightAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void startAtZero() {		
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		
		api.update();
		assertEquals(0, delayedHandler.getAllEvents().size());
	}
	@Test
	public void delaySingleFlightOnce(){
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		//get Flight
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		//delay muc_fra_1 flights
		delayFlight(flight);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
	}
	@Test
	public void hurrySingleFlightOnce(){
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		//hurry muc_fra_1 flights
		hurryFlight(flight);
		api.update();
		assertEquals(0, delayedHandler.getAllEvents().size());
	}
	
	@Test
	public void delaySingleFlightsMultipleTimes() {	
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		for(int i=0; i<4;i++) {
			//delay muc_fra_1 flights
			delayFlight(flight);
			api.update();
			assertEquals(i+1, delayedHandler.getAllEvents().size());
		}
	}
	
	@Test
	public void hurrySingleFlightsMultipleTimes() {	
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		for(int i=0; i<4;i++) {
			//hurry muc_fra_1 flights
			hurryFlight(flight);
			api.update();
			assertEquals(0, delayedHandler.getAllEvents().size());
		}
	}

	@Test
	public void hurryAndDelayOneFlight() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		//delay muc_fra_1 flights
		delayFlight(flight);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
		//hurry muc_fra_1 flights
		hurryFlight(flight);
		api.update();
		assertEquals(1, delayedHandler.getAllEvents().size());
	}
	
	@Test
	public void hurryAndDelayOneFlightMultipleTimes() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		Flight flight = findFlight(getModel().getFlights(), "MUC->FRA_1");
		
		for(int i=0; i<4;i++) {
			//delay muc_fra_1 flights
			delayFlight(flight);
			api.update();
			assertEquals(i+1, delayedHandler.getAllEvents().size());
			//hurry muc_fra_1 flights
			hurryFlight(flight);
			api.update();
			assertEquals(i+1, delayedHandler.getAllEvents().size());
		}
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
