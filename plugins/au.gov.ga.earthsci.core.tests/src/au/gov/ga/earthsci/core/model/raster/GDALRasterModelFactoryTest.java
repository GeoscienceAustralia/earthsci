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
import java.nio.ByteBuffer;

import org.gdal.GDALDataSetup;
import org.gdal.GDALDataSetup.DataFileSource;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdal.gdalJNI;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.math.vector.Vector3;
import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.model.bounds.BoundingBox;
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
		if (!gdalJNI.isAvailable())
		{
			return;
		}

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
		if (!gdalJNI.isAvailable())
		{
			return;
		}

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

		// Single result with correct names
		assertNotNull(result);
		assertFalse(Util.isEmpty(result.getId()));
		assertEquals("testgrid", result.getName()); //$NON-NLS-1$
		assertEquals(ds.GetDescription(), result.getDescription());

		// Containing a single geometry
		assertNotNull(result.getGeometries());
		assertEquals(1, result.getGeometries().size());

		IVertexColouredGeometry geometry = (IVertexColouredGeometry) result.getGeometries().get(0);
		assertNotNull(geometry);

		// With the correct vertices
		assertTrue(geometry.hasVertices());
		IModelData vertexData = geometry.getVertices();
		assertNotNull(vertexData);
		assertEquals(BufferType.FLOAT, vertexData.getBufferType());
		assertEquals(24 * 3 * BufferType.FLOAT.getNumberOfBytes(), vertexData.getSource().limit());

		// And a bounding volume that encompasses the vertices
		assertTrue(geometry.hasBoundingVolume());

		BoundingBox bounds = (BoundingBox) geometry.getBoundingVolume();
		assertNotNull(bounds);
		assertEquals(0.0, bounds.getXRange().getMinValue(), 0.001);
		assertEquals(150.0, bounds.getXRange().getMaxValue(), 0.001);
		assertEquals(50.0, bounds.getYRange().getMinValue(), 0.001);
		assertEquals(300.0, bounds.getYRange().getMaxValue(), 0.001);
		assertEquals(-9999.0, bounds.getZRange().getMinValue(), 0.001);
		assertEquals(100.0, bounds.getZRange().getMaxValue(), 0.001);

		ByteBuffer source = (ByteBuffer) vertexData.getSource();
		int count = 0;
		Vector3 vertex = new Vector3();
		while (source.hasRemaining())
		{
			vertex.x = vertexData.getBufferType().getValueFrom(source).doubleValue();
			vertex.y = vertexData.getBufferType().getValueFrom(source).doubleValue();
			vertex.z = vertexData.getBufferType().getValueFrom(source).doubleValue();
			assertTrue(bounds.contains(vertex));
			count++;
		}
		assertEquals(24, count);

		// A renderer should always be set
		assertNotNull(geometry.getRenderer());
	}

	private static Dataset openRaster(String rasterName) throws Exception
	{
		URL rasterUrl = TestUtils.resolveFileURL(GDALRasterModelFactoryTest.class.getResource(rasterName));

		Assume.assumeTrue(GDALUtils.canOpen(rasterUrl));

		return GDALUtils.open(rasterUrl);
	}
}
