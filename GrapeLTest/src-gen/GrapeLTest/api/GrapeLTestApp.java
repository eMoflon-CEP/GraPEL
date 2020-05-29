package GrapeLTest.api;

import Flights.FlightsPackage;
import org.emoflon.ibex.common.operational.IContextPatternInterpreter;
import org.emoflon.ibex.gt.api.GraphTransformationApp;

/**
 * An application using the GrapeLTestAPI.
 */
public class GrapeLTestApp extends GraphTransformationApp<GrapeLTestAPI> {

	/**
	 * Creates the application with the given engine.
	 * 
	 * @param engine
	 *            the pattern matching engine
	 */
	public GrapeLTestApp(final IContextPatternInterpreter engine) {
		super(engine);
	}

	/**
	 * Creates the application with the given engine.
	 * 
	 * @param engine
	 *            the pattern matching engine
	 * @param workspacePath
	 *            the workspace path
	 */
	public GrapeLTestApp(final IContextPatternInterpreter engine, final String workspacePath) {
		super(engine, workspacePath);
	}

	@Override
	public void registerMetaModels() {
		registerMetaModel(FlightsPackage.eINSTANCE);
	}

	@Override
	public GrapeLTestAPI initAPI() {
		if (defaultResource.isPresent()) {
			return new GrapeLTestAPI(engine, resourceSet, defaultResource.get(), workspacePath);
		}
		return new GrapeLTestAPI(engine, resourceSet, workspacePath);
	}
}
