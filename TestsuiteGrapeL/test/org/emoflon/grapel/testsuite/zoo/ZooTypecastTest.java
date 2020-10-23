package org.emoflon.grapel.testsuite.zoo;

import org.junit.After;
import org.junit.Before;

public class ZooTypecastTest extends ZooAbstractTest {
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
