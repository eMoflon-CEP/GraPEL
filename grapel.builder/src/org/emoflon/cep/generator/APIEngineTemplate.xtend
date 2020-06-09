package org.emoflon.cep.generator

class APIEngineTemplate extends AbstractTemplate{
	
	String engineName;
	
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