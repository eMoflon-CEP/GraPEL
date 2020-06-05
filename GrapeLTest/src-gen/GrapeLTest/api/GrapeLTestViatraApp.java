package GrapeLTest.api;

import org.emoflon.ibex.gt.viatra.runtime.ViatraGTEngine;

/**
 * An application using the GrapeLTestAPI with Viatra.
 */
public class GrapeLTestViatraApp extends GrapeLTestApp {

	/**
	 * Creates the application with Viatra.
	 */
	public GrapeLTestViatraApp() {
		super(new ViatraGTEngine());
	}

	/**
	 * Creates the application with Viatra.
	 * 
	 * @param workspacePath
	 *            the workspace path
	 */
	public GrapeLTestViatraApp(final String workspacePath) {
		super(new ViatraGTEngine(), workspacePath);
	}
}
