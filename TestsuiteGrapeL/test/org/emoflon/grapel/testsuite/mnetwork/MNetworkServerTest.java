package org.emoflon.grapel.testsuite.mnetwork;

import org.junit.After;
import org.junit.Before;

public class MNetworkServerTest extends MNetworkAbstractTest {
	@Before
	public void start() {
		api = this.init("test1.xmi");
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
