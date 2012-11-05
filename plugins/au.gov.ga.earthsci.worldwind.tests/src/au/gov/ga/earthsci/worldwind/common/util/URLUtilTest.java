package au.gov.ga.earthsci.worldwind.common.util;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Test;

/**
 * Unit tests for the {@link URLUtil} class
 */
public class URLUtilTest
{

	// stripQuery()

	@Test
	public void testStripQueryWithNull()
	{
		URL url = null;

		URL result = URLUtil.stripQuery(url);

		assertNull(result);
	}

	@Test
	public void testStripQueryWithNoQuery() throws Exception
	{
		URL url = new URL("http://www.some.url.com/without/query.html");

		URL result = URLUtil.stripQuery(url);

		assertNotNull(result);
		assertEquals("http://www.some.url.com/without/query.html", result.toExternalForm());
	}

	@Test
	public void testStripQueryWithQuery() throws Exception
	{
		URL url = new URL("http://www.some.url.com/with/query.html?param1=value1&param2=value2");

		URL result = URLUtil.stripQuery(url);

		assertNotNull(result);
		assertEquals("http://www.some.url.com/with/query.html", result.toExternalForm());
	}

	
	// urlToFile()
	@Test
	public void testUrlToFileWithNull()
	{
		File result = URLUtil.urlToFile(null);
		
		assertEquals(null, result);
	}
	
	@Test
	public void testUrlToFileWithFileUrl() throws Exception
	{
		File result = URLUtil.urlToFile(new URL("file://c:/this/is/a/file.extension"));
		
		assertEquals("C:\\this\\is\\a\\file.extension", result.getAbsolutePath());
	}
	
	@Test
	public void testUrlToFileWithHttpUrl() throws Exception
	{
		File result = URLUtil.urlToFile(new URL("http://this/is/not/a/file.html"));
		
		assertEquals(null, result);
	}
	
	// isForResourceWithExtension()
	@Test
	public void testIsForResourceWithExtensionWithNullUrl() throws Exception
	{
		assertFalse(URLUtil.isForResourceWithExtension(null, "zip"));
	}
	
	@Test
	public void testIsForResourceWithExtensionWithNullExtension() throws Exception
	{
		assertFalse(URLUtil.isForResourceWithExtension(new URL("http://somewhere.com/file.zip"), null));
	}
	
	@Test
	public void testIsForResourceWithExtensionWithBlankExtension() throws Exception
	{
		assertFalse(URLUtil.isForResourceWithExtension(new URL("http://somewhere.com/file.zip"), "   "));
	}
	
	@Test
	public void testIsForResourceWithExtensionWithMatchingExtension() throws Exception
	{
		assertTrue(URLUtil.isForResourceWithExtension(new URL("http://somewhere.com/file.zip"), "zip"));
	}
	
	@Test
	public void testIsForResourceWithExtensionWithNonMatchingExtension() throws Exception
	{
		assertFalse(URLUtil.isForResourceWithExtension(new URL("http://somewhere.com/file.zip"), "zipp"));
	}
	
	@Test
	public void testIsForResourceWithExtensionWithNonQueryParamsAndMatchingExtension() throws Exception
	{
		assertTrue(URLUtil.isForResourceWithExtension(new URL("http://somewhere.com/file.zip?param1=something&"), "zip"));
	}
	
	// fromObject()
	
	@Test
	public void testFromObjectWithNull() throws Exception
	{
		assertEquals(null, URLUtil.fromObject(null));
	}
	
	@Test
	public void testFromObjectWithValidURLString() throws Exception
	{
		assertEquals(new URL("http://somewhere.com/something.html"), URLUtil.fromObject("http://somewhere.com/something.html"));
	}
	
	@Test
	public void testFromObjectWithInvalidURLString() throws Exception
	{
		assertEquals(null, URLUtil.fromObject("httpp://somewhere.com/something.html"));
	}
	
	@Test
	public void testFromObjectWithURL() throws Exception
	{
		assertEquals(new URL("http://somewhere.com/something.html"), URLUtil.fromObject(new URL("http://somewhere.com/something.html")));
	}
	
	@Test
	public void testFromObjectWithFile() throws Exception
	{
		assertEquals(new URL("file:/C:/mypath/myfile.txt"), URLUtil.fromObject(new File("C:/mypath/myfile.txt")));
	}
	
	@Test
	public void testFromObjectWithUnsupportedObject() throws Exception
	{
		assertEquals(null, URLUtil.fromObject(new Integer(2)));
	}
}
