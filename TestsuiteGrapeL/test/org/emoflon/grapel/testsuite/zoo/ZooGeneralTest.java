package org.emoflon.grapel.testsuite.zoo;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ZooGrapeL.grapel.Zoo.eventhandler.ContainerEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.EnviromentMissmatchWarningEventHandler;
import zoo.Enclosure;
import zoo.Zoo;


public class ZooGeneralTest extends ZooAbstractTest {
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
	public void modelIncludesEnclosuruesWithAnimals() {
		Zoo model = getModel();
		
		api.update();
		assertEquals(4, model.getEnclosures().getEnclosures().size());
		
		int i = 0;
		for(Enclosure e: model.getEnclosures().getEnclosures())
			i = i + e.getAnimals().size();
		assertEquals(12,i);
	}
	@Test
	public void includes4EnviromentWarnings() {
		EnviromentMissmatchWarningEventHandler enviromentHandler = api.getEnviromentMissmatchWarningEventHandler();
		
		api.update();
		assertEquals(4, enviromentHandler.getAllEvents().size());
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
