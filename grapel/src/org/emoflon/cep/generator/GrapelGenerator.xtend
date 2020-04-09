/*
 * generated by Xtext 2.20.0
 */
package org.emoflon.cep.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.XMIResource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.common.util.URI
import org.emoflon.cep.grapel.EditorGTFile
import org.eclipse.emf.ecore.util.EcoreUtil
import java.util.function.Consumer
import org.eclipse.core.runtime.SafeRunner
import org.eclipse.core.runtime.ISafeRunnable
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.resources.IProject
import java.io.FileNotFoundException
import java.util.Collection
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IConfigurationElement
import org.eclipse.core.runtime.Platform
import java.util.ArrayDeque

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class GrapelGenerator extends AbstractGenerator {

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
//		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl())
//		val rs = new ResourceSetImpl;
//		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl())
//		val model = resource.contents.get(0) as EditorGTFile;
//		val output = rs.createResource(URI.createURI(resource.URI.trimFileExtension+".xmi"))
//		output.contents.add(model)
//		EcoreUtil.resolveAll(output)
//		println(resource.URI)
//
//		val saveOptions = (output as XMIResource).getDefaultSaveOptions()
//		saveOptions.put(XMIResource.OPTION_ENCODING,"UTF-8");
//		saveOptions.put(XMIResource.OPTION_USE_XMI_TYPE, Boolean.TRUE);
//		saveOptions.put(XMIResource.OPTION_SAVE_TYPE_INFORMATION,Boolean.TRUE);
//		saveOptions.put(XMIResource.OPTION_SCHEMA_LOCATION_IMPLEMENTATION, Boolean.TRUE);
//		(output as XMIResource).save(saveOptions)
//		System.out.println("Xtext model saved to: "+output.URI.path)
//		
//		println("Running Builder extensions...")
//		val workspace = getWorkspace()
//		val project = getProjectOfResource(workspace, output)
//		if(project === null)
//			throw new FileNotFoundException("Could not find xtext model file: "+ output.URI.path)
//			
//		runBuilderExtensions([ext | ext.run(project, output)])
		
	}
	
	def static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace()
	}
	
	def static IProject getProjectOfResource(IWorkspace workspace, Resource resource) {
		if(resource.URI.segmentCount<2)
				return null;
				
		for(project : workspace.root.projects) {
			val projectName = resource.URI.segment(1)
			if(project.name.equalsIgnoreCase(projectName)) {
				return project;
			}
		}
		
		return null;
	}
	
	def static void runBuilderExtensions(Consumer<GrapelBuilderExtension> action) {
		val ISafeRunnable runnable = new ISafeRunnable() {
			
			override handleException(Throwable e) {
				System.err.println(e.getMessage())
			}
			
			override run() throws Exception {
				collectExtensions(GrapelBuilderExtension.BUILDER_EXTENSON_ID, "class", typeof(GrapelBuilderExtension))
						.forEach(action);
			}

		};
		SafeRunner.run(runnable);
	}
	
	/**
	 * Collects all registered extensions with the given ID.
	 * 
	 * @param extensionID
	 *            the ID of the extension
	 * @param property
	 *            the name of the property
	 * @param extensionType
	 *            the extension type
	 * @return all extensions with the given ID as extensions of the given type
	 */
	def static <T> Collection<T> collectExtensions(String extensionID, String property, Class<T> extensionType) {
		val extensions = new ArrayDeque<T>();
		val config = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionID);
		try {
			for (IConfigurationElement e : config) {
				val o = e.createExecutableExtension(property);
				if (extensionType.isInstance(o)) {
					extensions.add(extensionType.cast(o));
				}
			}
		} catch (CoreException ex) {
			System.err.println(ex.message)
		}

		return extensions;
	}
}
