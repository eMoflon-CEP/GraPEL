package org.emoflon.grapel.testsuite.mnetwork;

import org.emoflon.grapel.testsuite.GrapeLAppTestCase;

import MNetworkGrapeL.grapel.Mnetwork.MnetworkGrapeLAPI;
import MNetworkGrapeL.grapel.Mnetwork.MnetworkGrapeLHiPEEngine;

public abstract class MNetworkAbstractTest extends GrapeLAppTestCase<MnetworkGrapeLAPI> {
	MnetworkGrapeLAPI api;
	@Override
	protected String getTestName() {
		return "MNetwork";
	}
	@Override
	protected MnetworkGrapeLAPI init(String modelInstanceFileName) {
		MnetworkGrapeLAPI api = new MnetworkGrapeLHiPEEngine();
		initAPI(api,modelInstanceFileName);
		return api;
	}
}
