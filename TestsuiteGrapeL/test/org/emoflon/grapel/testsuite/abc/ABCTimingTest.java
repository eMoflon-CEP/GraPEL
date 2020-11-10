package org.emoflon.grapel.testsuite.abc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ABC.ABCContainer;
import ABC.F;
import ABCGrapeL.grapel.Abc.eventhandler.ApamaExternalFEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.ApamaInternalFEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.FInElementsEventHandler;

public class ABCTimingTest extends ABCAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void createFAndCheckTiming() {
		ABCContainer abc = getModel();
		F f = createFElement();
		
		FInElementsEventHandler fHandler = api.getFInElementsEventHandler();
		ApamaInternalFEventHandler internalFHandler = api.getApamaInternalFEventHandler();
		ApamaExternalFEventHandler externalFHandler = api.getApamaExternalFEventHandler();
		
		api.update();
		assertEquals(0, fHandler.getAllEvents().size());
		assertEquals(0, internalFHandler.getAllEvents().size());
		assertEquals(0, externalFHandler.getAllEvents().size());
		
		abc.getElements().getElement().add(f);
		
		api.update();
		assertEquals(1, fHandler.getAllEvents().size());
		assertEquals(1000, internalFHandler.getAllEvents().size());
		assertEquals(1, externalFHandler.getAllEvents().size());
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
