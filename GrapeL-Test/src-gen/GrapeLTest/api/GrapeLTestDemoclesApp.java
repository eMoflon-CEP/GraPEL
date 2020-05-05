package GrapeLTest.api;

import org.emoflon.ibex.gt.democles.runtime.DemoclesGTEngine;

/**
 * An application using the GrapeLTestAPI with Democles.
 */
public class GrapeLTestDemoclesApp extends GrapeLTestApp {

	/**
	 * Creates the application with Democles.
	 */
	public GrapeLTestDemoclesApp() {
		super(new DemoclesGTEngine());
	}

	/**
	 * Creates the application with Democles.
	 * 
	 * @param workspacePath
	 *            the workspace path
	 */
	public GrapeLTestDemoclesApp(final String workspacePath) {
		super(new DemoclesGTEngine(), workspacePath);
	}
}
