package org.emoflon.grapel.testsuite.mnetwork;


import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import MNetworkGrapeL.grapel.Mnetwork.eventhandler.ContainerEventHandler;
import mNetwork.Database;
import mNetwork.MEMIK;
import mNetwork.Server;
import mNetwork.VServer;

public class MNetworkGeneralTest extends MNetworkAbstractTest {
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
	public void modelIncludes5Entities() {
		MEMIK model = super.getModel();
		
		assertEquals(5, model.getEntities().getEntity().size());
	}
	@Test
	public void modelIncludes4Servers() {
		MEMIK model = super.getModel();
		
		assertEquals(4, model.getNetwork().getServer().size());
		
		int vServerCount = 0;
		int databaseCount = 0;
		int entryCount = 0;
		for (Server s: model.getNetwork().getServer()) {
			vServerCount += s.getVserver().size();
			for (VServer v: s.getVserver()) {
				databaseCount += v.getDatabase().size();
				for (Database d : v.getDatabase()) {
					entryCount += d.getEntry().size();
				}
			}
		}
		assertEquals(5, vServerCount);
		assertEquals(6, databaseCount);
		assertEquals(11, entryCount);
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
