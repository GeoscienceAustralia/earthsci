package au.gov.ga.earthsci.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

/**
 * Unit tests for the {@link URIBuilder} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class URIBuilderTest
{

	@Test
	public void testBuildFullValidFromEmpty() throws Exception
	{
		URIBuilder classUnderTest = new URIBuilder();

		classUnderTest.setScheme("scheme")
				.setUserInfo("user:pass")
				.setHost("host.com")
				.setPort(1000)
				.setPath("/test/path/item.foo")
				.setParam("param1", "value1")
				.setParam("param2", null)
				.setFragment("fragment");

		URI result = classUnderTest.build();

		assertNotNull(result);

		assertEquals("scheme", result.getScheme());
		assertEquals("user:pass", result.getUserInfo());
		assertEquals("host.com", result.getHost());
		assertEquals(1000, result.getPort());
		assertEquals("/test/path/item.foo", result.getPath());

		assertTrue(result.getRawQuery().split("&").length == 2);
		assertTrue(result.getRawQuery().contains("param1=value1"));
		assertTrue(result.getRawQuery().contains("param2"));

		assertEquals("fragment", result.getFragment());

		assertEquals("scheme://user:pass@host.com:1000/test/path/item.foo?param1=value1&param2#fragment",
				result.toString());
	}

	@Test
	public void testBuildNoPath() throws Exception
	{
		URIBuilder classUnderTest = new URIBuilder();

		classUnderTest.setScheme("scheme")
				.setUserInfo("user:pass")
				.setHost("host.com")
				.setPort(1000)
				.setParam("param1", "value1")
				.setParam("param2", null)
				.setFragment("fragment");

		URI result = classUnderTest.build();
		assertNotNull(result);

		assertEquals("scheme://user:pass@host.com:1000?param1=value1&param2#fragment", result.toString());
	}

	@Test
	public void testBuildNoAuthority() throws Exception
	{
		URIBuilder classUnderTest = new URIBuilder();

		classUnderTest.setScheme("scheme")
				.setPath("/test/path/item.foo")
				.setParam("param1", "value1")
				.setParam("param2", null)
				.setFragment("fragment");

		URI result = classUnderTest.build();
		assertNotNull(result);

		assertEquals("scheme:/test/path/item.foo?param1=value1&param2#fragment", result.toString());
	}

	@Test
	public void testBuildWithExistingAndChanges() throws Exception
	{
		URI existing = new URI("scheme://user:pass@host.com:1000/test/path/item.foo?param1=value1&param2#fragment");

		URIBuilder classUnderTest = new URIBuilder(existing);

		classUnderTest.setFragment("newFragment");
		classUnderTest.setUserInfo(null);

		URI result = classUnderTest.build();

		assertNotNull(result);

		assertEquals("scheme://host.com:1000/test/path/item.foo?param1=value1&param2#newFragment", result.toString());
	}

	@Test
	public void testBuildWithExistingNoChanges() throws Exception
	{
		URI existing = new URI("scheme://user:pass@host.com:1000/test/path/item.foo?param1=value1&param2#fragment");

		URIBuilder classUnderTest = new URIBuilder(existing);
		URI result = classUnderTest.build();

		assertNotNull(result);

		assertEquals(existing.toString(), result.toString());
	}
}
