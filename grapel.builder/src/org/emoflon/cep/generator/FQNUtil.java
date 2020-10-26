package org.emoflon.cep.generator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;

import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.plugin.PluginElement;

@SuppressWarnings("restriction")
public class FQNUtil {
	
	private static Map<String, String> packName2fq = new ConcurrentHashMap<>();
	private static Map<String, String> className2fq = new ConcurrentHashMap<>();
	private static Map<String, IProject> pack2project = new ConcurrentHashMap<>();
	private static Map<String, IPluginExtension> pack2extension = new ConcurrentHashMap<>();
	private static Map<EPackage, String> pack2fq = new ConcurrentHashMap<>();
	private static Map<EPackage, String> class2fq = new ConcurrentHashMap<>();

	public static synchronized String getFQClassName(EPackage epack) {
		if(getFQPackageName(epack) != null) {
			if(className2fq.containsKey(epack.getNsURI()))
				return className2fq.get(epack.getNsURI());
			return packName2fq.get(epack.getNsURI());
		}else {
			//if there is no generated model code in the workspace (e.g. plugins) fall back to the old method
			return getFQClassName(epack.getName(), epack);
		}
	}
	
	public static synchronized String getFQPackageName(EPackage epack) {
		// if fully qualified package has already been calculated return it
		if(packName2fq.containsKey(epack.getNsURI())) {
			return packName2fq.get(epack.getNsURI());
		}
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		getProjectInWorkspace(epack, workspace);
		
		IPluginExtension extensionPoint = pack2extension.get(epack.getNsURI());
		if(extensionPoint == null)
			return null;
		
		IPluginObject[] children = extensionPoint.getChildren();
		
		for(IPluginObject child : children) {
			if(child instanceof PluginElement) {
				PluginElement element = (PluginElement)child;
				IPluginAttribute attribute = (IPluginAttribute)element.getAttribute("class");
				if(attribute == null)
					continue;
				
				String packageName = attribute.getValue().substring(0, attribute.getValue().lastIndexOf("."));
				packName2fq.put(epack.getNsURI(), packageName);
				return packageName;
			}
		}
		
		return null;
	}
	
	public static synchronized IProject getProjectInWorkspace(EPackage epack, IWorkspace workspace) {
		if(pack2project.containsKey(epack.getNsURI()))
			return pack2project.get(epack.getNsURI());
			
		for(IProject project : workspace.getRoot().getProjects()) {
			IPluginModelBase pluginmodel = PluginRegistry.findModel(project);
			if(pluginmodel == null)
				continue;
			
			IPluginBase pluginBase = pluginmodel.getPluginBase();
			IPluginExtension[] ipe = pluginBase.getExtensions();

			for(IPluginExtension extensionPoint : ipe) {
				if("org.eclipse.emf.ecore.generated_package".equals(extensionPoint.getPoint())) {
					IPluginObject[] children = extensionPoint.getChildren();
					for(IPluginObject child : children) {
						if(child instanceof PluginElement) {
							PluginElement element = (PluginElement)child;
							IPluginAttribute attribute = (IPluginAttribute)element.getAttribute("uri");
							if(attribute == null)
								continue;
							
							if(!epack.getNsURI().equals(attribute.getValue()))
									continue;
							
							pack2project.put(epack.getNsURI(), project);
							pack2extension.put(epack.getNsURI(), extensionPoint);
							return project;
							
						}
					}
				}

			}
			
		}
		return null;
	}
	
	public static String getFQClassName(String modelName, EPackage epack) {
		getFQPackageName(modelName, epack);
		if(class2fq.containsKey(epack))
			return class2fq.get(epack);
		return pack2fq.get(epack);
	}
	
	public static String getFQPackageName(String modelName, EPackage epack) {
		// if fully qualified package has already been calculated return it
		if(pack2fq.containsKey(epack))
			return pack2fq.get(epack);
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = getProjectInWorkspace(modelName, workspace);
		IJavaProject javaProject = JavaCore.create(project);

		// if possible only search in the metamodel project (if it is in the workspace)
		// else search for it in all workspace projects in case that it is contained in an import
		List<IJavaProject> jProjects = new LinkedList<>();
		if(javaProject == null) {
			IProject[] projects = workspace.getRoot().getProjects();
			for(int i=0; i < projects.length; i++) {
				jProjects.add(JavaCore.create(projects[i]));
			}
		}
		else {
			jProjects.add(javaProject);
		}
		
		// calculate qualified name as far as we know it
		String qualifiedPackageName = epack.getName();
		EPackage parentPackage = epack.getESuperPackage();
		while(parentPackage != null) {
			qualifiedPackageName = parentPackage.getName() + "." + qualifiedPackageName;
			parentPackage = parentPackage.getESuperPackage();
		}
		
		// search for a project that contains the qualified name in its package structure and
		// has the PackageImpl class 
		try {
			for(IJavaProject jProject : jProjects)
			for(IJavaElement iSrcFolder : jProject.getChildren()) {
				JavaElement srcFolder = (JavaElement) iSrcFolder;
				for(IJavaElement iPack : srcFolder.getChildren()) {
					JavaElement pack = (JavaElement) iPack;
					String packageName = pack.getElementName();
					
					if(!packageName.contains(qualifiedPackageName))
						continue;
					
					IType t = jProject.findType(packageName + "." + StringUtil.firstToUpper(epack.getName()) + "Package");
					if(t != null) {
						// check if all eclasses can be found in this package
						boolean allEClassesFound = getClass(epack, jProject, packageName);
						
						if(allEClassesFound) {
							pack2fq.put(epack, packageName);
							return packageName;
						}
					}
				}
			}
		} catch (JavaModelException e) {
			System.out.println(e);
		}
		
		pack2fq.put(epack, epack.getName());
		return epack.getName();
	}

	private static boolean getClass(EPackage epack, IJavaProject jProject, String packageName)
			throws JavaModelException {
		
		String packageNameCopy = packageName;
		int found = 0;
		
		for(EClassifier ec : epack.getEClassifiers()) {
			if(ec instanceof EClass) {
				IType eType = jProject.findType(packageNameCopy + "." + ec.getName());
				if(eType == null) {
					if(found == 0)
						while(packageNameCopy.contains(".")) {
							packageNameCopy = packageName.substring(0, packageNameCopy.lastIndexOf("."));
							eType = jProject.findType(packageNameCopy + "." + ec.getName());
							if(eType != null) {
								found++;
								break;
							}
						}
					else {
						eType = jProject.findType(packageNameCopy + "." + ec.getName());
						if(eType != null) {
							found++;
						}
					}
				}
				else
					found++;
			}
		}
		if(found >= epack.getEClassifiers().stream().filter(c -> c instanceof EClass).count() / 2) {
			class2fq.put(epack, packageNameCopy);
			return true;
		}
		return false;
	}
	
	public static IProject getProjectInWorkspace(String modelName, IWorkspace workspace) {
		IProject[] projects = workspace.getRoot().getProjects();
		for(IProject project : projects) {
			if(project.getName().toLowerCase().equals(modelName.toLowerCase())) {
				return project;
			}
		}
		return null;
	}
	
}
