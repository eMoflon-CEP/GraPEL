package org.emoflon.cep.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.moflon.core.utilities.WorkspaceHelper;

import GrapeLModel.GrapeLModelContainer;
import GrapeLModel.RuleEvent;

public class PathManager {
	
	private IProject project;
	private String projectName;
	private GrapeLModelContainer container;
	
	private NSManager names;
	
	private String projectLocation;
	
	private String eMoflonAPIFolder;
	private String eMoflonPatternFolder;
	private String eMoflonMatchFolder;
	
	private String basePackageLocation;
	private String eventFolder;
	private String handlerFolder;
	private String eventMonitorFolder;
	private String patternMonitorFolder;
	
	private Map<String, String> patternMonitors = new HashMap<>();
	private Map<String, String> eventMonitors = new HashMap<>();
	private Map<String, String> events = new HashMap<>();
	private Map<String, String> ruleEvents = new HashMap<>();
	private Map<String, String> eventHandler = new HashMap<>();
	
	private void mapMonitors() {
		container.getEventPatterns().forEach(pattern -> {
			patternMonitors.put(pattern.getName(), patternMonitorFolder+"/"+StringUtil.firstToUpper(pattern.getName())+".mon");
		});
		patternMonitors.put(NSManager.MAINTAINANCE_MONITOR, patternMonitorFolder+"/Maintainance.mon");
		
		container.getEvents().forEach(event -> {
			eventMonitors.put(event.getName(), eventMonitorFolder+"/"+names.getEventName(event.getName())+".mon");
		});
	}
	
	private void mapEvents() {
		container.getEvents().forEach(event -> {
			events.put(event.getName(), eventFolder+"/"+names.getEventName(event.getName())+".java");
			eventHandler.put(event.getName(), handlerFolder+"/"+names.getEventHandlerName(event.getName())+".java");
			if(event instanceof RuleEvent) {
				ruleEvents.put(event.getName(), eventFolder+"/"+names.getEventName(event.getName())+"Application.java");
			}
		});
		
		events.put(NSManager.SYNCHRONIZATION_EVENT, eventMonitorFolder+"/RequestSynchronizationEvent.mon");
		events.put(NSManager.UPDATE_EVENT, eventMonitorFolder+"/UpdateEvent.mon");
	}
	
	public PathManager(IProject project, NSManager names, GrapeLModelContainer container) {
		this.project = project;
		this.projectName = project.getName();
		this.names = names;
		this.container = container;
		
		projectLocation = project.getLocation().toPortableString();
		eMoflonAPIFolder = projectLocation + "/src-gen/" + projectName.replace(".", "/") + "/api";
		eMoflonMatchFolder = eMoflonAPIFolder + "/matches";
		eMoflonPatternFolder = eMoflonAPIFolder + "/rules";
		
		basePackageLocation = projectLocation + "/src-gen/" + projectName.replace(".", "/") + "/grapel/" + StringUtil.firstToUpper(container.getName());
		eventFolder = basePackageLocation + "/events";
		handlerFolder = basePackageLocation + "/eventhandler";
		eventMonitorFolder = basePackageLocation + "/eventmonitors";
		patternMonitorFolder = basePackageLocation + "/patternmonitors";
		
		mapMonitors();
		mapEvents();
	}
	
	public void createRequiredFolders() throws CoreException {
		String relGrapelPkg = "src-gen/"+project.getName().replace(".", "/")+"/grapel";
		String relBasePkg = relGrapelPkg + "/" + StringUtil.firstToUpper(container.getName());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder(relGrapelPkg), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder(relBasePkg), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder(relBasePkg + "/events"), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder(relBasePkg + "/eventhandler"), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder(relBasePkg + "/eventmonitors"), new NullProgressMonitor());
		WorkspaceHelper.createFolderIfNotExists(project.getFolder(relBasePkg + "/patternmonitors"), new NullProgressMonitor());
	}
	
	public String getCorrelatorLocation( ) {
		return container.getCorrelatorLocation();
	}
	
	public Collection<String> getPatternMonitorLocations() {
		return patternMonitors.values();
	}
	
	public String getAPILocation() {
		return basePackageLocation+"/"+names.getAPIName()+".java";
	}
	
	public String getAPIEngineLocation(String engineName) {
		return basePackageLocation+"/"+names.getEngineName(engineName)+".java";
	}
	
	public String getEventLocation(String eventName) {
		return events.get(eventName);
	}
	
	public String getRuleEventLocation(String eventName) {
		return ruleEvents.get(eventName);
	}
	
	public String getEventHandlerLocation(String eventHandlerName) {
		return eventHandler.get(eventHandlerName);
	}
	
	public String getMatchEventHandlerLocation(String patternName) {
		return getEventHandlerLocation(patternName);
	}
	
	public String getEventMonitorLocation(String eventName) {
		return eventMonitors.get(eventName);
	}
	
	public String getMatchEventMonitorLocation(String patternName) {
		return getEventMonitorLocation(patternName);
	}
	
	public String getEventPatternMonitorLocation(String eventPatternName) {
		return patternMonitors.get(eventPatternName);
	}
}
