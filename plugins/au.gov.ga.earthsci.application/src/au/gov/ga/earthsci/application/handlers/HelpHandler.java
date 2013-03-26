package au.gov.ga.earthsci.application.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.AbstractHelpUI;

/**
 * A handler responsible for launching the help mechanism.
 * <p/>
 * Note that this is currently a placeholder required until the help mechanism
 * is ported to the Eclipse 4 platform.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class HelpHandler
{
	@Inject
	private Logger logger;

	@Inject
	private IExtensionRegistry extensionRegistry;

	private AbstractHelpUI pluggableHelpUI;

	static
	{
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}

	public void initializePluggableHelpUI()
	{
		logger.info("Initialising help UI"); //$NON-NLS-1$
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
		{
			@Override
			public void run()
			{
				// get the help UI extension from the registry
				IExtensionPoint point = extensionRegistry.getExtensionPoint("org.eclipse.ui.helpSupport"); //$NON-NLS-1$
				if (point == null)
				{
					// our extension point is missing (!) - act like there was
					// no help UI
					return;
				}
				IExtension[] extensions = point.getExtensions();
				if (extensions.length == 0)
				{
					// no help UI present
					return;
				}

				IConfigurationElement elementToUse = getFirstElement(extensions);
				if (elementToUse != null)
				{
					initializePluggableHelpUI(elementToUse);
				}
			}

			private IConfigurationElement getFirstElement(IExtension[] extensions)
			{
				// There should only be one extension/config element so we just take the first
				IConfigurationElement[] elements = extensions[0].getConfigurationElements();
				if (elements.length == 0)
				{
					// help UI present but mangled - act like there was no help UI
					return null;
				}
				return elements[0];
			}

			private boolean initializePluggableHelpUI(IConfigurationElement element)
			{
				// Instantiate the help UI
				try
				{
					pluggableHelpUI = (AbstractHelpUI) element.createExecutableExtension("class"); //$NON-NLS-1$
					return true;
				}
				catch (CoreException e)
				{
					logger.error("Unable to instantiate help UI" + e.getStatus(), e);//$NON-NLS-1$
				}
				return false;
			}

		});
	}

	@Execute
	public void execute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
	{
		if (pluggableHelpUI == null)
		{
			initializePluggableHelpUI();
		}
		pluggableHelpUI.displayHelp();
	}
}
