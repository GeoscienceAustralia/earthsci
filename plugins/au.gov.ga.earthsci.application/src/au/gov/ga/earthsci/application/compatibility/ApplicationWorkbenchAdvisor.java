package au.gov.ga.earthsci.application.compatibility;

import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;

import au.gov.ga.earthsci.notification.ui.statushandlers.NotificationErrorHandler;

/**
 * This workbench advisor creates the window advisor, and specifies the
 * perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor
{

	@Override
	public String getInitialWindowPerspectiveId()
	{
		return "au.gov.ga.earthsci.application.perspective"; //$NON-NLS-1$
	}

	@Override
	public synchronized AbstractStatusHandler getWorkbenchErrorHandler()
	{
		return new NotificationErrorHandler();
	}
}
