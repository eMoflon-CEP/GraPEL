package org.emoflon.grapel.testsuite.abc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ABCGrapeL.grapel.Abc.eventhandler.AInElementsEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.BInElementsEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.CInElementsEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.ContainerEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.FInElementsEventHandler;


public class ABCGeneralTest extends ABCAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void includes1Model() {
		ContainerEventHandler containerHandler = api.getContainerEventHandler();

		api.update();
		assertEquals(1, containerHandler.getAllEvents().size());
	}
	@Test
	public void includesElements() {
		AInElementsEventHandler aHandler = api.getAInElementsEventHandler();
		BInElementsEventHandler bHandler = api.getBInElementsEventHandler();
		CInElementsEventHandler cHandler = api.getCInElementsEventHandler();
		FInElementsEventHandler fHandler = api.getFInElementsEventHandler();

		api.update();
		assertEquals(3, aHandler.getAllEvents().size());
		assertEquals(3, bHandler.getAllEvents().size());
		assertEquals(3, cHandler.getAllEvents().size());
		assertEquals(0, fHandler.getAllEvents().size());
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
