package org.emoflon.grapel.testsuite.abc;

import java.util.ArrayList;

import org.emoflon.grapel.testsuite.GrapeLAppTestCase;

import ABC.ABCContainer;
import ABC.ABCFactory;
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
		ArrayList<ABCContainer> models = new ArrayList<ABCContainer>();
		
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
}
