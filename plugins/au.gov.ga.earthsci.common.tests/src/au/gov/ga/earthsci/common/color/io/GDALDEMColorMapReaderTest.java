package au.gov.ga.earthsci.common.color.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.junit.Test;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;

/**
 * Unit tests for the {@link GDALDEMColorMapReader} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class GDALDEMColorMapReaderTest
{

	private final GDALDEMColorMapReader classUnderTest = new GDALDEMColorMapReader();

	@Test
	public void testReadWithNull() throws Exception
	{
		Object source = null;

		ColorMap map = classUnderTest.read(source);

		assertNull(map);
	}

	@Test
	public void testReadWithValidRgbAbsoluteValues() throws Exception
	{
		ColorMap map = classUnderTest.read(open("colorMapValidRgbAbsolute.txt"));

		assertNotNull(map);
		assertEquals("Dummy map", map.getName());
		assertEquals("A dummy map", map.getDescription());
		assertFalse(map.isPercentageBased());
		assertEquals(InterpolationMode.INTERPOLATE_RGB, map.getMode());

		assertEquals(4, map.getSize());

		assertEquals(new Color(50, 50, 50, 255), map.getEntries().get(0.0));
		assertEquals(new Color(255, 0, 0, 255), map.getEntries().get(20.0));
		assertEquals(new Color(100, 100, 100, 255), map.getEntries().get(50.0));
		assertEquals(new Color(200, 200, 200, 255), map.getEntries().get(100.0));

		assertEquals(new Color(255, 0, 0, 255), map.getNodataColour());
	}

	@Test
	public void testReadWithValidExactPercentValues() throws Exception
	{
		ColorMap map = classUnderTest.read(open("colorMapValidExactPercentages.txt"));

		assertNotNull(map);
		assertEquals("Test map", map.getName());
		assertEquals("A test map", map.getDescription());
		assertTrue(map.isPercentageBased());
		assertEquals(InterpolationMode.EXACT_MATCH, map.getMode());

		assertEquals(4, map.getSize());

		assertEquals(new Color(50, 50, 50, 255), map.getEntries().get(0.0));
		assertEquals(new Color(255, 0, 0, 255), map.getEntries().get(0.2));
		assertEquals(new Color(100, 100, 100, 255), map.getEntries().get(0.5));
		assertEquals(new Color(200, 200, 200, 255), map.getEntries().get(1.0));

		assertEquals(null, map.getNodataColour());
	}

	@Test
	public void testReadWithValidHueMixedValues() throws Exception
	{
		ColorMap map = classUnderTest.read(open("colorMapValidHueMixed.txt"));

		assertNotNull(map);
		assertEquals("Hue map", map.getName());
		assertEquals(null, map.getDescription());
		assertFalse(map.isPercentageBased());
		assertEquals(InterpolationMode.INTERPOLATE_HUE, map.getMode());

		assertEquals(3, map.getSize());

		assertEquals(new Color(50, 50, 50, 255), map.getEntries().get(0.0));
		assertEquals(new Color(100, 100, 100, 255), map.getEntries().get(50.0));
		assertEquals(new Color(200, 200, 200, 255), map.getEntries().get(100.0));

		assertEquals(null, map.getNodataColour());
	}

	@Test
	public void testReadWithInvalidEntries() throws Exception
	{
		ColorMap map = classUnderTest.read(open("colorMapInvalidEntries.txt"));

		assertNotNull(map);
		assertNotNull(map.getName());
		assertEquals(null, map.getDescription());
		assertFalse(map.isPercentageBased());
		assertEquals(InterpolationMode.INTERPOLATE_RGB, map.getMode());

		assertEquals(0, map.getSize());
		assertEquals(null, map.getNodataColour());
	}

	private Object open(String testFile)
	{
		return getClass().getResourceAsStream(testFile);
	}

}
