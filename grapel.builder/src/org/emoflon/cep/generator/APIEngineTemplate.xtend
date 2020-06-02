package org.emoflon.cep.generator

class APIEngineTemplate extends AbstractTemplate{
	
	String engineName;
	
	new(ImportManager imports, NSManager names, PathManager paths, String engineName) {
		super(imports, names, paths)
		this.engineName = engineName;
	}
	
	override generate() {
		return '''package «imports.packageFQN»;
		
import «imports.getEMoflonEngineAppFQN(engineName)»;
		
public class «names.getEngineName(engineName)» extends «names.EMoflonAppName» {
		
	public «names.getEngineName(engineName)»() {
		super(new «names.getEMoflonEngineAppName(engineName)»());
	}
		
}
'''
	}
	
}