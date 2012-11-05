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
package au.gov.ga.earthsci.worldwind.common.layers.model.gdal;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.awt.Color;

import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;

/**
 * Unit tests for the {@link GDALRasterModelParameters} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelParametersTest
{

	@Test
	public void testDefaultConstructorDefaults()
	{
		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters();
		
		assertDefaults(classUnderTest);
	}

	@Test
	public void testNullArgConstructorDefaults()
	{
		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters(null);
		
		assertDefaults(classUnderTest);
	}
	
	@Test
	public void testConstructWithAVListParams()
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.TARGET_BAND, 3);
		params.setValue(AVKeyMore.MAX_VARIANCE, 5.0);
		ColorMap colorMap = new ColorMap();
		params.setValue(AVKeyMore.COLOR_MAP, colorMap);
		params.setValue(AVKeyMore.COORDINATE_SYSTEM, "TESTCOORDINATE");
		params.setValue(AVKeyMore.SCALE, 11.1);
		params.setValue(AVKeyMore.OFFSET, -100d);
		
		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters(params);
		
		assertEquals(3, classUnderTest.getBand());
		assertEquals(5.0, classUnderTest.getMaxVariance(), 0.001);
		assertEquals(Color.GRAY, classUnderTest.getDefaultColor());
		assertEquals(colorMap, classUnderTest.getColorMap());
		assertEquals("TESTCOORDINATE", classUnderTest.getCoordinateSystem());
		assertEquals(11.1, classUnderTest.getScaleFactor(), 0.001);
		assertEquals(-100, classUnderTest.getOffset(), 0.001);
	}
	
	private void assertDefaults(GDALRasterModelParameters classUnderTest)
	{
		assertEquals(1, classUnderTest.getBand());
		assertEquals(0, classUnderTest.getMaxVariance(), 0.001);
		assertEquals(Color.GRAY, classUnderTest.getDefaultColor());
		assertEquals(null, classUnderTest.getColorMap());
		assertEquals(null, classUnderTest.getCoordinateSystem());
		assertEquals(null, classUnderTest.getScaleFactor());
		assertEquals(null, classUnderTest.getOffset());
	}
}
