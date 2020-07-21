package org.emoflon.grapel.testsuite;

import org.eclipse.emf.common.util.URI;
import org.emoflon.cep.engine.GrapeEngineAPI;

public abstract class GrapeLAppTestCase<API extends GrapeEngineAPI> {
	/**
	 * Relative path to the directory with the projects with the graph
	 * transformation rules.
	 */
	protected static String workspacePath = "../";
	/**
	 * Relative path to the instances directory. Files from this directory are
	 * changed during the transformations.
	 */
	protected static String instancesPath = "./instances/";
	/**
	 * Relative path to the resources directory. Files from this directory are just
	 * loaded, but never changed during transformation.
	 */
	protected static String resourcePath = "./resources/";
	/**
	 * Returns the name of the test which is used as a name of the subdirectory
	 * within the folders debug, instances and resources.
	 * 
	 * @return the test name
	 */
	protected abstract String getTestName();
	
	protected void initAPI(final API api, final String modelInstanceFileName) {
		URI instanceURI = URI.createFileURI(instancesPath + this.getTestName() + "/" + modelInstanceFileName);
		
		api.registerMetamodels();
		api.initEMoflonAPI(instanceURI);
		
		try {
			api.initGrapeEngine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	protected abstract API init(final String modelInstanceFileName);
	
}
