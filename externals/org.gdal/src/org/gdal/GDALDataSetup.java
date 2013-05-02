package org.gdal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;

/**
 * Setup the GDAL data configuration based on a data file source strategy
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class GDALDataSetup
{

	private static final String GDALDATA_ZIP = "gdaldata.zip";
	private static final String GDALDATA_CONFIG = "GDAL_DATA";

	private static Map<String, File> extractedDataMap = new ConcurrentHashMap<String, File>();
	
	/**
	 * A strategy interface for obtaining a target file for unpacking data
	 */
	public static interface DataFileSource
	{
		/**
		 * Return a file to unpack the named data file into.
		 */
		File getDataFile(String dataFileName);

		/**
		 * Return a reference to the data folder to use
		 */
		File getDataFolder();
	}


	/**
	 * Run the GDAL data setup using the provided data file source strategy
	 */
	public static void run(DataFileSource fileSource)
	{
		unpackData(fileSource);
		setupGdalDataConfig(fileSource);
		registerGdalDrivers();
	}

	/**
	 * Return the File object of the extracted data file with the given name, or <code>null</code> 
	 * if no such file exists.
	 * 
	 * @param name The name of the file to retrieve
	 * 
	 * @return The named GDAL data file
	 */
	public static File getDataFile(String name)
	{
		return extractedDataMap.get(name);
	}
	
	/**
	 * Unpack the GDAL_DATA folder into the plugin data area
	 */
	private static void unpackData(DataFileSource fileSource)
	{
		try
		{
			InputStream is = null;
			try
			{
				is = GDALDataSetup.class.getResourceAsStream(GDALDATA_ZIP);
				ZipInputStream zis = new ZipInputStream(is);
				ZipEntry entry = zis.getNextEntry();
				while (entry != null)
				{
					if (!entry.isDirectory())
					{
						String name = entry.getName();
						File output = fileSource.getDataFile(name);
						
						extractedDataMap.put(name, output);
						if (output.length() > 0)
						{
							// File already exists - move on to the next one
							entry = zis.getNextEntry();
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
	private static void setupGdalDataConfig(DataFileSource fileSource)
	{
		gdal.SetConfigOption(GDALDATA_CONFIG, fileSource.getDataFolder().getAbsolutePath());
	}

	/**
	 * Register required GDAL drivers and OGR transforms etc.
	 */
	private static void registerGdalDrivers()
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
