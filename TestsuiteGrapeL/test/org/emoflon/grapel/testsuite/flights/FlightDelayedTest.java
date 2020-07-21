package org.emoflon.grapel.testsuite.flights;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FlightsGrapeL.grapel.Flight.eventhandler.FlightDelayedEventHandler;
import FlightsGrapeL.grapel.Flight.eventhandler.FlightWithArrivalEventHandler;

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
	public void delayAllFlightsMultipleTimes() {
		FlightDelayedEventHandler delayedHandler = api.getFlightDelayedEventHandler();
		
		//getFlights
		FlightWithArrivalEventHandler flightHandler = api.getFlightWithArrivalEventHandler();
		
		// TODO: UPDATE flights
		
//		//delay all flights
//		flightHandler.getNewEvents().forEach(event -> event.getArrival().setTime(event.getArrival().getTime()+100000));
//		api.update();
//		assertEquals(3, delayedHandler.getAllEvents().size());
//		// update all flights to be on time
//		flightHandler.getNewEvents().forEach(event -> event.getArrival().setTime(event.getArrival().getTime()-100000));
//		api.update();
//		assertEquals(0, delayedHandler.getAllEvents().size());
//		
//		//delay all flights
//		flightHandler.getNewEvents().forEach(event -> event.getArrival().setTime(event.getArrival().getTime()+100000));
//		api.update();
//		assertEquals(3, delayedHandler.getAllEvents().size());
//		// update all flights to be on time
//		flightHandler.getNewEvents().forEach(event -> event.getArrival().setTime(event.getArrival().getTime()-100000));
//		api.update();
//		assertEquals(0, delayedHandler.getAllEvents().size());
//		
//		//delay all flights
//		flightHandler.getNewEvents().forEach(event -> event.getArrival().setTime(event.getArrival().getTime()+100000));
//		api.update();
//		assertEquals(3, delayedHandler.getAllEvents().size());
//		// update all flights to be on time
//		flightHandler.getNewEvents().forEach(event -> event.getArrival().setTime(event.getArrival().getTime()-100000));
//		api.update();
//		assertEquals(0, delayedHandler.getAllEvents().size());
	}
	@Test
	public void delayOneFlightMultipleTimes() {
		// TODO: fill in
	}
	@Test
	public void hurryOneFlight() {
		// TODO: fill in
	}
	
	@After
	public void dispose() {
		api.dispose();
	}
}
