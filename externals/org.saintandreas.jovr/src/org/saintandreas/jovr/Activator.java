package org.saintandreas.jovr;

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
		System.loadLibrary("OculusVR"); //load manually; JNA cannot find the native binary in an OSGI environment
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		Activator.context = null;
	}
}
