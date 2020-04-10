package org.emoflon.cep.builder;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.cep.generator.GrapelBuilderExtension;
import org.emoflon.cep.grapel.EditorGTFile;

import GrapeLModel.GrapeLModelContainer;

public class GrapelBuilder implements GrapelBuilderExtension {

	@Override
	public void run(IProject project, Resource resource) {
		System.out.println("Running extension: " + this.getClass().getSimpleName());
		System.out.println("Given project: " + project.getName());
		System.out.println("Given resource: " + resource.getURI());
		
		EditorGTFile grapelFile = (EditorGTFile) resource.getContents().get(0);
		GrapelToGrapelModelTransformer transformer =  new GrapelToGrapelModelTransformer();
		
		GrapeLModelContainer container = transformer.transform(grapelFile);
		
		//save for debugging
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource output = rs.createResource(URI.createURI(resource.getURI().trimFileExtension()+"_model.xmi"));
		output.getContents().add(container);
		Map<Object, Object> saveOptions = ((XMIResource)output).getDefaultSaveOptions();
		saveOptions.put(XMIResource.OPTION_ENCODING,"UTF-8");
		saveOptions.put(XMIResource.OPTION_USE_XMI_TYPE, Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_SAVE_TYPE_INFORMATION,Boolean.TRUE);
		saveOptions.put(XMIResource.OPTION_SCHEMA_LOCATION_IMPLEMENTATION, Boolean.TRUE);
		try {
			((XMIResource)output).save(saveOptions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("GrapeLModel model saved to: "+output.getURI().path());
		
	}

}
