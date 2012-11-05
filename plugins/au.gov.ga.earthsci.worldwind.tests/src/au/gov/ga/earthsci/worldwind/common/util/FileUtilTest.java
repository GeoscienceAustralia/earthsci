package au.gov.ga.earthsci.worldwind.common.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for the {@link FileUtil} class
 */
public class FileUtilTest
{
	
	@Test
	public void testGetExtensionNull()
	{
		assertEquals(null, FileUtil.getExtension(null));
	}
	
	@Test
	public void testGetExtensionEmpty()
	{
		assertEquals(null, FileUtil.getExtension(""));
	}
	
	@Test
	public void testGetExtensionBlank()
	{
		assertEquals(null, FileUtil.getExtension("   "));
	}
	
	@Test
	public void testGetExtensionNoExtension()
	{
		assertEquals(null, FileUtil.getExtension("some/file/without/an/extension"));
	}
	
	@Test
	public void testGetExtensionWithExtension()
	{
		assertEquals("ext", FileUtil.getExtension("some/file/with/an/extension.ext"));
	}
	
	@Test
	public void testGetExtensionWithMultiplePeriod()
	{
		assertEquals("ext2", FileUtil.getExtension("some/file/with/an/extension.ext1.ext2"));
	}
	
	@Test
	public void testHasExtensionNullFilename()
	{
		assertEquals(false, FileUtil.hasExtension(null, "ext"));
	}
	
	@Test
	public void testHasExtensionNullExtension()
	{
		assertEquals(false, FileUtil.hasExtension("some/file/with/an/extension.ext", null));
	}
	
	@Test
	public void testHasExtensionEmptyFilename()
	{
		assertEquals(false, FileUtil.hasExtension("", "ext"));
	}
	
	@Test
	public void testHasExtensionEmptyExtension()
	{
		assertEquals(false, FileUtil.hasExtension("some/file/with/an/extension.ext", ""));
	}
	
	@Test
	public void testHasExtensionNonMatchingExtension()
	{
		assertEquals(false, FileUtil.hasExtension("some/file/with/an/extension.ext1", "ext2"));
	}
	
	@Test
	public void testHasExtensionMatchingExtensionSameCase()
	{
		assertEquals(true, FileUtil.hasExtension("some/file/with/an/extension.ext1", "ext1"));
	}
	
	@Test
	public void testHasExtensionMatchingExtensionMixedCase()
	{
		assertEquals(true, FileUtil.hasExtension("some/file/with/an/extension.exT1", "eXt1"));
	}
	
	@Test
	public void testHasExtensionMatchingExtensionWithPeriod()
	{
		assertEquals(true, FileUtil.hasExtension("some/file/with/an/extension.exT1.Ext2", ".eXt2"));
	}
}
