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

public class GrapelAPIGenerator {
	
	private IProject project;
	private GrapeLModelContainer container;
	private Collection<String> pmEngines;
	
	private ImportManager imports;
	private NSManager names;
	private PathManager paths;
	private ModelManager model;
	
	private List<AbstractTemplate> templates = new LinkedList<>();
	private Map<String, String> files = Collections.synchronizedMap(new HashMap<>());

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
	
	private void initTemplates() {
		templates.add(new APITemplate(imports, names, paths));
		pmEngines.forEach(engine -> {
			templates.add(new APIEngineTemplate(imports, names, paths, engine));
		});
		
		container.getEvents().forEach(event -> {
			if(event instanceof MatchEvent) {
				templates.add(new MatchEventTemplate(imports, names, paths, model, event.getName()));
				templates.add(new MatchEventHandlerTemplate(imports, names, paths, model, event.getName()));
				templates.add(new MatchEventMonitorTemplate(event.getName(), imports, names, paths, model));
			}else {
				templates.add(new EventTemplate(imports, names, paths, model, event.getName()));
				templates.add(new EventHandlerTemplate(imports, names, paths, model, event.getName()));
				templates.add(new EventMonitorTemplate(event.getName(), imports, names, paths, model));
			}
		});
		
		container.getEventPatterns().forEach(eventPattern -> {
			templates.add(new EventPatternTemplate(eventPattern.getName(), imports, names, paths, model));
		});
	}
	
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
