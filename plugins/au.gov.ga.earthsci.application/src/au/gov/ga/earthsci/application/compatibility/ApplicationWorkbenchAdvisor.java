package au.gov.ga.earthsci.application.compatibility;

import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * This workbench advisor creates the window advisor, and specifies the
 * perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor
{
	/*@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
	{
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}*/

	@Override
	public String getInitialWindowPerspectiveId()
	{
		return "au.gov.ga.earthsci.application.perspective"; //$NON-NLS-1$
	}
}
