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
package au.gov.ga.earthsci.core.model.raster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.util.gdal.GDALUtils;

import java.io.File;
import java.net.URL;

import org.gdal.GDALDataSetup;
import org.gdal.GDALDataSetup.DataFileSource;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.model.data.IModelData;
import au.gov.ga.earthsci.model.geometry.IVertexColouredGeometry;
import au.gov.ga.earthsci.test.util.TestUtils;

/**
 * Unit tests for the {@link GDALRasterModelFactory}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelFactoryTest
{

	@BeforeClass
	public static void init()
	{
		gdal.PushErrorHandler("CPLQuietErrorHandler");

		if (gdal.GetConfigOption("GDAL_DATA") == null)
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

	@AfterClass
	public static void destroy()
	{
		gdal.PopErrorHandler();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNullDataset() throws Exception
	{
		Dataset ds = null;
		GDALRasterModelParameters parameters = new GDALRasterModelParameters(ds);

		GDALRasterModelFactory.createModel(ds, parameters);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNullParameters() throws Exception
	{
		Dataset ds = null;
		GDALRasterModelParameters parameters = new GDALRasterModelParameters(ds);

		GDALRasterModelFactory.createModel(ds, parameters);
	}

	@Test
	public void testCreateWithValidRasterAndParams() throws Exception
	{
		Dataset ds = openRaster("testgrid.asc");
		GDALRasterModelParameters parameters = new GDALRasterModelParameters(ds);
		parameters.setModelName("testgrid");

		GDALRasterModel result = GDALRasterModelFactory.createModel(ds, parameters);

		assertNotNull(result);
		assertFalse(Util.isEmpty(result.getId()));
		assertEquals("testgrid", result.getName()); //$NON-NLS-1$
		assertEquals(ds.GetDescription(), result.getDescription());

		assertNotNull(result.getGeometries());
		assertEquals(1, result.getGeometries().size());

		IVertexColouredGeometry geometry = (IVertexColouredGeometry) result.getGeometries().get(0);
		assertNotNull(geometry);

		assertTrue(geometry.hasVertices());
		IModelData vertices = geometry.getVertices();
		assertNotNull(vertices);
	}

	private static Dataset openRaster(String rasterName) throws Exception
	{
		URL rasterUrl = TestUtils.resolveFileURL(GDALRasterModelFactoryTest.class.getResource(rasterName));

		Assume.assumeTrue(GDALUtils.canOpen(rasterUrl));

		return GDALUtils.open(rasterUrl);
	}
}
