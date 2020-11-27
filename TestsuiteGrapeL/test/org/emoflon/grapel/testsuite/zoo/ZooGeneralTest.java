package org.emoflon.grapel.testsuite.zoo;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ZooGrapeL.grapel.Zoo.eventhandler.AnimalsWithSameAgeEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.AnimalsWithSameNameAndAge1EventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.AnimalsWithSameNameAndAge2EventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.AnimalsWithSameNameAndAge3EventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.AnimalsWithSameNameEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.ContainerEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.EnviromentMissmatchWarningEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.HumidityWarningEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.MoreAnimalsThanCapacityEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.TempartureWarningEventHandler;
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
	public void produces4EnviromentWarnings() {
		EnviromentMissmatchWarningEventHandler enviromentHandler = api.getEnviromentMissmatchWarningEventHandler();
		
		api.update();
		assertEquals(4, enviromentHandler.getAllEvents().size());
	}
	@Test
	public void produces4TemperatureWarnings() {
		TempartureWarningEventHandler temperatureHandler = api.getTempartureWarningEventHandler();
		
		api.update();
		assertEquals(4, temperatureHandler.getAllEvents().size()); // needs to change, if mixed is ignored
	}
	@Test
	public void produces5HumidityWarnings() {
		HumidityWarningEventHandler humidityHandler = api.getHumidityWarningEventHandler();
		
		api.update();
		assertEquals(5, humidityHandler.getAllEvents().size()); // needs to change, if mixed is ignored
	}
	@Test
	public void produces1overfilledWarning() {
		MoreAnimalsThanCapacityEventHandler capacityHandler = api.getMoreAnimalsThanCapacityEventHandler();
		
		api.update();
		assertEquals(1, capacityHandler.getAllEvents().size());
	}
	@Test
	public void producesSameAgeAndNameWarning() {
		AnimalsWithSameNameAndAge1EventHandler sameNameAndAge1 = api.getAnimalsWithSameNameAndAge1EventHandler();
		AnimalsWithSameNameAndAge2EventHandler sameNameAndAge2 = api.getAnimalsWithSameNameAndAge2EventHandler();
		AnimalsWithSameNameAndAge3EventHandler sameNameAndAge3 = api.getAnimalsWithSameNameAndAge3EventHandler();
		
		AnimalsWithSameNameEventHandler sameName = api.getAnimalsWithSameNameEventHandler();
		AnimalsWithSameAgeEventHandler sameAge = api.getAnimalsWithSameAgeEventHandler();
		
		api.update();
		assertEquals(4, sameName.getAllEvents().size());
		assertEquals(4, sameAge.getAllEvents().size());
		
		
		assertEquals(2, sameNameAndAge1.getAllEvents().size());
		assertEquals(4, sameNameAndAge2.getAllEvents().size()); // double because doubled name and Events
		assertEquals(2, sameNameAndAge3.getAllEvents().size());
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
