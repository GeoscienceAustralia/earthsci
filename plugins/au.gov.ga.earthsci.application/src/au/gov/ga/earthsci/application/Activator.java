package au.gov.ga.earthsci.application;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{
	private static BundleContext context;

	static BundleContext getContext()
	{
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		Activator.context = bundleContext;

		System.setProperty("java.net.useSystemProxies", "true"); //TODO make configurable!
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		Activator.context = null;
	}
}
