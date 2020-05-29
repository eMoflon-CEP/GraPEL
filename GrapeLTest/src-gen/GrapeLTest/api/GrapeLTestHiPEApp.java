package GrapeLTest.api;

import org.emoflon.ibex.gt.hipe.runtime.HiPEGTEngine;

/**
 * An application using the GrapeLTestAPI with HiPE.
 */
public class GrapeLTestHiPEApp extends GrapeLTestApp {

	/**
	 * Creates the application with HiPE.
	 */
	public GrapeLTestHiPEApp() {
		super(new HiPEGTEngine());
	}

	/**
	 * Creates the application with HiPE.
	 * 
	 * @param workspacePath
	 *            the workspace path
	 */
	public GrapeLTestHiPEApp(final String workspacePath) {
		super(new HiPEGTEngine(), workspacePath);
	}
}
