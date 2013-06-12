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
		assertTrue(classUnderTest.isEmpty());
		assertEquals(0, classUnderTest.getSize());
	}

	@Test
	public void testFullConstructor()
	{
		ColorMap classUnderTest =
				new ColorMap(null, null, null, new Color(1, 1, 1, 1), InterpolationMode.EXACT_MATCH, true);

		assertEquals(InterpolationMode.EXACT_MATCH, classUnderTest.getMode());
		assertColorsEqual(new Color(1, 1, 1, 1), classUnderTest.getNodataColour());
		assertTrue(classUnderTest.isPercentageBased());
		assertTrue(classUnderTest.getEntries().isEmpty());
		assertTrue(classUnderTest.isEmpty());
		assertEquals(0, classUnderTest.getSize());
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
		ColorMap classUnderTest =
				new ColorMap(null, null, PERCENTAGE_ENTRIES, null, InterpolationMode.EXACT_MATCH, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(0.0));
		assertColorsEqual(null, classUnderTest.getColor(0.5));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorNearestMatchPercentages()
	{
		ColorMap classUnderTest =
				new ColorMap(null, null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(0.0));
		assertColorsEqual(new Color(0.0f, 0.0f, 1.0f, 0.0f), classUnderTest.getColor(0.5));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorRGBInterpolationPercentages()
	{
		ColorMap classUnderTest =
				new ColorMap(null, null, PERCENTAGE_ENTRIES, null, InterpolationMode.INTERPOLATE_RGB, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(0.0));
		assertColorsEqual(new Color(0.0f, 0.333f, 0.666f, 0.0f), classUnderTest.getColor(0.5));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorHueInterpolationPercentages()
	{
		ColorMap classUnderTest =
				new ColorMap(null, null, PERCENTAGE_ENTRIES, null, InterpolationMode.INTERPOLATE_HUE, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(0.0));
		assertColorsEqual(new Color(0.0f, 0.666f, 1.0f, 0.0f), classUnderTest.getColor(0.5));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(1.0));
	}

	@Test
	public void testGetColorFromAbsoluteWithPercentages()
	{
		ColorMap classUnderTest =
				new ColorMap(null, null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, true);

		assertColorsEqual(new Color(1.0f, 0.0f, 0.0f, 0.0f), classUnderTest.getColor(10, 10, 100));
		assertColorsEqual(new Color(0.0f, 0.0f, 1.0f, 0.0f), classUnderTest.getColor(55, 10, 100));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(100, 10, 100));
	}

	@Test
	public void testGetColorFromAbsoluteWithNonPercentages()
	{
		ColorMap classUnderTest =
				new ColorMap(null, null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);

		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(10, 10, 100));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(55, 10, 100));
		assertColorsEqual(new Color(0.0f, 0.0f, 0.0f, 1.0f), classUnderTest.getColor(100, 10, 100));
	}

	@Test
	public void testEqualsWithSame()
	{
		ColorMap map1 = new ColorMap(null, null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);
		ColorMap map2 = map1;

		assertTrue(map1.equals(map2));
	}

	@Test
	public void testEqualsWithNull()
	{
		ColorMap map1 = new ColorMap(null, null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);
		ColorMap map2 = null;

		assertFalse(map1.equals(map2));
	}

	@Test
	public void testEqualsWithEqual()
	{
		ColorMap map1 = new ColorMap("name", null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);
		ColorMap map2 = new ColorMap("name", null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);

		assertTrue(map1.equals(map2));
	}

	@Test
	public void testEqualsWithNotEqual()
	{
		ColorMap map1 = new ColorMap("name", null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);
		ColorMap map2 = new ColorMap("name", null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, true);

		assertFalse(map1.equals(map2));
	}

	@Test
	public void testHashCodeWithEqual()
	{
		ColorMap map1 = new ColorMap("name", null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);
		ColorMap map2 = new ColorMap("name", null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);

		assertTrue(map1.hashCode() == map2.hashCode());
	}

	@Test
	public void testHashCodeWithNotEqual()
	{
		ColorMap map1 = new ColorMap("name", null, PERCENTAGE_ENTRIES, null, InterpolationMode.NEAREST_MATCH, false);
		ColorMap map2 = new ColorMap("name", null, PERCENTAGE_ENTRIES, null, InterpolationMode.EXACT_MATCH, false);

		assertTrue(map1.hashCode() != map2.hashCode());
	}

	@Test
	public void testGetNextEntry()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES);

		assertEquals(0.0, classUnderTest.getNextEntry(-1.0).getKey(), 0.001);
		assertEquals(0.1, classUnderTest.getNextEntry(0.0).getKey(), 0.001);
		assertEquals(0.7, classUnderTest.getNextEntry(0.1).getKey(), 0.001);
		assertEquals(1.0, classUnderTest.getNextEntry(0.7).getKey(), 0.001);
		assertEquals(null, classUnderTest.getNextEntry(1.0));
		assertEquals(null, classUnderTest.getNextEntry(null));
	}

	@Test
	public void testGetPreviousEntry()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES);

		assertEquals(null, classUnderTest.getPreviousEntry(-1.0));
		assertEquals(null, classUnderTest.getPreviousEntry(0.0));
		assertEquals(0.0, classUnderTest.getPreviousEntry(0.1).getKey(), 0.001);
		assertEquals(0.1, classUnderTest.getPreviousEntry(0.7).getKey(), 0.001);
		assertEquals(0.7, classUnderTest.getPreviousEntry(1.0).getKey(), 0.001);
		assertEquals(1.0, classUnderTest.getPreviousEntry(1.1).getKey(), 0.001);
		assertEquals(null, classUnderTest.getPreviousEntry(null));
	}

	@Test
	public void testGetFirstWithEntries()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES);

		assertEquals(0.0, classUnderTest.getFirstEntry().getKey(), 0.001);
	}

	@Test
	public void testGetFirstWithNoEntries()
	{
		ColorMap classUnderTest = new ColorMap(null);

		assertEquals(null, classUnderTest.getFirstEntry());
	}

	@Test
	public void testGetLastWithEntries()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES);

		assertEquals(1.0, classUnderTest.getLastEntry().getKey(), 0.001);
	}

	@Test
	public void testGetLastWithNoEntries()
	{
		ColorMap classUnderTest = new ColorMap(null);

		assertEquals(null, classUnderTest.getLastEntry());
	}

	@Test
	public void testGetEntryWithValid()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES);

		assertEquals(0.7, classUnderTest.getEntry(0.7).getKey(), 0.001);
	}

	@Test
	public void testGetEntryWithInvalid()
	{
		ColorMap classUnderTest = new ColorMap(PERCENTAGE_ENTRIES);

		assertEquals(null, classUnderTest.getEntry(0.5));
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
