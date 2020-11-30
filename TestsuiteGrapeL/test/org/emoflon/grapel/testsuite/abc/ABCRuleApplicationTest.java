package org.emoflon.grapel.testsuite.abc;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ABC.ABCContainer;
import ABC.D;
import ABCGrapeL.grapel.Abc.eventhandler.ChangeIntInDEventHandler;

public class ABCRuleApplicationTest extends ABCAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void testSimpleRuleApplicationForDElement() {
		ABCContainer abc = getModel();
		D d = createDElement();

		ChangeIntInDEventHandler changeDHandler = api.getChangeIntInDEventHandler();
		
		api.setRuleAutoApply(true);
		api.getChangeIntInDEventHandler().applyAutmatically();
		
		api.update();
		assertEquals(0, changeDHandler.getAllEvents().size());
		assertEquals(10, d.getI());
		
		abc.getElements().getElement().add(d);
		
		api.update();
		api.getChangeIntInDEventHandler().applyAutmatically();
		assertEquals(1, changeDHandler.getAllEvents().size());
		assertEquals(20, d.getI());
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
