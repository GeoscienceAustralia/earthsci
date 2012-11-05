package au.gov.ga.earthsci.worldwind.common.layers.model.gocad;

import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Test;

public class GocadColorTest
{
	@Test
	public void testColorStrings()
	{
		Color c = GocadColor.gocadLineToColor("*solid*color:1 0 0 0");
		assertEquals(255, c.getRed());
		assertEquals(0, c.getGreen());
		assertEquals(0, c.getBlue());
		assertEquals(0, c.getAlpha());

		c = GocadColor.gocadLineToColor("*solid*color:1 0 1");
		assertEquals(255, c.getRed());
		assertEquals(0, c.getGreen());
		assertEquals(255, c.getBlue());
		assertEquals(255, c.getAlpha());

		assertEquals(GocadColor.DarkOliveGreen.color, GocadColor.gocadLineToColor("*solid*color:dark olive green"));
		assertEquals(GocadColor.DarkOliveGreen.color, GocadColor.gocadLineToColor("*solid*color:darkolivegreen"));
	}
}
