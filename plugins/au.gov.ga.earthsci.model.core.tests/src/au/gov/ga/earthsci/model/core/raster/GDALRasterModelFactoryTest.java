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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.gdal.gdal.Dataset;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.math.vector.Vector3;
import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.model.bounds.BoundingBox;
import au.gov.ga.earthsci.model.data.IModelData;
import au.gov.ga.earthsci.model.geometry.IMeshGeometry;

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
		GDALTestUtils.initGDAL();
	}

	@AfterClass
	public static void destroy()
	{
		GDALTestUtils.destroyGDAL();
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
		Dataset ds = GDALTestUtils.openRaster("testgrid.asc"); //$NON-NLS-1$
		GDALRasterModelParameters parameters = new GDALRasterModelParameters(ds);
		parameters.setModelName("testgrid"); //$NON-NLS-1$

		GDALRasterModel result = GDALRasterModelFactory.createModel(ds, parameters);

		assertLoadedModelCorrect(result, "testgrid", ds.GetDescription(), //$NON-NLS-1$
				24, 4, 6,
				0, 150,
				50, 300,
				-9999, 100,
				new int[] { 0, 4, 1, 5, 2, 6, 3, 7, 7, 7, 4, 4, 4, 8, 5, 9 });
	}

	@Test
	public void testCreateWithSubsampling() throws Exception
	{
		Dataset ds = GDALTestUtils.openRaster("testgrid.asc"); //$NON-NLS-1$
		GDALRasterModelParameters parameters = new GDALRasterModelParameters(ds);
		parameters.setSubsample(3);

		GDALRasterModel result = GDALRasterModelFactory.createModel(ds, parameters);

		assertLoadedModelCorrect(result, "testgrid.asc", ds.GetDescription(), //$NON-NLS-1$
				4, 2, 2,
				0, 150,
				150, 300,
				-9999, 32,
				new int[] { 0, 2, 1, 3 });
	}

	private void assertLoadedModelCorrect(GDALRasterModel result,
			String name, String description,
			int expectedNumVertices, int xSize, int ySize,
			double minX, double maxX,
			double minY, double maxY,
			double minZ, double maxZ,
			int[] exampleEdges)
	{
		// Single result with correct names
		assertNotNull(result);
		assertFalse(Util.isEmpty(result.getId()));
		assertEquals(name, result.getName());
		assertEquals(description, result.getDescription());

		// Containing a single geometry
		assertNotNull(result.getGeometries());
		assertEquals(1, result.getGeometries().size());

		IMeshGeometry geometry = (IMeshGeometry) result.getGeometries().get(0);
		assertNotNull(geometry);

		// With the correct vertices
		assertTrue(geometry.hasVertices());
		IModelData vertexData = geometry.getVertices();
		assertNotNull(vertexData);
		assertEquals(BufferType.FLOAT, vertexData.getBufferType());
		assertEquals(expectedNumVertices * 3 * BufferType.FLOAT.getNumberOfBytes(), vertexData.getSource().limit());

		// And a bounding volume that encompasses the vertices
		assertTrue(geometry.hasBoundingVolume());

		BoundingBox bounds = (BoundingBox) geometry.getBoundingVolume();
		assertNotNull(bounds);
		assertEquals(minX, bounds.getXRange().getMinValue(), 0.001);
		assertEquals(maxX, bounds.getXRange().getMaxValue(), 0.001);
		assertEquals(minY, bounds.getYRange().getMinValue(), 0.001);
		assertEquals(maxY, bounds.getYRange().getMaxValue(), 0.001);
		assertEquals(minZ, bounds.getZRange().getMinValue(), 0.001);
		assertEquals(maxZ, bounds.getZRange().getMaxValue(), 0.001);

		ByteBuffer source = vertexData.getSource();
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
		assertEquals(expectedNumVertices, count);

		// Edges should be set
		assertTrue(geometry.hasEdgeIndices());
		IModelData edgeData = geometry.getEdgeIndices();
		assertNotNull(edgeData);
		assertEquals(BufferType.INT, edgeData.getBufferType());

		int numIndices = (2 * xSize * (ySize - 1)) + 4 * (ySize - 2);
		assertEquals(numIndices * BufferType.INT.getNumberOfBytes(), edgeData.getSource().limit());

		source = edgeData.getSource();
		for (int i = 0; i < exampleEdges.length; i++)
		{
			assertEquals(exampleEdges[i], source.getInt());
		}

		// A renderer should always be set
		assertNotNull(geometry.getRenderer());
	}


}
