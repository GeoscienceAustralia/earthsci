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

import java.util.HashMap;
import java.util.Map;

import org.gdal.gdal.Dataset;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMaps;

/**
 * Unit tests for the {@link GDALRasterModelParameters} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
@SuppressWarnings("nls")
public class GDALRasterModelParametersTest
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

	@Test
	public void testEmptyConstructor()
	{
		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters();

		assertParamsCorrect(classUnderTest, 1, null, null, null, null, null, null, ColorMaps.getRGBRainbowMap());
	}

	@Test
	public void testConstructWithNullDataset()
	{
		Dataset ds = null;

		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters(ds);

		assertParamsCorrect(classUnderTest, 1, null, null, null, null, null, null, ColorMaps.getRGBRainbowMap());
	}

	@Test
	public void testConstructWithDataset() throws Exception
	{
		Dataset ds = GDALTestUtils.openRaster("testgrid.tif");

		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters(ds);

		assertParamsCorrect(classUnderTest, 1, "testgrid.tif", ds.GetDescription(), null, null, null,
				ds.GetProjection(),
				ColorMaps.getRGBRainbowMap());
	}

	@Test
	public void testConstructWithNullParamMap() throws Exception
	{
		Map<String, String> params = null;

		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters(params);

		assertParamsCorrect(classUnderTest, 1, null, null, null, null, null, null, ColorMaps.getRGBRainbowMap());
	}

	@Test
	public void testConstructWithEmptyParamMap() throws Exception
	{
		Map<String, String> params = new HashMap<String, String>();

		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters(params);

		assertParamsCorrect(classUnderTest, 1, null, null, null, null, null, null, ColorMaps.getRGBRainbowMap());
	}

	@Test
	public void testConstructWithValidParamMap() throws Exception
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put(GDALRasterModelParameters.ELEVATION_BAND, "2");
		params.put(GDALRasterModelParameters.MODEL_NAME, "Model name");
		params.put(GDALRasterModelParameters.MODEL_DESCRIPTION, "Model description");
		params.put(GDALRasterModelParameters.SOURCE_SRS, "Model SRS");
		params.put(GDALRasterModelParameters.ELEVATION_OFFSET, "123.45");
		params.put(GDALRasterModelParameters.ELEVATION_SCALE, "-1e3");
		params.put(GDALRasterModelParameters.ELEVATION_SUBSAMPLE, "5");

		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters(params);

		assertParamsCorrect(classUnderTest, 2, "Model name", "Model description", -1000.0, 123.45, 5, "Model SRS",
				ColorMaps.getRGBRainbowMap());
	}

	@Test(expected = NumberFormatException.class)
	public void testConstructWithParamMapInvalidElevationBand() throws Exception
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put(GDALRasterModelParameters.ELEVATION_BAND, "2.1");
		new GDALRasterModelParameters(params);
	}

	@Test(expected = NumberFormatException.class)
	public void testConstructWithParamMapInvalidElevationScale() throws Exception
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put(GDALRasterModelParameters.ELEVATION_SCALE, "a");
		new GDALRasterModelParameters(params);
	}

	@Test(expected = NumberFormatException.class)
	public void testConstructWithParamMapInvalidElevationOffset() throws Exception
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put(GDALRasterModelParameters.ELEVATION_OFFSET, "a");
		new GDALRasterModelParameters(params);
	}

	@Test(expected = NumberFormatException.class)
	public void testConstructWithParamMapInvalidSubsample() throws Exception
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put(GDALRasterModelParameters.ELEVATION_SUBSAMPLE, "1.5");
		new GDALRasterModelParameters(params);
	}

	@Test
	public void testToParamMapWithMinimal() throws Exception
	{
		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters();

		Map<String, String> result = classUnderTest.asParameterMap();

		assertParamMapCorrect(result,
				true, "1",
				false, null,
				false, null,
				false, null,
				false, null,
				false, null,
				false, null);
	}

	@Test
	public void testToParamMapWithFullyPopulated() throws Exception
	{
		GDALRasterModelParameters classUnderTest = new GDALRasterModelParameters();
		classUnderTest.setElevationBandIndex(2);
		classUnderTest.setModelName("name");
		classUnderTest.setModelDescription("description");
		classUnderTest.setScaleFactor(2.5);
		classUnderTest.setOffset(250.0);
		classUnderTest.setSubsample(2);
		classUnderTest.setSourceProjection("srs");

		Map<String, String> result = classUnderTest.asParameterMap();

		assertParamMapCorrect(result,
				true, "2",
				true, "2.5",
				true, "250.0",
				true, "2",
				true, "name",
				true, "description",
				true, "srs");
	}

	private void assertParamsCorrect(GDALRasterModelParameters params,
			int rasterband, String name, String description,
			Double scale, Double offset,
			Integer subsample,
			String srs,
			ColorMap colormap)
	{
		assertEquals(rasterband, params.getElevationBandIndex());
		assertEquals(name, params.getModelName());
		assertEquals(description, params.getModelDescription());
		assertEquals(offset, params.getOffset());
		assertEquals(scale, params.getScaleFactor());
		assertEquals(subsample, params.getSubsample());
		assertEquals(srs, params.getSourceProjection());
		assertEquals(ColorMaps.getRGBRainbowMap(), params.getColorMap());
	}

	private void assertParamMapCorrect(Map<String, String> params,
			boolean hasBand, String band,
			boolean hasScale, String scale,
			boolean hasOffset, String offset,
			boolean hasSubsample, String subsample,
			boolean hasName, String name,
			boolean hasDescription, String description,
			boolean hasSRS, String srs)
	{
		assertEquals(hasBand, params.containsKey(GDALRasterModelParameters.ELEVATION_BAND));
		assertEquals(band, params.get(GDALRasterModelParameters.ELEVATION_BAND));

		assertEquals(hasScale, params.containsKey(GDALRasterModelParameters.ELEVATION_SCALE));
		assertEquals(scale, params.get(GDALRasterModelParameters.ELEVATION_SCALE));

		assertEquals(hasOffset, params.containsKey(GDALRasterModelParameters.ELEVATION_OFFSET));
		assertEquals(offset, params.get(GDALRasterModelParameters.ELEVATION_OFFSET));

		assertEquals(hasSubsample, params.containsKey(GDALRasterModelParameters.ELEVATION_SUBSAMPLE));
		assertEquals(subsample, params.get(GDALRasterModelParameters.ELEVATION_SUBSAMPLE));

		assertEquals(hasName, params.containsKey(GDALRasterModelParameters.MODEL_NAME));
		assertEquals(name, params.get(GDALRasterModelParameters.MODEL_NAME));

		assertEquals(hasDescription, params.containsKey(GDALRasterModelParameters.MODEL_DESCRIPTION));
		assertEquals(description, params.get(GDALRasterModelParameters.MODEL_DESCRIPTION));

		assertEquals(hasSRS, params.containsKey(GDALRasterModelParameters.SOURCE_SRS));
		assertEquals(srs, params.get(GDALRasterModelParameters.SOURCE_SRS));
	}

}
