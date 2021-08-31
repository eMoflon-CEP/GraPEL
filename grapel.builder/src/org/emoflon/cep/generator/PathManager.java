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

/**
 * Path manager for GrapeL code generation
 */
public class PathManager {
	
	// project + model
	private IProject project;
	private String projectName;
	private GrapeLModelContainer container;
	
	private NSManager names;
	
	private String projectLocation;
	
	// emoflon folders
	private String eMoflonAPIFolder;
	private String eMoflonPatternFolder;
	private String eMoflonMatchFolder;
	
	// event + handler folders
	private String basePackageLocation;
	private String eventFolder;
	private String handlerFolder;
	private String eventMonitorFolder;
	private String patternMonitorFolder;
	
	// maps for elements to be generated
	private Map<String, String> patternMonitors = new HashMap<>();
	private Map<String, String> eventMonitors = new HashMap<>();
	private Map<String, String> events = new HashMap<>();
	private Map<String, String> ruleEvents = new HashMap<>();
	private Map<String, String> eventHandler = new HashMap<>();
	
	/**
	 * Creates a mapping for monitor names to folder paths of the maintainance monitor and monitors for each pattern and event
	 */
	private void mapMonitors() {
		container.getEventPatterns().forEach(pattern -> {
			patternMonitors.put(pattern.getName(), patternMonitorFolder+"/"+StringUtil.firstToUpper(pattern.getName())+".mon");
		});
		patternMonitors.put(NSManager.MAINTAINANCE_MONITOR, patternMonitorFolder+"/Maintainance.mon");
		
		container.getEvents().forEach(event -> {
			eventMonitors.put(event.getName(), eventMonitorFolder+"/"+names.getEventName(event.getName())+".mon");
		});
	}
	
	/**
	 * Creates a mapping for event names to folder paths of all events and event handlers
	 */
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
	
	/**
	 * Constructor for the path manager specifying the project, the name space manager and the GrapeL model container
	 * @param project the project the path manager operates in
	 * @param names the name space manager including the name mapping for the project
	 * @param container the model container that includes all elements that should be generated specified in the grapel file
	 */
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
	
	/**
	 * Ensures, that all folders required for code generation exist.
	 * If the folders do not exists, they will be created.
	 * @throws CoreException if folder generation fails
	 */
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
	
	/**
	 * Returns the location to the correlator
	 * @return the location of the the correlator
	 */
	public String getCorrelatorLocation( ) {
		return container.getCorrelatorLocation();
	}
	
	/**
	 * Returns the location to all pattern monitors
	 * @return a collection of strings with all locations to the pattern monitors
	 */
	public Collection<String> getPatternMonitorLocations() {
		return patternMonitors.values();
	}
	
	/**
	 * Returns the location to the API
	 * @return the location to the GrapeL API
	 */
	public String getAPILocation() {
		return basePackageLocation+"/"+names.getAPIName()+".java";
	}
	
	/**
	 * Returns the location to the API engine
	 * @param engineName the name of the engine
	 * @return the location to the API engine by the engine name
	 */
	public String getAPIEngineLocation(String engineName) {
		return basePackageLocation+"/"+names.getEngineName(engineName)+".java";
	}
	
	/**
	 * Returns the location to a specific event
	 * @param eventName the name of the event
	 * @return the location an event specified by its event name
	 */
	public String getEventLocation(String eventName) {
		return events.get(eventName);
	}
	
	/**
	 * Returns the location to a specific rule event
	 * @param eventName the name of the rule event
	 * @return the location an rule event specified by its event name
	 */
	public String getRuleEventLocation(String eventName) {
		return ruleEvents.get(eventName);
	}
	
	/**
	 * Returns the location to a specific event handler
	 * @param eventHandlerName the name of the event handler
	 * @return the location an event handler specified by its event handler name
	 */
	public String getEventHandlerLocation(String eventHandlerName) {
		return eventHandler.get(eventHandlerName);
	}
	
	/**
	 * Returns the location to a specific match event handler
	 * @param patternName the pattern name of the match event handler
	 * @return the location an match event handler specified by its pattern name
	 */
	public String getMatchEventHandlerLocation(String patternName) {
		return getEventHandlerLocation(patternName);
	}
	
	/**
	 * Returns the location to a specific event monitor
	 * @param eventName the event name of the event monitor
	 * @return the location an event monitor specified by its event name
	 */
	public String getEventMonitorLocation(String eventName) {
		return eventMonitors.get(eventName);
	}
	
	/**
	 * Returns the location to a specific match event monitor
	 * @param patternName the pattern name of the event match monitor
	 * @return the location an match event monitor specified by its pattern name
	 */
	public String getMatchEventMonitorLocation(String patternName) {
		return getEventMonitorLocation(patternName);
	}
	
	/**
	 * Returns the location to a specific event pattern monitor
	 * @param eventPatternName the event pattern name of the event pattern monitor
	 * @return the location an event pattern monitor specified by its event pattern name
	 */
	public String getEventPatternMonitorLocation(String eventPatternName) {
		return patternMonitors.get(eventPatternName);
	}
}
