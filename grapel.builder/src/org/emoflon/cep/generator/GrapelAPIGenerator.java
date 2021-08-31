package org.emoflon.cep.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import GrapeLModel.GrapeLModelContainer;
import GrapeLModel.MatchEvent;
import GrapeLModel.RuleEvent;

/**
 * Generator for generation of the GrapeL API
 */
public class GrapelAPIGenerator {
	
	// general project stuff
	private IProject project;
	private GrapeLModelContainer container;
	/**
	 * Collection of pattern matching engine identifiers
	 */
	private Collection<String> pmEngines;
	
	// Utility helpers
	private ImportManager imports;
	private NSManager names;
	private PathManager paths;
	private ModelManager model;
	
	private List<AbstractTemplate> templates = new LinkedList<>();
	private Map<String, String> files = Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * Constructor for the GrapeL API generator
	 * @param project that the generator should use for code generation 
	 * @param container that includes all GrapeL elements
	 * @param pmEngines the pattern matching engine identifiers for API generation
	 */
	public GrapelAPIGenerator(IProject project, GrapeLModelContainer container, Collection<String> pmEngines) {
		this.project = project;
		this.container = container;
		this.pmEngines = pmEngines;
		
		names = new NSManager(project.getName(), container);
		imports = new ImportManager(project.getName(), names, container);
		paths = new PathManager(project, names, container);
		model = new ModelManager(project.getName(), container);
		
		initTemplates();
	}
	
	/**
	 * Initializes the templates for code generation with the import manager, the name manager and the path manager
	 */
	private void initTemplates() {
		templates.add(new APITemplate(imports, names, paths));
		pmEngines.forEach(engine -> {
			templates.add(new APIEngineTemplate(imports, names, paths, engine));
		});
		
		container.getEvents().forEach(event -> {
			if(event instanceof RuleEvent) {
				templates.add(new MatchEventTemplate(imports, names, paths, model, event.getName()));
				templates.add(new RuleEventTemplate(imports, names, paths, model, event.getName()));
				templates.add(new RuleEventHandlerTemplate(imports, names, paths, model, event.getName()));
				templates.add(new MatchEventMonitorTemplate(event.getName(), imports, names, paths, model));
			}else if(event instanceof MatchEvent && !(event instanceof RuleEvent)) {
				templates.add(new MatchEventTemplate(imports, names, paths, model, event.getName()));
				templates.add(new MatchEventHandlerTemplate(imports, names, paths, model, event.getName()));
				templates.add(new MatchEventMonitorTemplate(event.getName(), imports, names, paths, model));
			} else {
				templates.add(new EventTemplate(imports, names, paths, model, event.getName()));
				templates.add(new EventHandlerTemplate(imports, names, paths, model, event.getName()));
				templates.add(new EventMonitorTemplate(event.getName(), imports, names, paths, model));
			}
		});
		
		container.getEventPatterns().forEach(eventPattern -> {
			templates.add(new EventPatternTemplate(eventPattern.getName(), imports, names, paths, model));
		});
	}
	
	/**
	 * Generates the GrapeL code
	 * @throws CoreException if the generation of the required folders fails
	 */
	public void generateCode() throws CoreException {
		paths.createRequiredFolders();
		
		templates.parallelStream().forEach(template -> {
			files.put(template.getPath(), template.generate());
		});
		
		files.put(paths.getEventPatternMonitorLocation(NSManager.MAINTAINANCE_MONITOR), EventPatternTemplate.getSyncPattern());
		
		files.entrySet().parallelStream().forEach(entry -> {
			try {
				Files.write(Paths.get(entry.getKey()), Arrays.asList(entry.getValue()));
//				System.out.println("Saved file: "+entry.getKey());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
