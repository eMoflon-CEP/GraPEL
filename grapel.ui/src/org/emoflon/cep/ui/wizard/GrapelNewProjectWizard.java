package org.emoflon.cep.ui.wizard;

import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.xtext.ui.wizard.DefaultProjectInfo;
import org.eclipse.xtext.ui.wizard.IExtendedProjectInfo;
import org.eclipse.xtext.ui.wizard.XtextNewProjectWizard;

import com.google.inject.Inject;

public class GrapelNewProjectWizard extends XtextNewProjectWizard {
	public static final String PROJECT_WIZARD_ID = "org.emoflon.cep.ui.wizard.GTNewProjectWizard";
	private static final String PROJECT_WIZARD_TITLE = "New GraPEL Project";
	private static final String PROJECT_WIZARD_DESCRIPTION = "Create a new GraPEL Project.";
	
	private WizardNewProjectCreationPage mainPage;
	
	@Inject
	public GrapelNewProjectWizard(GrapelProjectCreator projectCreator) {
		super(projectCreator);
		this.setWindowTitle(PROJECT_WIZARD_TITLE);
	}
	
	@Override
	public void addPages() {
		mainPage = new WizardNewProjectCreationPage("basicNewGrapeLProjectPage");
		mainPage.setTitle(PROJECT_WIZARD_TITLE);
		mainPage.setDescription(PROJECT_WIZARD_DESCRIPTION);
		this.addPage(mainPage); // TODO: Add meta model selection page
	}
	
	

	@Override
	protected IExtendedProjectInfo getProjectInfo() {
		DefaultProjectInfo projectInfo = new DefaultProjectInfo();
		projectInfo.setProjectName(mainPage.getProjectName());
		if(!mainPage.useDefaults())
			projectInfo.setLocationPath(mainPage.getLocationPath());
		return projectInfo;
	}

}
