package org.emoflon.grapel.testsuite.abc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ABCGrapeL.grapel.Abc.eventhandler.AInElementsEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.ApamaExternalAEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.ApamaExternalBEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.ApamaExternalCEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.BInElementsEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.CInElementsEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.DoubleCTypecastEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.IntegerATypecastEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.StringBTypecastEventHandler;

public class ABCTypecastTest extends ABCAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void integerTypecast() {
		AInElementsEventHandler aHandler = api.getAInElementsEventHandler();
		IntegerATypecastEventHandler intHandler = api.getIntegerATypecastEventHandler();
		ApamaExternalAEventHandler extAHandler = api.getApamaExternalAEventHandler();

		api.update();
		assertEquals(3, aHandler.getAllEvents().size());
		assertEquals(3, intHandler.getAllEvents().size());
		assertEquals(2,extAHandler.getAllEvents().size());
	}
	@Test
	public void stringTypecast() {
		BInElementsEventHandler bHandler = api.getBInElementsEventHandler();
		StringBTypecastEventHandler stringHandler = api.getStringBTypecastEventHandler();
		ApamaExternalBEventHandler extBHandler = api.getApamaExternalBEventHandler();

		api.update();
		assertEquals(3, bHandler.getAllEvents().size());
		assertEquals(3, stringHandler.getAllEvents().size());
		assertEquals(2,extBHandler.getAllEvents().size());
	}
	@Test
	public void doubleTypecast() {
		CInElementsEventHandler cHandler = api.getCInElementsEventHandler();
		DoubleCTypecastEventHandler doubleHandler = api.getDoubleCTypecastEventHandler();
		ApamaExternalCEventHandler extCHandler = api.getApamaExternalCEventHandler();

		api.update();
		assertEquals(3, cHandler.getAllEvents().size());
		assertEquals(3, doubleHandler.getAllEvents().size());
		assertEquals(2,extCHandler.getAllEvents().size());
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
