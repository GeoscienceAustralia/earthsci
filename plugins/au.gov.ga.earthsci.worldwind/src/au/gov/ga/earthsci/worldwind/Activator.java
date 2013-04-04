package au.gov.ga.earthsci.worldwind;

import gov.nasa.worldwind.util.gdal.GDALUtils;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Plugin activator for the Earthsci Worldwind plugin
 */
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
		GDALUtilsHack.override();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		Activator.context = null;
	}

	/*
	 * The GDALUtils class assumes the application is running outside the OSGI
	 * environment and makes changes to library loaders and paths etc. in order
	 * to load the GDAL library. This is already taken care of for us by the
	 * OSGI runtime, so we need to trick the util class into thinking its done
	 * its job and not fail when we know the library has in fact been loaded.
	 */
	private static class GDALUtilsHack extends GDALUtils
	{
		public static void override()
		{
			GDALUtils.gdalIsAvailable.set(true);
		}
	}
}
