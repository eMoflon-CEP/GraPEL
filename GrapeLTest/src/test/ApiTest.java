package test;

import org.eclipse.emf.common.util.URI;

import GrapeLTest.grapel.Test.TestGrapeLAPI;
import GrapeLTest.grapel.Test.TestGrapeLHiPEEngine;
import GrapeLTest.grapel.Test.eventhandler.E1EventHandler;

public class ApiTest {
	
	public static void main(String[] args) {
		TestGrapeLAPI api = new TestGrapeLHiPEEngine();
		api.registerMetamodels();
		api.initEMoflonAPI(URI.createFileURI("C:\\Users\\sebas\\git\\GraPEL\\GrapeLTest\\instances\\test1.xmi"));
		
		try {
			api.initGrapeEngine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		E1EventHandler e1 = api.getE1EventHandler();
		
		api.update(); 
		
		e1.getAllEvents().forEach(event -> System.out.println(event));
		
		api.dispose();	
	}
}
