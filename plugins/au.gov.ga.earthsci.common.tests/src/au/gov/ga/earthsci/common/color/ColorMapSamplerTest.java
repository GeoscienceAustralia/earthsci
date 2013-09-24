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

	@Test(expected = IllegalArgumentException.class)
	public void testSampleFloatWithNullMap()
	{
		ColorMap map = null;
		int numSamples = 10;
		double minValue = 0;
		double maxValue = 1;
		ColorType type = ColorType.RGBA;

		ColorMapSampler.sample(map, numSamples, minValue, maxValue, type);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSampleFloatWithNegativeSamples()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = -10;
		double minValue = 0;
		double maxValue = 1;
		ColorType type = ColorType.RGBA;

		ColorMapSampler.sample(map, numSamples, minValue, maxValue, type);
	}

	@Test
	public void testSampleFloatWithZeroSamples()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 0;
		double minValue = 0;
		double maxValue = 1;
		ColorType type = ColorType.RGBA;

		float[] result = ColorMapSampler.sample(map, numSamples, minValue, maxValue, type);

		assertNotNull(result);
		assertEquals(0, result.length);
	}


	@Test
	public void testSampleFloatWithSingleSampleRGBA()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 1;
		double minValue = 0;
		double maxValue = 10;
		ColorType type = ColorType.RGBA;

		float[] result = ColorMapSampler.sample(map, numSamples, minValue, maxValue, type);

		assertNotNull(result);
		assertEquals(4, result.length);

		assertEquals(1.0f, result[0], 0.001f);
		assertEquals(0.0f, result[1], 0.001f);
		assertEquals(0.0f, result[2], 0.001f);
		assertEquals(1.0f, result[3], 0.001f);
	}

	@Test
	public void testSampleFloatWithSingleSampleRGB()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 1;
		double minValue = 0;
		double maxValue = 1;
		ColorType type = ColorType.RGB;

		float[] result = ColorMapSampler.sample(map, numSamples, minValue, maxValue, type);

		assertNotNull(result);
		assertEquals(3, result.length);

		assertEquals(1.0f, result[0], 0.001f);
		assertEquals(0.0f, result[1], 0.001f);
		assertEquals(0.0f, result[2], 0.001f);
	}

	@Test
	public void testSampleFloatWithMultipleSamples()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 10;
		double minValue = 0;
		double maxValue = 1;
		ColorType type = ColorType.RGBA;

		float[] result = ColorMapSampler.sample(map, numSamples, minValue, maxValue, type);

		assertNotNull(result);
		assertEquals(40, result.length);

		assertEquals(1.0f, result[0], 0.001f);
		assertEquals(0.0f, result[1], 0.001f);
		assertEquals(0.0f, result[2], 0.001f);
		assertEquals(1.0f, result[3], 0.001f);

		assertEquals(0.0f, result[36], 0.001f);
		assertEquals(0.0f, result[37], 0.001f);
		assertEquals(1.0f, result[38], 0.001f);
		assertEquals(1.0f, result[39], 0.001f);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSampleFloatWithNotEnoughCapacity()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 10;
		double minValue = 0;
		double maxValue = 1;
		int offset = 0;
		ColorType type = ColorType.RGBA;

		float[] result = new float[39];

		ColorMapSampler.sample(map, numSamples, minValue, maxValue, result, offset, type);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSampleFloatWithNegativeOffset()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 10;
		double minValue = 0;
		double maxValue = 1;
		int offset = -1;
		ColorType type = ColorType.RGBA;

		float[] result = new float[40];

		ColorMapSampler.sample(map, numSamples, minValue, maxValue, result, offset, type);
	}

	@Test
	public void testSampleFloatWithPositiveOffset()
	{
		ColorMap map = ColorMaps.getRGBRainbowMap();
		int numSamples = 10;
		double minValue = 0;
		double maxValue = 1;
		int offset = 10;
		ColorType type = ColorType.RGBA;

		float[] result = new float[50];

		ColorMapSampler.sample(map, numSamples, minValue, maxValue, result, offset, type);

		assertEquals(0.0f, result[0], 0.001f);
		assertEquals(0.0f, result[1], 0.001f);
		assertEquals(0.0f, result[2], 0.001f);
		assertEquals(0.0f, result[3], 0.001f);

		assertEquals(1.0f, result[10], 0.001f);
		assertEquals(0.0f, result[11], 0.001f);
		assertEquals(0.0f, result[12], 0.001f);
		assertEquals(1.0f, result[13], 0.001f);

		assertEquals(0.0f, result[46], 0.001f);
		assertEquals(0.0f, result[47], 0.001f);
		assertEquals(1.0f, result[48], 0.001f);
		assertEquals(1.0f, result[49], 0.001f);
	}

}
