package org.emoflon.grapel.testsuite.abc;

import java.util.LinkedList;

import org.emoflon.grapel.testsuite.GrapeLAppTestCase;

import ABC.ABCContainer;
import ABC.ABCFactory;
import ABC.D;
import ABC.E;
import ABC.F;
import ABCGrapeL.grapel.Abc.AbcGrapeLAPI;
import ABCGrapeL.grapel.Abc.AbcGrapeLHiPEEngine;
import ABCGrapeL.grapel.Abc.eventhandler.ContainerEventHandler;


public abstract class ABCAbstractTest extends GrapeLAppTestCase<AbcGrapeLAPI> {
	AbcGrapeLAPI api;
	private ABCFactory factory = ABCFactory.eINSTANCE;
	@Override
	protected String getTestName() {
		return "ABC";
	}
	@Override
	protected AbcGrapeLAPI init(String modelInstanceFileName) {
		AbcGrapeLAPI api = new AbcGrapeLHiPEEngine();
		initAPI(api,modelInstanceFileName);
		return api;
	}

	protected ABCContainer getModel() {
		ContainerEventHandler containerHandler = api.getContainerEventHandler();
		LinkedList<ABCContainer> models = new LinkedList<ABCContainer>();
		
		api.update();
		containerHandler.getAllEvents().forEach(event -> models.add(event.getAbc()));
		ABCContainer model = models.get(0);
		
		return model;
	}
	
	protected F createFElement() {		
		return createFElement(0,0,0,"");
	}
	protected F createFElement(double d, float fl, int i, String str) {
		F f = factory.createF();
		
		f.setD(d);
		f.setF(fl);
		f.setI(i);
		f.setS(str);
		
		return f;
	}
	
	protected D createDElement() {		
		return createDElement(0,0,10,"");
	}
	protected D createDElement(double doub, float f, int i, String str) {
		D d = factory.createD();
		
		d.setD(doub);
		d.setF(f);
		d.setI(i);
		d.setS(str);
		
		return d;
	}
	
	protected E createEElement() {		
		return createEElement(0,0,0,"");
	}
	
	protected E createEElement(double d, float f, int i, String str) {
		E e = factory.createE();
		
		e.setD(d);
		e.setF(f);
		e.setI(i);
		e.setS(str);
		
		return e;
	}
}
