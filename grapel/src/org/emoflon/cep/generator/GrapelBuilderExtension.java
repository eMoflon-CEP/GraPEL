package org.emoflon.cep.generator;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.resource.Resource;

public interface GrapelBuilderExtension {
	static final String BUILDER_EXTENSON_ID = "grapel.GrapelBuilderExtension";
	
	/**
	 * Builds the project.
	 * 
	 * @param project
	 *            the project to build
	 */
	public void run(IProject project, Resource resource);

}
