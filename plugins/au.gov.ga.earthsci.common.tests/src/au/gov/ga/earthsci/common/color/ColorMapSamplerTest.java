package au.gov.ga.earthsci.common.color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;

import org.junit.Test;

/**
 * Unit tests for the {@link ColorMapSampler} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class ColorMapSamplerTest
{

	@Test(expected = IllegalArgumentException.class)
	public void testSampleWithNullMap()
	{
		ColorMap map = null;
		int numSamples = 100;
		double min = 0;
		double max = 1;

		ColorMapSampler.sample(map, numSamples, min, max);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSampleWithNegativeSamples()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = -100;
		double min = 0;
		double max = 1;

		ColorMapSampler.sample(map, numSamples, min, max);
	}

	@Test
	public void testSampleWithZeroSamples()
	{
		ColorMap map = ColorMaps.getRBGRainbowMap();
		int numSamples = 0;
		double min = 0;
		double max = 1;

		Color[] result = ColorMapSampler.sample(map, numSamples, min, max);

		assertNotNull(result);
		assertEquals(0, result.length);
	}

	@Test
	public void testSampleWithMultipleSamples()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 100;
		double min = 0;
		double max = 1;

		Color[] result = ColorMapSampler.sample(map, numSamples, min, max);

		assertNotNull(result);
		assertEquals(numSamples, result.length);

		assertEquals(map.getFirstEntry().getValue(), result[0]);
		assertEquals(map.getLastEntry().getValue(), result[numSamples - 1]);
	}

	@Test
	public void testSampleWithSingleSample()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 1;
		double min = 0;
		double max = 1;

		Color[] result = ColorMapSampler.sample(map, numSamples, min, max);

		assertNotNull(result);
		assertEquals(numSamples, result.length);

		assertEquals(map.getFirstEntry().getValue(), result[0]);
		assertEquals(map.getFirstEntry().getValue(), result[numSamples - 1]);
	}

	@Test
	public void testSampleWithTwoSamples()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 2;
		double min = 0;
		double max = 1;

		Color[] result = ColorMapSampler.sample(map, numSamples, min, max);

		assertNotNull(result);
		assertEquals(numSamples, result.length);

		assertEquals(map.getFirstEntry().getValue(), result[0]);
		assertEquals(map.getLastEntry().getValue(), result[numSamples - 1]);
	}

	@Test
	public void testSampleWithSameMinMax()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 10;
		double min = 0;
		double max = 0;

		Color[] result = ColorMapSampler.sample(map, numSamples, min, max);

		assertNotNull(result);
		assertEquals(numSamples, result.length);

		for (int i = 0; i < result.length; i++)
		{
			assertEquals(map.getFirstEntry().getValue(), result[i]);
		}
	}

	@Test
	public void testSampleWithPositiveOffsetEnoughCapacity()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		Color[] result = new Color[10];
		int numSamples = 5;
		int offset = 5;
		double min = 0;
		double max = 1;

		ColorMapSampler.sample(map, numSamples, min, max, result, offset);

		for (int i = 0; i < offset; i++)
		{
			assertEquals(null, result[i]);
		}
		assertEquals(map.getFirstEntry().getValue(), result[5]);
		assertEquals(map.getLastEntry().getValue(), result[9]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSampleWithPositiveOffsetNotEnoughCapacity()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		Color[] result = new Color[10];
		int numSamples = 5;
		int offset = 6;
		double min = 0;
		double max = 1;

		ColorMapSampler.sample(map, numSamples, min, max, result, offset);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSampleWithNegativeOffset()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		Color[] result = new Color[10];
		int numSamples = 5;
		int offset = -5;
		double min = 0;
		double max = 1;

		ColorMapSampler.sample(map, numSamples, min, max, result, offset);
	}


}
