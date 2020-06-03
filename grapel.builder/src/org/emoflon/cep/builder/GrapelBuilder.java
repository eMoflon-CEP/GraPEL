package org.emoflon.cep.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.cep.generator.GrapelBuilderExtension;
import org.emoflon.cep.grapel.EditorGTFile;
import org.emoflon.ibex.gt.codegen.EClassifiersManager;
import org.emoflon.ibex.gt.codegen.GTEngineExtension;
import org.emoflon.ibex.gt.codegen.JavaFileGenerator;
import org.emoflon.ibex.gt.editor.ui.builder.GTBuilderExtension;
import org.emoflon.ibex.gt.transformations.EditorToGTModelTransformation;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXPatternSet;
import org.gervarro.eclipse.workspace.util.AntPatternCondition;
import org.moflon.core.build.CleanVisitor;
import org.moflon.core.plugins.manifest.ManifestFileUpdater;
import org.moflon.core.utilities.ExtensionsUtil;
import org.moflon.core.utilities.WorkspaceHelper;

import GTLanguage.GTNode;
import GTLanguage.GTRule;
import GTLanguage.GTRuleSet;
import GrapeLModel.GrapeLModelContainer;

public class GrapelBuilder implements GrapelBuilderExtension {

	@Override
	public void run(IProject project, Resource resource) {
		System.out.println("Running extension: " + this.getClass().getSimpleName());
		System.out.println("Given project: " + project.getName());
		System.out.println("Given resource: " + resource.getURI());
		
		// clean old code and create folders
		try {
			removeGeneratedCode(project, "src-gen/**");
			createFolders(project);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		// Create intermediate grapel model and ibex patterns
		EditorGTFile grapelFile = (EditorGTFile) resource.getContents().get(0);
		GrapelToGrapelModelTransformer transformer =  new GrapelToGrapelModelTransformer();
		GrapeLModelContainer container = transformer.transform(grapelFile);
		container.setName(resource.getURI().trimFileExtension().lastSegment());
		container.setCorrelatorLocation("C:\\SoftwareAG\\Apama\\bin\\correlator.exe");
			
		// Create GT rule model
		EditorToGTModelTransformation trafo = new EditorToGTModelTransformation();
		GTRuleSet gtRules = trafo.transform(grapelFile);
		
		if(gtRules!= null) {
			try {
				buildEMoflonAPI(project, gtRules);
				updateManifest(project, this::processManifestForPackage);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		
		// Save ibex-patterns and gt-rules for the hipe engine builder
		IFolder apiPackage = project.getFolder("src-gen/"+project.getName().replace(".", "/")+"/api");
		saveResource(container, resource.getURI().trimFileExtension()+"_model.xmi");
		
		IBeXPatternSet ibexPatterns = container.getIbexPatterns();
		saveResource(ibexPatterns, apiPackage.getFullPath()+"/ibex-patterns.xmi");
		saveResource(gtRules, apiPackage.getFullPath()+"/gt-rules.xmi");
		
		//cleanup
		if(container.eResource() != null)
			container.eResource().unload();
		if(ibexPatterns.eResource() != null)
			ibexPatterns.eResource().unload();
		if(gtRules.eResource() != null)
			gtRules.eResource().unload();
		
		// build HiPE engine code
		if(ibexPatterns != null && !ibexPatterns.getContextPatterns().isEmpty()) {
			IFolder packagePath = project.getFolder(project.getName().replace(".", "/"));
			collectEngineBuilderExtensions().forEach(ext->ext.run(project, packagePath.getProjectRelativePath()));
		}
		
		//TODO: Build Grapel API :)

		
		
		
		try {
			project.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
	}

	
	public static void removeGeneratedCode(final IProject project, final String pattern) throws CoreException {
		final CleanVisitor cleanVisitor = new CleanVisitor(project, //
				new AntPatternCondition(new String[] { pattern }) //
				);
		project.accept(cleanVisitor, IResource.DEPTH_INFINITE, IResource.NONE);
	}
	
	public void createFolders(IProject project) throws CoreException {
		WorkspaceHelper.createFolderIfNotExists(WorkspaceHelper.getSourceFolder(project), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(WorkspaceHelper.getBinFolder(project), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder("src-gen"), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder("src-gen/"+project.getName().replace(".", "/")+"/api"), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder("src-gen/"+project.getName().replace(".", "/")+"/api/matches"), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder("src-gen/"+project.getName().replace(".", "/")+"/api/rules"), new NullProgressMonitor());
	}
	
	private void updateManifest(final IProject project, final BiFunction<IProject, Manifest, Boolean> updateFunction) throws CoreException {
		new ManifestFileUpdater().processManifest(project, manifest -> updateFunction.apply(project, manifest));
	}
	
	private boolean processManifestForPackage(IProject project, Manifest manifest) {
		List<String> dependencies = new ArrayList<String>();
		dependencies.addAll(Arrays.asList("org.emoflon.ibex.common", "org.emoflon.ibex.gt", "grapelmodel"));
		collectEngineExtensions().forEach(engine -> dependencies.addAll(engine.getDependencies()));
		boolean changedBasics = ManifestFileUpdater.setBasicProperties(manifest, project.getName());
		boolean updatedDependencies = ManifestFileUpdater.updateDependencies(manifest, dependencies);
		return changedBasics || updatedDependencies;
	}
	
	public void buildEMoflonAPI(IProject project, GTRuleSet gtRules) throws CoreException {
		final Registry packageRegistry = new EPackageRegistryImpl();
		findAllEPackages(gtRules, packageRegistry);
		
		IFolder apiPackage = project.getFolder("src-gen/"+project.getName().replace(".", "/")+"/api");
		ensureFolderExists(apiPackage);
		generateAPI(project, apiPackage, gtRules, packageRegistry);
		
		//cleanup
		if(gtRules.eResource() != null)
			gtRules.eResource().unload();
	}
	
	public static Collection<GTBuilderExtension> collectEngineBuilderExtensions() {
		return ExtensionsUtil.collectExtensions(GTBuilderExtension.BUILDER_EXTENSON_ID, "class", GTBuilderExtension.class)
				.stream()
				.filter(ext -> ext.getClass().getCanonicalName().contains("HiPE"))
				.collect(Collectors.toList());
	}
	
	public static void findAllEPackages(final GTRuleSet gtRules, final Registry packageRegistry) {
		for(GTRule rule : gtRules.getRules()) {
			for(GTNode node : rule.getNodes()) {
				EPackage foreign = node.getType().getEPackage();
				if(!packageRegistry.containsKey(foreign.getNsURI())) {
					packageRegistry.put(foreign.getNsURI(), foreign);
				}
			}
			for(GTNode node : rule.getRuleNodes()) {
				EPackage foreign = node.getType().getEPackage();
				if(!packageRegistry.containsKey(foreign.getNsURI())) {
					packageRegistry.put(foreign.getNsURI(), foreign);
				}
			}
		}
	}
	
	public static void generateAPI(final IProject project, final IFolder apiPackage, final GTRuleSet gtRuleSet,
			final Registry packageRegistry) throws CoreException {
		JavaFileGenerator generator = new JavaFileGenerator(getClassNamePrefix(project), project.getName(), createEClassifierManager(packageRegistry));
		IFolder matchesPackage = ensureFolderExists(apiPackage.getFolder("matches"));
		IFolder rulesPackage = ensureFolderExists(apiPackage.getFolder("rules"));
		gtRuleSet.getRules().forEach(gtRule -> {
			generator.generateMatchClass(matchesPackage, gtRule);
			generator.generateRuleClass(rulesPackage, gtRule);
		});

		generator.generateAPIClass(apiPackage, gtRuleSet,
				String.format("%s/%s/%s/api/ibex-patterns.xmi", project.getName(), "src-gen", project.getName().replace(".", "/")));
		generator.generateAppClass(apiPackage);
		collectEngineExtensions().forEach(e -> generator.generateAppClassForEngine(apiPackage, e));
	}
	
	public static IFolder ensureFolderExists(final IFolder folder) throws CoreException{
		if (!folder.exists()) {
			folder.create(true, true, null);
		}
		return folder;
	}
	
	public static String getClassNamePrefix(final IProject project) {
		URI projectNameAsURI = URI.createFileURI(project.getName().replace(".", "/"));
		String prefix = projectNameAsURI.lastSegment();
		return Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1);
	}
	
	public static Collection<GTEngineExtension> collectEngineExtensions() {
		return ExtensionsUtil.collectExtensions(GTEngineExtension.BUILDER_EXTENSON_ID, "class",
				GTEngineExtension.class);
	}
	
	public static EClassifiersManager createEClassifierManager(final Registry packageRegistry) {
		EClassifiersManager eClassifiersManager = new EClassifiersManager(new HashMap<>());
		packageRegistry.values().stream().filter(x -> (x instanceof EPackage)).forEach(obj -> {
			EPackage epackage = (EPackage) obj;
			eClassifiersManager.loadMetaModelClasses(epackage.eResource());
		});
		return eClassifiersManager;
	}
	
	public static void saveResource(EObject object, String path) {
		//save for debugging
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource output = rs.createResource(URI.createURI(path));
		output.getContents().add(object);
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
