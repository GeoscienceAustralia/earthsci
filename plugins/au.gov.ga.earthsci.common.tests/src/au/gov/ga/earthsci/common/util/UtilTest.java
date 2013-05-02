package au.gov.ga.earthsci.common.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for the {@link Util} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class UtilTest
{

	@Test
	public void testRemoveWhitespaceNull()
	{
		String input = null;
		String expected = null;
		
		assertEquals(expected, Util.removeWhitespace(input));
	}
	
	@Test
	public void testRemoveWhitespaceAllWhitespace()
	{
		String input = "  \t\t \t";
		String expected = "";
		
		assertEquals(expected, Util.removeWhitespace(input));
	}
	
	@Test
	public void testRemoveWhitespaceMixed()
	{
		String input = "1 \t2\t3  45";
		String expected = "12345";
		
		assertEquals(expected, Util.removeWhitespace(input));
	}
	
}
