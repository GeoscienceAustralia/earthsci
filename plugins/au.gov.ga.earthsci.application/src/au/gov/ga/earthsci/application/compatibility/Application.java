package au.gov.ga.earthsci.application.compatibility;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication
{
	private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

	@Override
	public Object start(IApplicationContext context) throws Exception
	{
		//this is a dodgy way to override the model factory used by everything to create model
		//elements (the org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl class)
		/*EPackage.Registry.INSTANCE.put(BasicPackageImpl.eNS_URI, new EPackage.Descriptor()
		{
			@Override
			public EPackage getEPackage()
			{
				return null; //TODO should this be?
			}

			@Override
			public EFactory getEFactory()
			{
				return new MyCustomFactoryClass();
			}
		});*/

		Display display = PlatformUI.createDisplay();
		try
		{
			// look and see if there's a splash shell we can parent off of
			Shell shell = WorkbenchPlugin.getSplashShell(display);
			if (shell == null)
			{
				shell = new Shell(display);
			}

			Object instanceLocationCheck = WorkspaceHelper.checkInstanceLocation(shell, context.getArguments());
			if (instanceLocationCheck != null)
			{
				WorkbenchPlugin.unsetSplashShell(display);
				context.applicationRunning();
				return instanceLocationCheck;
			}

			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());

			// the workbench doesn't support relaunch yet (bug 61809) so
			// for now restart is used, and exit data properties are checked
			// here to substitute in the relaunch return code if needed
			if (returnCode != PlatformUI.RETURN_RESTART)
			{
				return EXIT_OK;
			}

			// if the exit code property has been set to the relaunch code, then
			// return that code now, otherwise this is a normal restart
			return EXIT_RELAUNCH.equals(Integer.getInteger(PROP_EXIT_CODE)) ? EXIT_RELAUNCH
					: EXIT_RESTART;
		}
		finally
		{
			display.dispose();
		}
	}

	@Override
	public void stop()
	{
		if (!PlatformUI.isWorkbenchRunning())
		{
			return;
		}
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!display.isDisposed())
				{
					workbench.close();
				}
			}
		});
	}
}
