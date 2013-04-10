package org.gdal;

import java.io.File;

import org.gdal.GDALDataSetup.DataFileSource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator for the GDAL plugin.
 * 
 * Responsible for unpacking the GDAL data files and setting up appropriate
 * configurations.
 * 
 * @author James Navin (james.navin@ga.gov.au)
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

		GDALDataSetup.run(new DataFileSource()
		{
			@Override
			public File getDataFolder()
			{
				return context.getDataFile("");
			}

			@Override
			public File getDataFile(String dataFileName)
			{
				return context.getDataFile(dataFileName);
			}
		});
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		Activator.context = null;
	}

	public static String getBundleName()
	{
		return context.getBundle().getSymbolicName();
	}


}