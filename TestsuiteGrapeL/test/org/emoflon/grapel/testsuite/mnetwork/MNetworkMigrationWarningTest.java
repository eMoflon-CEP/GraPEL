package org.emoflon.grapel.testsuite.mnetwork;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import MNetworkGrapeL.grapel.Mnetwork.eventhandler.ServerMigrationWarningEventHandler;
import mNetwork.GeoTag;
import mNetwork.Server;

public class MNetworkMigrationWarningTest extends MNetworkAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	@Test
	public void moveEUServerToNonEU1() {
		ServerMigrationWarningEventHandler migrationHandler = api.getServerMigrationWarningEventHandler();
		Server euServer1 = findServer(getModel().getNetwork(), 0);
		Server euServer2 = findServer(getModel().getNetwork(), 1);
		Server euServer3 = findServer(getModel().getNetwork(), 2);
		
		api.update();
		assertEquals(0,migrationHandler.getAllEvents().size());
		
		euServer1.setTag(GeoTag.NON_EU);
		api.update();
		assertEquals(3,migrationHandler.getAllEvents().size());
		
		euServer2.setTag(GeoTag.NON_EU);
		api.update();
		assertEquals(3,migrationHandler.getAllEvents().size());
		
		euServer3.setTag(GeoTag.NON_EU);
		api.update();
		assertEquals(5,migrationHandler.getAllEvents().size());
	}
	@Test
	public void moveEUServerToNonEU2() {
		ServerMigrationWarningEventHandler migrationHandler = api.getServerMigrationWarningEventHandler();
		Server euServer = findServer(getModel().getNetwork(), 0);
		
		api.update();
		assertEquals(0,migrationHandler.getAllEvents().size());
		
		euServer.setTag(GeoTag.NON_EU);
		api.update();
		assertEquals(3,migrationHandler.getAllEvents().size());
		
		euServer.setTag(GeoTag.EU);
		api.update();
		assertEquals(6,migrationHandler.getAllEvents().size());
		
		euServer.setTag(GeoTag.NON_EU);
		api.update();
		assertEquals(12,migrationHandler.getAllEvents().size());
	}
	@Test
	public void moveNonEUServerToEU() {
		ServerMigrationWarningEventHandler migrationHandler = api.getServerMigrationWarningEventHandler();
		Server nonEUServer = findServer(getModel().getNetwork(), 3);
		
		api.update();
		assertEquals(0,migrationHandler.getAllEvents().size());
		
		nonEUServer.setTag(GeoTag.EU);
		api.update();
		assertEquals(0,migrationHandler.getAllEvents().size());
		
		nonEUServer.setTag(GeoTag.NON_EU);
		api.update();
		assertEquals(1,migrationHandler.getAllEvents().size());
		
		nonEUServer.setTag(GeoTag.EU);
		api.update();
		assertEquals(2,migrationHandler.getAllEvents().size());
		
		nonEUServer.setTag(GeoTag.NON_EU);
		api.update();
		assertEquals(4,migrationHandler.getAllEvents().size());
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
