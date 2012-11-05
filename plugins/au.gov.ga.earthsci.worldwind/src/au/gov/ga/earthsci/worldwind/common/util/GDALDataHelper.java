/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.worldwind.common.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gdal.gdal.gdal;

/**
 * This class extracts the contents of the gdaldata.zip file into the current
 * directory (user.dir property) or, failing that, the temporary directory
 * (java.io.tmpdir). The gdaldata.zip file is inserted into the jar files by the
 * Ant build scripts, and contains the data required by GDAL for certain
 * operations such as reprojecting datasets.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GDALDataHelper
{
	protected final static String DATA_ZIP = "/gdaldata.zip";
	protected final static String TEMP_DIR = ".gaww.gdaldata";
	protected final static String GDAL_DATA_PATH = "GDAL_DATA";

	public static void init()
	{
		InputStream is = null;
		try
		{
			is = GDALDataHelper.class.getResourceAsStream(DATA_ZIP);
			if (is != null)
			{
				//the findGdalDataFolder() of the GDALUtils class searches all subdirectories of the user.dir
				//directory for a directory that contains gdal_datum.csv, so first try extracting the zip
				//contents to a temporary directory in user.dir
				File tmpdir = new File(System.getProperty("user.dir") + "/" + TEMP_DIR);
				if (!tmpdir.mkdirs())
				{
					//if that doesn't work, just use the java.io.tmpdir
					tmpdir = new File(System.getProperty("java.io.tmpdir") + "/" + TEMP_DIR);
					tmpdir.mkdirs();
				}
				tmpdir.deleteOnExit();

				ZipInputStream zis = new ZipInputStream(is);
				ZipEntry entry = zis.getNextEntry();
				while (entry != null)
				{
					//ignore directories
					if (!entry.isDirectory())
					{
						File output = new File(tmpdir, entry.getName());
						writeStreamToFile(zis, output);
						output.deleteOnExit();
					}
					zis.closeEntry();
					entry = zis.getNextEntry();
				}

				gdal.SetConfigOption(GDAL_DATA_PATH, tmpdir.getAbsolutePath());
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				if (is != null)
					is.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	protected static void writeStreamToFile(InputStream is, File file)
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
