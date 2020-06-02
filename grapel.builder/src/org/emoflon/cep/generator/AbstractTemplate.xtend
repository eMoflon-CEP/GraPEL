package org.emoflon.cep.generator

abstract class AbstractTemplate {
	protected ImportManager imports;
	protected NSManager names;
	protected PathManager paths;
	
	new(ImportManager imports, NSManager names, PathManager paths) {
		this.imports = imports;
		this.names = names;
		this.paths = paths;
	}
	
	abstract def String generate();
}