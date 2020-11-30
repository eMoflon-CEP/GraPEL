package org.emoflon.grapel.testsuite.zoo;

import java.util.LinkedList;

import org.emoflon.grapel.testsuite.GrapeLAppTestCase;

import ZooGrapeL.grapel.Zoo.ZooGrapeLAPI;
import ZooGrapeL.grapel.Zoo.ZooGrapeLHiPEEngine;
import ZooGrapeL.grapel.Zoo.eventhandler.ContainerEventHandler;
import zoo.Zoo;

public abstract class ZooAbstractTest extends GrapeLAppTestCase<ZooGrapeLAPI> {
	ZooGrapeLAPI api;
	@Override
	protected String getTestName() {
		return "Zoo";
	}
	@Override
	protected ZooGrapeLAPI init(String modelInstanceFileName) {
		ZooGrapeLAPI api = new ZooGrapeLHiPEEngine();
		initAPI(api,modelInstanceFileName);
		return api;
	}
	protected Zoo getModel() {
		ContainerEventHandler containerHandler = api.getContainerEventHandler();
		LinkedList<Zoo> models = new LinkedList<Zoo>();

		api.update();
		containerHandler.getAllEvents().forEach(event -> models.add(event.getZoo()));
		Zoo model = models.get(0);
		
		return model;
	}
}
