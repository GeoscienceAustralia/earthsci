package au.gov.ga.earthsci.common.color.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;
import au.gov.ga.earthsci.common.color.ColorMapBuilder;

/**
 * Unit tests for the {@link CompactStringColorMapWriter} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class CompactStringColorMapWriterTest
{

	private final CompactStringColorMapWriter classUnderTest = new CompactStringColorMapWriter();
	private ByteArrayOutputStream out;

	@Before
	public void setup()
	{
		out = new ByteArrayOutputStream();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWriteWithNullMap() throws Exception
	{
		classUnderTest.write(null, out);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWriteWithNullStream() throws Exception
	{
		ColorMap map = ColorMapBuilder.createColorMap().named("dummy").build();

		classUnderTest.write(map, null);
	}

	@Test
	public void testWriteFullyConfigured() throws Exception
	{
		ColorMap map = ColorMapBuilder.createColorMap()
				.named("test")
				.describedAs("Test map")
				.using(InterpolationMode.INTERPOLATE_HUE)
				.withPercentageValues()
				.withEntry(0.0, Color.BLACK)
				.withEntry(1.0, Color.WHITE)
				.withNodata(Color.RED)
				.build();

		classUnderTest.write(map, out);

		String expected = "test|Test map|INTERPOLATE_HUE|1|" + Color.RED.getRGB()
				+ "|0.0," + Color.BLACK.getRGB() + ",1.0," + Color.WHITE.getRGB();

		assertEquals(expected, out.toString());
	}

	@Test
	public void testWriteMinimumConfigured() throws Exception
	{
		ColorMap map = ColorMapBuilder.createColorMap()
				.using(InterpolationMode.NEAREST_MATCH)
				.withEntry(0.0, Color.BLACK)
				.withEntry(1.0, Color.WHITE)
				.build();

		classUnderTest.write(map, out);

		String expected = map.getName() + "||NEAREST_MATCH|0||0.0," + Color.BLACK.getRGB()
				+ ",1.0," + Color.WHITE.getRGB();

		assertEquals(expected, out.toString());
	}

	@Test
	public void testWriteToStringWithNull() throws Exception
	{
		assertNull(classUnderTest.writeToString(null));
	}

	@Test
	public void testWriteToStringFullyConfigured() throws Exception
	{
		ColorMap map = ColorMapBuilder.createColorMap()
				.named("test")
				.describedAs("Test map")
				.using(InterpolationMode.INTERPOLATE_HUE)
				.withPercentageValues()
				.withEntry(0.0, Color.BLACK)
				.withEntry(1.0, Color.WHITE)
				.withNodata(Color.RED)
				.build();

		String result = classUnderTest.writeToString(map);
		String expected = "test|Test map|INTERPOLATE_HUE|1|" + Color.RED.getRGB()
				+ "|0.0," + Color.BLACK.getRGB() + ",1.0," + Color.WHITE.getRGB();

		assertEquals(expected, result);
	}
}
