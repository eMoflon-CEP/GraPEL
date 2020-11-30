package org.emoflon.grapel.testsuite.abc;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ABC.ABCContainer;
import ABC.E;
import ABCGrapeL.grapel.Abc.eventhandler.ApamaExternalEEventHandler;
import ABCGrapeL.grapel.Abc.eventhandler.EInElementsEventHandler;

public class ABCObjectObfuscationTest extends ABCAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	
	@Test
	public void testObjectObfuscationWithEElement() {
		ABCContainer abc = getModel();
		E e1 = createEElement();
		E e2 = createEElement();
		E e3 = createEElement();
		
		LinkedList<E> eHandlerEList = new LinkedList<E>();
		LinkedList<E> externalEHandlerEList = new LinkedList<E>();
		
		EInElementsEventHandler eHandler = api.getEInElementsEventHandler();
		ApamaExternalEEventHandler externalEHandler = api.getApamaExternalEEventHandler();
		
		api.update();
		assertEquals(0, eHandler.getAllEvents().size());
		assertEquals(0, externalEHandler.getAllEvents().size());
		
		abc.getElements().getElement().add(e1);
		api.update();
		assertEquals(1, eHandler.getAllEvents().size());
		assertEquals(1, externalEHandler.getAllEvents().size());
		
		eHandler.getAllEvents().forEach(event -> eHandlerEList.add(event.getE()));
		externalEHandler.getAllEvents().forEach(event -> externalEHandlerEList.add(event.getE()));
		
		assertEquals(1, eHandlerEList.size());
		assertEquals(1, externalEHandlerEList.size());
		
		assertEquals(true, eHandlerEList.contains(e1));
		assertEquals(true, externalEHandlerEList.contains(e1));
		assertEquals(false, eHandlerEList.contains(e2) || eHandlerEList.contains(e3));
		assertEquals(false, externalEHandlerEList.contains(e2) || externalEHandlerEList.contains(e3));
		
		// dirty reset
		eHandlerEList.clear();
		externalEHandlerEList.clear();
		
		
		abc.getElements().getElement().add(e2);
		abc.getElements().getElement().add(e3);
		api.update();
		assertEquals(3, eHandler.getAllEvents().size());
		assertEquals(3, externalEHandler.getAllEvents().size());
		
		eHandler.getAllEvents().forEach(event -> eHandlerEList.add(event.getE()));
		externalEHandler.getAllEvents().forEach(event -> externalEHandlerEList.add(event.getE()));
		
		assertEquals(3, eHandlerEList.size());
		assertEquals(3, externalEHandlerEList.size());
		
		assertEquals(true, eHandlerEList.contains(e1) && eHandlerEList.contains(e2) && eHandlerEList.contains(e3));
		assertEquals(true, externalEHandlerEList.contains(e1) && externalEHandlerEList.contains(e2) && externalEHandlerEList.contains(e3));
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
