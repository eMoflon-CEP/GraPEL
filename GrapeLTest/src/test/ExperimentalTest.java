package test;

import org.eclipse.emf.common.util.URI;

import experimental.E1EventHandler;
import experimental.GrapeLTestGrapeEngineAPI;
import experimental.GrapeLTestGrapeEngineHiPE;

public class ExperimentalTest {
	
	public static void main(String[] args) {
		GrapeLTestGrapeEngineAPI api = new GrapeLTestGrapeEngineHiPE();
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
