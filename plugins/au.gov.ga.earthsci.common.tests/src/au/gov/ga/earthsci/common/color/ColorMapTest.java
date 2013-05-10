package au.gov.ga.earthsci.common.color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;

/**
 * Unit tests for the {@link ColorMap} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class ColorMapTest
{

	@SuppressWarnings("serial")
	private static final Map<Double, Color> PERCENTAGE_ENTRIES = new HashMap<Double, Color>()
	{
		{
			put(0.0, new Color(1.0f, 0.0f, 0.0f, 0.0f));
			put(0.1, new Color(0.0f, 1.0f, 0.0f, 0.0f));
			put(0.7, new Color(0.0f, 0.0f, 1.0f, 0.0f));
			put(1.0, new Color(0.0f, 0.0f, 0.0f, 1.0f));
		}
	};

	@Test
	public void testMinimalConstructor()
	{
		ColorMap classUnderTest = new ColorMap(null);

		assertEquals(InterpolationMode.INTERPOLATE_RGB, classUnderTest.getMode());
		assertColorsEqual(new Color(0, 0, 0, 0), classUnderTest.getNodataColour());
		assertFalse(classUnderTest.isPercentageBased());
	}

	@Test
	public void testFullConstructor()
	{
		ColorMap classUnderTest = new ColorMap(null, new Color(1, 1, 1, 1), InterpolationMode.EXACT_MATCH, true);

		assertEquals(InterpolationMode.EXACT_MATCH, classUnderTest.getMode());
		assertColorsEqual(new Color(1, 1, 1, 1), classUnderTest.getNodataColour());
		assertTrue(classUnderTest.isPercentageBased());
		assertTrue(classUnderTest.getEntries().isEmpty());
	}

	@Test
	public void testGetColorNullEntries()
	{
		ColorMap classUnderTest = new ColorMap(null);

		assertColorsEqual(classUnderTest.getNodataColour(), classUnderTest.getColor(0.0));
		assertColorsEqual(classUnderTest.getNodataColour(), classUnderTest.getColor(0.5));
		assertColorsEqual(classUnderTest.getNodataColour(), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorExactMatchPercentages()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES, null, InterpolationMode.EXACT_MATCH, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(0.0));
		assertColorsEqual(null, classUnderTest.getColor(0.5));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorNearestMatchPercentages()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(0.0));
		assertColorsEqual(new Color(0.0f, 0.0f, 1.0f, 0.0f), classUnderTest.getColor(0.5));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorRGBInterpolationPercentages()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES, null, InterpolationMode.INTERPOLATE_RGB, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(0.0));
		assertColorsEqual(new Color(0.0f, 0.333f, 0.666f, 0.0f), classUnderTest.getColor(0.5));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorHueInterpolationPercentages()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES, null, InterpolationMode.INTERPOLATE_HUE, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(0.0));
		assertColorsEqual(new Color(0.0f, 0.666f, 1.0f, 0.0f), classUnderTest.getColor(0.5));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorFromAbsoluteWithPercentages()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(10, 10, 100));
		assertColorsEqual(new Color(0.0f, 0.0f, 1.0f, 0.0f), classUnderTest.getColor(55, 10, 100));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(100, 10, 100));
	}

	@Test
	public void testGetColorFromAbsoluteWithNonPercentages()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);

		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(10, 10, 100));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(55, 10, 100));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(100, 10, 100));
	}

	private static void assertColorsEqual(Color expected, Color actual)
	{
		if (expected == null)
		{
			assertNull(actual);
			return;
		}
		assertNotNull(actual);

		float[] expectedRGBA = expected.getRGBComponents(null);
		float[] actualRGBA = actual.getRGBComponents(null);

		assertEquals(expectedRGBA[0], actualRGBA[0], 0.001);
		assertEquals(expectedRGBA[1], actualRGBA[1], 0.001);
		assertEquals(expectedRGBA[2], actualRGBA[2], 0.001);
		assertEquals(expectedRGBA[3], actualRGBA[3], 0.001);
	}
}
