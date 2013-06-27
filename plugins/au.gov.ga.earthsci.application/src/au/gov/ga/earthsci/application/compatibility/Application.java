package au.gov.ga.earthsci.application.compatibility;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication
{
	@Override
	public Object start(IApplicationContext context)
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
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
			{
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
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
