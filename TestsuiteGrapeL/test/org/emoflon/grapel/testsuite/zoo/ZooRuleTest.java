package org.emoflon.grapel.testsuite.zoo;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ZooGrapeL.grapel.Zoo.eventhandler.ApeKleeWarningEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.MoreAnimalsThanCapacityEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.MoveAnimalToEnclousureWithFreeSpaceEventHandler;
import ZooGrapeL.grapel.Zoo.eventhandler.RenameApeEventHandler;
import zoo.Animal;
import zoo.Ape;

public class ZooRuleTest extends ZooAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	@Test
	public void ruleAppliesNewNameToApeKlee() {
		ApeKleeWarningEventHandler kleeWarningEventHandler = api.getApeKleeWarningEventHandler();
		RenameApeEventHandler renameApeEventHandler = api.getRenameApeEventHandler();
		
		api.setRuleAutoApply(true);	
		assertEquals(0, kleeWarningEventHandler.getAllEvents().size());
		assertEquals(0, renameApeEventHandler.getAllEvents().size());
		
		api.update();
		assertEquals(1, kleeWarningEventHandler.getAllEvents().size());
		assertEquals(3, renameApeEventHandler.getAllEvents().size());
		
		LinkedList<Ape> apes = new LinkedList<Ape>();
		kleeWarningEventHandler.getAllEvents().forEach(event -> apes.add(event.getAnimal()));

		long richterCount = apes.stream().filter(ape -> ape.getName().equals("Richter")).count();
		long kleeCount = apes.stream().filter(ape -> ape.getName().equals("Klee")).count();
		assertEquals(1, richterCount);
		assertEquals(0, kleeCount);
	}
	@Test
	public void ruleAppliesOnOverfilledEnclosure() {
		MoveAnimalToEnclousureWithFreeSpaceEventHandler moveHandler = api.getMoveAnimalToEnclousureWithFreeSpaceEventHandler();
		MoreAnimalsThanCapacityEventHandler capacityHandler = api.getMoreAnimalsThanCapacityEventHandler();
		
		assertEquals(0, moveHandler.getAllEvents().size());
		
		api.update();
		
		assertEquals(1, capacityHandler.getNewEvents().size());
		assertEquals(8, moveHandler.getNewEvents().size());
		
		api.setRuleAutoApply(true);
		api.update();
		
		assertEquals(0, capacityHandler.getNewEvents().stream().filter(event -> !(Boolean)event.getField("vanished")).count());
		assertEquals(0, moveHandler.getNewEvents().stream().filter(event -> !(Boolean)event.getField("vanished")).count());
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
