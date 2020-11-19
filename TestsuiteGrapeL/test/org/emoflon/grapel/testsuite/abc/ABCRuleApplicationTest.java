package org.emoflon.grapel.testsuite.abc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ABC.ABCContainer;
import ABC.D;
import ABCGrapeL.grapel.Abc.eventhandler.ChangeIntInDEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.DInElementsEventHandler;

public class ABCRuleApplicationTest extends ABCAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void testSimpleRuleApplicationForDElement1() {
		ABCContainer abc = getModel();
		D d = createDElement();
		ArrayList<D> dElements = new ArrayList<D>();
		
		// double functions to check obfuscation -> 2nd handler, array and foreach
		// get removed when rule application is working
		DInElementsEventHandler dHandler = api.getDInElementsEventHandler();
		ChangeIntInDEventHandler changeDHanler = api.getChangeIntInDEventHandler();
		
		
		api.update();
		assertEquals(0, dHandler.getAllEvents().size());
		assertEquals(10, d.getI());
		
		abc.getElements().getElement().add(d);
		
		api.update();
		assertEquals(1, dHandler.getAllEvents().size());
		assertEquals(1, changeDHanler.getAllEvents().size());
		
		
		changeDHanler.getAllEvents().forEach(event -> dElements.add(event.getD()));
		
		assertEquals(true, dElements.contains(d));
		assertEquals(20, dElements.get(0).getI());
		assertEquals(20, d.getI());
	}
	
	@Test
	public void testSimpleRuleApplicationForDElement() {
		ABCContainer abc = getModel();
		D d = createDElement();

		ChangeIntInDEventHandler changeDHanler = api.getChangeIntInDEventHandler();
		
		api.setRuleAutoApply(true);
		api.getChangeIntInDEventHandler().applyAutmatically();
		
		api.update();
		assertEquals(0, changeDHanler.getAllEvents().size());
		assertEquals(10, d.getI());
		
		abc.getElements().getElement().add(d);
		
		api.update();
		assertEquals(1, changeDHanler.getAllEvents().size());
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
