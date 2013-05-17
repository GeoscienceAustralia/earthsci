package au.gov.ga.earthsci.common.color.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;

/**
 * Unit tests for the {@link CompactStringColorMapReader} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CompactStringColorMapReaderTest
{

	private static final String VALID_MIN = "name|||||0.0,-16777216";
	private static final String VALID_FULL = "name|description|NEAREST_MATCH|0|-1000|0.0,-16777216,1.0,-1";

	private final CompactStringColorMapReader classUnderTest = new CompactStringColorMapReader();

	@Test
	public void testSupportsWithNull()
	{
		assertFalse(classUnderTest.supports(null));
	}

	@Test
	public void testSupportsWithEmptyString()
	{
		assertFalse(classUnderTest.supports(""));
	}

	@Test
	public void testSupportsWithValidFullyConfigured()
	{
		assertTrue(classUnderTest.supports(VALID_FULL));
	}

	@Test
	public void testSupportsWithValidMinimalConfigured()
	{
		assertTrue(classUnderTest.supports(VALID_MIN));
	}

	@Test
	public void testSupportsWithInvalidMissingName()
	{
		assertFalse(classUnderTest.supports(VALID_FULL.replace("name", "")));
	}

	@Test
	public void testSupportsWithInvalidOddNumberKeyValuePairs()
	{
		assertFalse(classUnderTest.supports(VALID_FULL.replace("0.0,", "")));
	}

	@Test
	public void testSupportsWithInvalidBadColorValue()
	{
		assertFalse(classUnderTest.supports(VALID_FULL.replace("-1", "-1.0")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadWithNull() throws Exception
	{
		classUnderTest.read(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadWithEmpty() throws Exception
	{
		classUnderTest.read("");
	}

	@Test
	public void testReadWithValidFullyConfigured() throws Exception
	{
		ColorMap result = classUnderTest.read(VALID_FULL);

		assertNotNull(result);
		assertEquals("name", result.getName());
		assertEquals("description", result.getDescription());
		assertEquals(InterpolationMode.NEAREST_MATCH, result.getMode());
		assertFalse(result.isPercentageBased());
		assertEquals(-1000, result.getNodataColour().getRGB());

		assertEquals(2, result.getEntries().size());
		assertEquals(-16777216, result.getColor(0.0).getRGB());
		assertEquals(-1, result.getColor(1.0).getRGB());
	}

	@Test
	public void testReadWithValidMinimalConfigured() throws Exception
	{
		ColorMap result = classUnderTest.read(VALID_MIN);

		assertNotNull(result);
		assertEquals("name", result.getName());
		assertEquals(null, result.getDescription());
		assertEquals(InterpolationMode.INTERPOLATE_RGB, result.getMode());
		assertTrue(result.isPercentageBased());
		assertEquals(null, result.getNodataColour());

		assertEquals(1, result.getEntries().size());
		assertEquals(-16777216, result.getColor(0.0).getRGB());
		assertEquals(-16777216, result.getColor(1.0).getRGB());
	}
}
