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
package au.gov.ga.earthsci.worldwind.common.layers.volume;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.FloatBuffer;

import junit.framework.AssertionFailedError;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.worldwind.test.util.TestUtils;

/**
 * Unit tests for the {@link SGridVolumeDataProvider} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SGridVolumeDataProviderTest
{
	private static final URL ASCII_FILE = TestUtils.resolveFileURL(SGridVolumeDataProviderTest.class.getClassLoader().getResource("au/gov/ga/earthsci/worldwind/common/layers/model/gocad/sgrid/test_sgrid_ascii.sg"));
	private static final URL ASCII_ZIP_FILE = TestUtils.resolveFileURL(SGridVolumeDataProviderTest.class.getClassLoader().getResource("au/gov/ga/earthsci/worldwind/common/layers/model/gocad/sgrid/test_sgrid_ascii.zip"));
	private static final URL BINARY_FILE = TestUtils.resolveFileURL(SGridVolumeDataProviderTest.class.getClassLoader().getResource("au/gov/ga/earthsci/worldwind/common/layers/model/gocad/sgrid/test_sgrid_binary.sg"));

	private Mockery mockContext;
	private SGridVolumeDataProvider classUnderTest;
	private VolumeLayer parentLayer;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		
		classUnderTest = new SGridVolumeDataProvider();
		
		parentLayer = mockContext.mock(VolumeLayer.class);
		
		mockContext.checking(new Expectations(){{
			allowing(parentLayer).getCoordinateTransformation();will(returnValue(null));
			allowing(parentLayer).getPaintedVariableName();will(returnValue(null));
			allowing(parentLayer).dataAvailable(with(classUnderTest));
		}});
	}
	
	@Test
	public void testBasicReadAsciiFormat() throws Exception
	{
		boolean result = classUnderTest.doLoadData(ASCII_FILE, parentLayer);
		
		assertBasicProperties(result);
	}
	
	@Test
	public void testBasicReadAsciiFormatFromZip() throws Exception
	{
		boolean result = classUnderTest.doLoadData(ASCII_ZIP_FILE, parentLayer);
		
		assertBasicProperties(result);
	}
	
	@Test
	public void testPropertiesParsedOkAsciiFormat() throws Exception
	{
		classUnderTest.doLoadData(ASCII_FILE, parentLayer);
		
		assertEquals("test_sgrid_ascii__ascii@@", TestUtils.getField(classUnderTest, "asciiDataFile", String.class));
		assertEquals(null, TestUtils.getField(classUnderTest, "pointsDataFile", String.class));
		assertEquals((Integer)0, TestUtils.getField(classUnderTest, "pointsOffset", Integer.class));
		assertEquals("test_sgrid_ascii__flags@@", TestUtils.getField(classUnderTest, "flagsDataFile", String.class));
		assertEquals((Integer)0, TestUtils.getField(classUnderTest, "flagsOffset", Integer.class));
		
		GocadPropertyDefinition paintedProperty = TestUtils.getField(classUnderTest, "paintedProperty", GocadPropertyDefinition.class);
		assertNotNull(paintedProperty);
		assertEquals(null, paintedProperty.getFile());
		assertEquals(0, paintedProperty.getOffset());
		assertEquals("layer", paintedProperty.getName());
		assertEquals(1, paintedProperty.getId());
		assertEquals(-99999, paintedProperty.getNoDataValue(), 0.001);
		assertEquals(true, paintedProperty.isCellCentred());
	}
	
	@Test
	public void testZipReadsSameDataAsNonZip() throws Exception
	{
		classUnderTest.doLoadData(ASCII_ZIP_FILE, parentLayer);
		
		float[] zipData = classUnderTest.getData().array();
		
		setup();
		classUnderTest.doLoadData(ASCII_FILE, parentLayer);
		
		float[] normalData = classUnderTest.getData().array();
		
		assertArrayEquals(normalData, zipData, 0.001f);
	}
	
	@Test
	public void testBasicReadBinaryFormat() throws Exception
	{
		boolean result = classUnderTest.doLoadData(BINARY_FILE, parentLayer);
		
		assertBasicProperties(result);
	}

	@Test
	public void testPropertiesParsedOkBinaryFormat() throws Exception
	{
		classUnderTest.doLoadData(BINARY_FILE, parentLayer);
		
		assertEquals(null, TestUtils.getField(classUnderTest, "asciiDataFile", String.class));
		assertEquals("test_sgrid_binary__points@@", TestUtils.getField(classUnderTest, "pointsDataFile", String.class));
		assertEquals((Integer)0, TestUtils.getField(classUnderTest, "pointsOffset", Integer.class));
		assertEquals("test_sgrid_binary__flags@@", TestUtils.getField(classUnderTest, "flagsDataFile", String.class));
		assertEquals((Integer)0, TestUtils.getField(classUnderTest, "flagsOffset", Integer.class));
		
		GocadPropertyDefinition paintedProperty = TestUtils.getField(classUnderTest, "paintedProperty", GocadPropertyDefinition.class);
		assertNotNull(paintedProperty);
		assertEquals("test_sgrid_binary__layer@@", paintedProperty.getFile());
		assertEquals(0, paintedProperty.getOffset());
		assertEquals("layer", paintedProperty.getName());
		assertEquals("IEEE", paintedProperty.getType());
		assertEquals("RAW", paintedProperty.getFormat());
		assertEquals(1, paintedProperty.getId());
		assertEquals(-99999, paintedProperty.getNoDataValue(), 0.001);
		assertEquals(true, paintedProperty.isCellCentred());
	}
	
	@Test
	public void testBinaryReadsSameDataAsAscii() throws Exception
	{
		classUnderTest.doLoadData(ASCII_FILE, parentLayer);
		
		float[] asciiData = classUnderTest.getData().array();
		
		setup();
		classUnderTest.doLoadData(BINARY_FILE, parentLayer);
		
		float[] binaryData = classUnderTest.getData().array();
		
		assertArrayEquals(asciiData, binaryData, 0.001f);
	}
	
	private void assertBasicProperties(boolean result)
	{
		assertTrue(result);
		
		assertTrue(classUnderTest.isCellCentred());
		
		assertEquals(6, classUnderTest.getXSize());
		assertEquals(6, classUnderTest.getYSize());
		assertEquals(6, classUnderTest.getZSize());
		
		assertEquals(1, classUnderTest.getMinValue(), 0.001);
		assertEquals(5, classUnderTest.getMaxValue(), 0.001);
		
		FloatBuffer data = classUnderTest.getData();
		assertNotNull(data);
		assertEquals(5*5*5, data.capacity());
		assertDataPopulated(data.array());
		
		// Check that cell-centred clamping etc. is working
		for (int z = 0; z < classUnderTest.getZSize(); z++)
		{
			for (int y = 0; y < classUnderTest.getYSize(); y++)
			{
				for (int x = 0; x < classUnderTest.getXSize(); x++)
				{
					float expectedProperty = Math.min(z + 1, classUnderTest.getZSize() - 1);
					assertEquals(expectedProperty, classUnderTest.getValue(x, y, z), 0.001);
				}
			}
		}
		
		assertEquals(12500, classUnderTest.getTop(), 0.01);
		assertEquals(12500+112500, classUnderTest.getDepth(), 0.01);
		
	}
	
	private void assertDataPopulated(float[] data)
	{
		for (float f : data)
		{
			if (!Float.isNaN(f))
			{
				return;
			}
		}
		throw new AssertionFailedError("Data is all NaN");
	}
}
