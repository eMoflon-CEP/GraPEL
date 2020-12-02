package org.emoflon.grapel.testsuite.mnetwork;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import MNetworkGrapeL.grapel.Mnetwork.eventhandler.ReseatNonEUServerToEUEventHandler;
import MNetworkGrapeL.grapel.Mnetwork.eventhandler.ServerMigrationWarningEventHandler;
import mNetwork.GeoTag;
import mNetwork.Server;

public class MNetworkRuleTest extends MNetworkAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
	}
	@Test
	public void reseatOnMigrationWarning() {
		ServerMigrationWarningEventHandler migrationHandler = api.getServerMigrationWarningEventHandler();
		ReseatNonEUServerToEUEventHandler reseatHandler = api.getReseatNonEUServerToEUEventHandler();
		Server euServer = findServer(getModel().getNetwork(), 0);

		api.update();
		assertEquals(0,migrationHandler.getAllEvents().size());
		assertEquals(1,reseatHandler.getAllEvents().size());	
		
		euServer.setTag(GeoTag.NON_EU);
		api.update();
		assertEquals(3,migrationHandler.getAllEvents().size());		
		assertEquals(2,reseatHandler.getAllEvents().size());
		
		api.setRuleAutoApply(true);
		api.update();
		
		assertEquals(GeoTag.EU, euServer.getTag());
		
		
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
