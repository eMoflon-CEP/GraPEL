package org.emoflon.cep.generator

/**
 * Template for GrapeL API engine generation
 */
class APIEngineTemplate extends AbstractTemplate{
	/**
	 * Name of the engine
	 */
	String engineName;
	
	/**
	 * Constructor for an API engine template
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 * @param engineName the name of the API engine
	 */
	new(ImportManager imports, NSManager names, PathManager paths, String engineName) {
		super(imports, names, paths)
		this.engineName = engineName;
	}
	
	override generate() {
		return '''package «imports.packageFQN»;
		
import «imports.APIFQN»;
import «imports.getEMoflonEngineAppFQN(engineName)»;
		
public class «names.getEngineName(engineName)» extends «names.APIName» {
		
	public «names.getEngineName(engineName)»() {
		super(new «names.getEMoflonEngineAppName(engineName)»());
	}
		
}
'''
	}
	
	override getPath() {
		return paths.getAPIEngineLocation(engineName)
	}
	
}