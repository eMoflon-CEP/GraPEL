package org.emoflon.grapel.testsuite.flights;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Flights.FlightModel;
import FlightsGrapeL.grapel.Flight.eventhandler.ContainerEventHandler;
import FlightsGrapeL.grapel.Flight.eventhandler.ModelContainerEventHandler;

public class FlightGeneralTest extends FlightAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}

	@Test
	public void includes1Model() {
		ModelContainerEventHandler containerHandler = api.getModelContainerEventHandler();

		api.update();
		assertEquals(1, containerHandler.getAllEvents().size());
	}
	
	@Test
	public void includes1ModelwoCEP() {
		ContainerEventHandler containerHandler = api.getContainerEventHandler();

		api.update();
		assertEquals(1, containerHandler.getAllEvents().size());
	}
	
	@Test
	public void modelIncludes3Flights() {
		FlightModel model = getModel();
		
		assertEquals(3, model.getFlights().getFlights().size());		
	}
	
	@Test
	public void modelIncludes8Persons() {
		FlightModel model = getModel();
		
		assertEquals(8, model.getPersons().getPersons().size());		
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
