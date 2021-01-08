package org.emoflon.grapel.benchmark.flights;

import org.eclipse.emf.common.util.URI;
import org.emoflon.cep.engine.GrapeEngineAPI;
import org.emoflon.grapel.benchmark.PatternMatchingEngine;

public abstract class FlightMonitor<API extends GrapeEngineAPI> {
	protected API api;
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
	 * Returns the name of the benchmark which is used as a name of the subdirectory
	 * within the folders debug, instances and resources.
	 * 
	 * @return the benchmark name
	 */
	protected abstract String getBenchmarkName();
	protected final static long shutdownTime = 1000;
	
	protected void initAPI(final API api, final String modelInstanceFileName) {
		URI instanceURI = URI.createFileURI(instancesPath + this.getBenchmarkName() + "/" + modelInstanceFileName);
		
		api.registerMetamodels();
		api.initEMoflonAPI(instanceURI);
		
		try {
			api.initGrapeEngine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public abstract API init(final String modelInstanceFileName, PatternMatchingEngine engine);
	public void shutdown() {
		api.dispose();
		try {
			Thread.sleep(shutdownTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	};
}
