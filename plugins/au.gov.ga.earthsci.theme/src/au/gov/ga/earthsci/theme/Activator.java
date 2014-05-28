package au.gov.ga.earthsci.theme;

import net.jeeeyul.eclipse.themes.css.dynamicresource.JTDynamicResourceLocator;

import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends AbstractUIPlugin
{
	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ga.earthsci.theme"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator()
	{
	}

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;

		ServiceReference<IThemeManager> serviceReference = context.getServiceReference(IThemeManager.class);
		IThemeManager manager = context.getService(serviceReference);

		IThemeEngine engine = manager.getEngineForDisplay(Display.getDefault());
		engine.registerResourceLocator(new JTDynamicResourceLocator());
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault()
	{
		return plugin;
	}
}