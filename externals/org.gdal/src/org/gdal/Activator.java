package org.gdal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
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
	private static final String GDALDATA_ZIP = "/gdaldata.zip";
	private static final String GDALDATA_CONFIG = "GDAL_DATA";

	private static BundleContext context;

	static BundleContext getContext()
	{
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		Activator.context = bundleContext;

		unpackData();
		setupGdalDataConfig();
		registerGdalDrivers();
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

	/**
	 * Unpack the GDAL_DATA folder into the plugin data area
	 */
	private void unpackData()
	{
		try
		{
			InputStream is = null;
			try
			{
				is = Activator.class.getResourceAsStream(GDALDATA_ZIP);
				ZipInputStream zis = new ZipInputStream(is);
				ZipEntry entry = zis.getNextEntry();
				while (entry != null)
				{
					if (!entry.isDirectory())
					{
						File output = context.getDataFile(entry.getName());
						if (output.length() > 0)
						{
							// File already exists - move on to the next one
							continue;
						}

						writeStreamToFile(zis, output);
					}
					zis.closeEntry();
					entry = zis.getNextEntry();
				}
			}
			finally
			{
				if (is != null)
				{
					is.close();
				}
			}
		}
		catch (IOException e)
		{
			// Catch exceptions occurring while closing the stream etc.
		}
	}

	/**
	 * Setup the GDAL_DATA configuration setting to point to the data area for
	 * the plugin
	 */
	private void setupGdalDataConfig()
	{
		gdal.SetConfigOption(GDALDATA_CONFIG, context.getDataFile("").getAbsolutePath());
	}

	/**
	 * Register required GDAL drivers and OGR transforms etc.
	 */
	private void registerGdalDrivers()
	{
		gdal.AllRegister();
		ogr.RegisterAll();
	}

	/**
	 * Write the contents of the input stream to the given file, overwriting any
	 * contents that already exist.
	 * 
	 * @param is
	 *            The input stream to read from
	 * @param file
	 *            The file to write to
	 */
	private static void writeStreamToFile(InputStream is, File file)
	{
		byte[] buffer = new byte[512];
		BufferedOutputStream out = null;
		try
		{
			try
			{
				out = new BufferedOutputStream(new FileOutputStream(file));
				while (true)
				{
					int read = is.read(buffer);
					if (read < 0)
					{
						break;
					}
					out.write(buffer, 0, read);
				}
			}
			finally
			{
				if (out != null)
				{
					out.close();
				}
			}
		}
		catch (IOException e)
		{
		}
	}
}