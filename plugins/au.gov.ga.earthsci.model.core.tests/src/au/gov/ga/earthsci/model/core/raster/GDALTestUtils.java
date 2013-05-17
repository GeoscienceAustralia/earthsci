/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.model.core.raster;

import gov.nasa.worldwind.util.gdal.GDALUtils;

import java.io.File;
import java.net.URL;

import org.gdal.GDALDataSetup;
import org.gdal.GDALDataSetup.DataFileSource;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdal.gdalJNI;
import org.junit.Assume;

import au.gov.ga.earthsci.test.util.TestUtils;

/**
 * Utilities for working with GDAL within a unit test
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALTestUtils
{

	public static void initGDAL()
	{
		if (!gdalJNI.isAvailable())
		{
			return;
		}

		gdal.PushErrorHandler("CPLQuietErrorHandler"); //$NON-NLS-1$

		if (gdal.GetConfigOption("GDAL_DATA") == null) //$NON-NLS-1$
		{
			GDALDataSetup.run(new DataFileSource()
			{
				@Override
				public File getDataFile(String dataFileName)
				{
					File result = new File(System.getProperty("java.io.tmpdir"), dataFileName); //$NON-NLS-1$
					result.deleteOnExit();
					return result;
				}

				@Override
				public File getDataFolder()
				{
					return new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
				}
			});
		}
	}

	public static void destroyGDAL()
	{
		if (!gdalJNI.isAvailable())
		{
			return;
		}

		gdal.PopErrorHandler();
	}

	public static Dataset openRaster(String rasterName) throws Exception
	{
		URL rasterUrl = TestUtils.resolveFileURL(GDALRasterModelFactoryTest.class.getResource(rasterName));

		Assume.assumeTrue(GDALUtils.canOpen(rasterUrl));

		return GDALUtils.open(rasterUrl);
	}
}
