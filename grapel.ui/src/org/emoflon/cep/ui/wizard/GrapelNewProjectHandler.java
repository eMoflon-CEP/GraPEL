package org.emoflon.cep.ui.wizard;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.moflon.core.ui.UiUtilities;

public class GrapelNewProjectHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		try {
			UiUtilities.openWizard(GrapelNewProjectWizard.PROJECT_WIZARD_ID, window);
		} catch (final Exception e) {
			Logger.getRootLogger().info("Cannot init New GraPEL Project Wizard");
		}
		return null;
	}

}
