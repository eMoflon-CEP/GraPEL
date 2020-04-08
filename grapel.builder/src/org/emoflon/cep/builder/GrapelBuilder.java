package org.emoflon.cep.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.cep.generator.GrapelBuilderExtension;

public class GrapelBuilder implements GrapelBuilderExtension {

	@Override
	public void run(IProject project, Resource resource) {
		System.out.println("Running extension: " + this.getClass().getSimpleName());
		System.out.println("Given project: " + project.getName());
		System.out.println("Given resource: " + resource.getURI());
	}

}
