package org.emoflon.grapel.testsuite.flights;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FlightConnectingTest extends FlightAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void connectingFlightNotReachable() {
		// TODO: fill in
	}
	
	@After
	public void dispose() {
		api.dispose();
	}
}
