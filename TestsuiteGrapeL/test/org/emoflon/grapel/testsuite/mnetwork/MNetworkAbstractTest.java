package org.emoflon.grapel.testsuite.mnetwork;

import java.util.LinkedList;

import org.emoflon.grapel.testsuite.GrapeLAppTestCase;

import MNetworkGrapeL.grapel.Mnetwork.MnetworkGrapeLAPI;
import MNetworkGrapeL.grapel.Mnetwork.MnetworkGrapeLHiPEEngine;
import MNetworkGrapeL.grapel.Mnetwork.eventhandler.ContainerEventHandler;
import mNetwork.MEMIK;
import mNetwork.Network;
import mNetwork.Server;

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
	protected MEMIK getModel() {
		ContainerEventHandler containerHandler = api.getContainerEventHandler();
		LinkedList<MEMIK> models = new LinkedList<MEMIK>();

		api.update();
		containerHandler.getAllEvents().forEach(event -> models.add(event.getMemik()));
		MEMIK model = models.get(0);
		
		return model;
	}
	protected Server findServer(Network network, int ID) {
		for(Server s: network.getServer())
			if(s.getId()==ID)
				return s;
		System.out.println("No matching server");
		return null;
	}
}
