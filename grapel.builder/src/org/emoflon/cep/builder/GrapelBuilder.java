package org.emoflon.cep.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.emoflon.cep.generator.GrapelAPIGenerator;
import org.emoflon.cep.generator.GrapelBuilderExtension;
import org.emoflon.cep.grapel.EditorGTFile;
import org.emoflon.ibex.gt.codegen.EClassifiersManager;
import org.emoflon.ibex.gt.codegen.GTEngineBuilderExtension;
import org.emoflon.ibex.gt.codegen.GTEngineExtension;
import org.emoflon.ibex.gt.codegen.JavaFileGenerator;
import org.emoflon.ibex.gt.editor.ui.builder.GTBuilderExtension;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXModel;
import org.emoflon.ibex.patternmodel.IBeXPatternModel.IBeXPattern;
import org.gervarro.eclipse.workspace.util.AntPatternCondition;
import org.moflon.core.build.CleanVisitor;
import org.moflon.core.plugins.manifest.ManifestFileUpdater;
import org.moflon.core.utilities.ExtensionsUtil;
import org.moflon.core.utilities.WorkspaceHelper;

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
		container.setCorrelatorLocation("C:\\\\SoftwareAG\\\\Apama\\\\bin\\\\correlator.exe");
		IBeXModel ibexModel = container.getIbexModel();
		
		if(ibexModel!= null) {
			try {
				buildEMoflonAPI(project, ibexModel);
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
		saveResource(ibexModel, apiPackage.getFullPath()+"/ibex-patterns.xmi");
		
		// build HiPE engine code
		if(ibexModel != null && !ibexModel.getPatternSet().getContextPatterns().isEmpty()) {
			IFolder packagePath = project.getFolder(project.getName().replace(".", "/"));
			collectEngineBuilderExtensions().forEach(ext->ext.run(project, packagePath.getProjectRelativePath(), ibexModel));
		}
		
		//TODO: Build Grapel API :)
		GrapelAPIGenerator apiGenerator = new GrapelAPIGenerator(project, container, Arrays.asList("Democles", "HiPE", "Viatra"));
		try {
			apiGenerator.generateCode();
		} catch (CoreException e1) {
			e1.printStackTrace();
			//cleanup
			if(container.eResource() != null)
				container.eResource().unload();
			return;
		}
		
		//cleanup
		if(container.eResource() != null)
			container.eResource().unload();
		
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
	
	public void buildEMoflonAPI(IProject project, IBeXModel ibexModel) throws CoreException {
		final Registry packageRegistry = new EPackageRegistryImpl();
		findAllEPackages(ibexModel, packageRegistry);
		
		IFolder apiPackage = project.getFolder("src-gen/"+project.getName().replace(".", "/")+"/api");
		ensureFolderExists(apiPackage);
		generateAPI(project, apiPackage, ibexModel, packageRegistry);
	}
	
	public static Collection<GTEngineBuilderExtension> collectEngineBuilderExtensions() {
		return ExtensionsUtil.collectExtensions(GTEngineBuilderExtension.BUILDER_EXTENSON_ID, "class", GTEngineBuilderExtension.class);
	}
	
	public static void findAllEPackages(final IBeXModel ibexModel, final Registry packageRegistry) {
		ibexModel.getNodeSet().getNodes().forEach(node -> {
			EPackage foreign = node.getType().getEPackage();
			if(!packageRegistry.containsKey(foreign.getNsURI())) {
				packageRegistry.put(foreign.getNsURI(), foreign);
			}
		});
	}
	
	public static void generateAPI(final IProject project, final IFolder apiPackage, final IBeXModel ibexModel,
			final Registry packageRegistry) throws CoreException {
		JavaFileGenerator generator = new JavaFileGenerator(getClassNamePrefix(project), project.getName(), createEClassifierManager(packageRegistry));
		IFolder matchesPackage = ensureFolderExists(apiPackage.getFolder("matches"));
		IFolder rulesPackage = ensureFolderExists(apiPackage.getFolder("rules"));
		IFolder probabilitiesPackage = ensureFolderExists(apiPackage.getFolder("probabilities"));
		
		Set<IBeXPattern> ruleContextPatterns = new HashSet<>();
		ibexModel.getRuleSet().getRules().forEach(ibexRule -> {
			generator.generateMatchClass(matchesPackage, ibexRule);
			generator.generateRuleClass(rulesPackage, ibexRule);
			generator.generateProbabilityClass(probabilitiesPackage, ibexRule);
			ruleContextPatterns.add(ibexRule.getLhs());
		});
		
		ibexModel.getPatternSet().getContextPatterns().stream()
		.filter(pattern -> !ruleContextPatterns.contains(pattern))
		.filter(pattern -> !pattern.getName().contains("CONDITION"))
		.forEach(pattern -> {
			generator.generateMatchClass(matchesPackage, pattern);
			generator.generatePatternClass(rulesPackage, pattern);
		});

		generator.generateAPIClass(apiPackage, ibexModel,
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
