package org.emoflon.cep.generator

/**
 * Template for GrapeL API generation
 */
class APITemplate extends AbstractTemplate{
	
	/**
	 * Constructor for an API Template
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 */
	new(ImportManager imports, NSManager names, PathManager paths) {
		super(imports, names, paths)
	}
	
	override String generate() {
		return '''package «imports.packageFQN»;

import java.util.Arrays;
import java.util.List;

import org.emoflon.cep.engine.ApamaCorrelator;
import org.emoflon.cep.engine.EventHandler;
import org.emoflon.cep.engine.GrapeEngineAPI;

import com.apama.EngineException;
import com.apama.engine.beans.EngineClientFactory;
import com.apama.engine.beans.interfaces.EngineClientInterface;

import «imports.EMoflonAppFQN»;

«FOR fqns : imports.getEventHandlerFQNs()»
import «fqns»;
«ENDFOR»

public abstract class «names.APIName» extends GrapeEngineAPI{
	
	final public static String CORRELATOR_LOCATION = "«paths.correlatorLocation»";
	final public static int CORRELATOR_PORT = 15903;
	final public static String CORRELATOR_HOSTNAME = "localhost";
	
	public «names.APIName»(final «names.EMoflonAppName» app) {
		super(app);
	}
	
	@Override
	public void registerMetamodels() {
		((«names.EMoflonAppName»)eMoflonApp).registerMetaModels();
	}

	@Override
	protected ApamaCorrelator configureApamaCorrelator() throws Exception {
		ApamaCorrelator correlator = new ApamaCorrelator(CORRELATOR_LOCATION);
		correlator.setDebug(true);
		return correlator;
	}

	@Override
	protected EngineClientInterface configureEngineClient() {
		EngineClientInterface engineClient = null;
		try {
			engineClient = EngineClientFactory.createEngineClient(CORRELATOR_HOSTNAME, CORRELATOR_PORT, «names.APIName».class.getSimpleName());
		} catch (EngineException e) {
			e.printStackTrace();
		}
		return engineClient;
	}
	
	@Override
	protected List<String> getMonitorScriptFiles() {
		return Arrays.asList(
			«FOR monitor : paths.patternMonitorLocations SEPARATOR ', '»
			"«monitor»"
			«ENDFOR»
		);
	}
	
	@Override
	protected List<EventHandler<?>> createEventHandler() {
		return Arrays.asList(
			«FOR handler : names.eventHandlerNames SEPARATOR ', '»
			new «handler»(grapeEngine)
			«ENDFOR»
		);
	}
	
	«FOR handler : names.eventHandlerNames»
	public «handler» get«handler»() {
		return («handler») getEventHandler(«handler».HANDLER_NAME);
	}
	«ENDFOR»
}'''
	}
	
	override String getPath() {
		return paths.APILocation
	}
	
}