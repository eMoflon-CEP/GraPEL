package org.emoflon.cep.generator

/**
 * Abstract Template for GrapeL generation
 */
abstract class AbstractTemplate {
	// Utility managers
	protected ImportManager imports;
	protected NSManager names;
	protected PathManager paths;
	
	/**
	 * Constructor for an Abstract Template
	 * @param imports the manager that organizes the imports
	 * @param names the manager that includes the name space mapping for the project
	 * @param paths the manager that includes the utility for path generation
	 */
	new(ImportManager imports, NSManager names, PathManager paths) {
		this.imports = imports;
		this.names = names;
		this.paths = paths;
	}
	
	/**
	 * Generates the the contents for a given entity
	 * @returns the contents for a given entity as a string
	 */
	abstract def String generate();
	
	/**
	 * @returns the path to the given entity
	 */
	abstract def String getPath();
}