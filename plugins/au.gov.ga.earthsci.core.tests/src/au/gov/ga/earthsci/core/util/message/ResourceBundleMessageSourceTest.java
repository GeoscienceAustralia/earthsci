package au.gov.ga.earthsci.core.util.message;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the {@link ResourceBundleMessageSource} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class ResourceBundleMessageSourceTest
{

	private ResourceBundleMessageSource classToBeTested = new ResourceBundleMessageSource(
			"au.gov.ga.earthsci.core.util.message.testMessages1", "au.gov.ga.earthsci.core.util.message.testMessages2");

	/**
	 * Tests the
	 * {@link ResourceBundleMessageSource#getMessage(String, String, Object...)}
	 * method with a key that exists only in the first bundle.
	 */
	@Test
	public void testGetMessageKeyInBundle1()
	{
		String message = classToBeTested.getMessage("key0");

		assertEquals("Key0Bundle1", message);
	}

	/**
	 * Tests the
	 * {@link ResourceBundleMessageSource#getMessage(String, String, Object...)}
	 * method with a key that exists only in the second bundle.
	 */
	@Test
	public void testGetMessageKeyInBundle2()
	{
		String message = classToBeTested.getMessage("key3");

		assertEquals("Key3Bundle2", message);
	}

	/**
	 * Tests the
	 * {@link ResourceBundleMessageSource#getMessage(String, String, Object...)}
	 * method with a key that exists in both bundles.
	 */
	@Test
	public void testGetMessageKeyInBothBundles()
	{
		String message = classToBeTested.getMessage("key1");

		assertEquals("Key1Bundle1", message);
	}

	/**
	 * Tests the
	 * {@link ResourceBundleMessageSource#getMessage(String, String, Object...)}
	 * method with a key that exists in no bundles.
	 */
	@Test
	public void testGetMessageKeyInNoBundles()
	{
		String message = classToBeTested.getMessage("keyNone");

		assertEquals(null, message);
	}

	/**
	 * Tests the
	 * {@link ResourceBundleMessageSource#getMessage(String, String, Object...)}
	 * method with a key that exists in no bundles and a default message
	 */
	@Test
	public void testGetMessageKeyInNoBundlesWithDefault()
	{
		String message = classToBeTested.getMessage("keyNone", "default");

		assertEquals("default", message);
	}

	/**
	 * Tests the
	 * {@link ResourceBundleMessageSource#getMessage(String, String, Object...)}
	 * method with a key that resolves a message with parameters, but not
	 * arguments passed
	 */
	@Test
	public void testGetMessageKeyInBundleWithParamsNoArgs()
	{
		String message = classToBeTested.getMessage("key.with.params", null, new Object[0]);

		assertEquals("This {0} message {1} has {2} 3 params", message);
	}

	/**
	 * Tests the
	 * {@link ResourceBundleMessageSource#getMessage(String, String, Object...)}
	 * method with a key that resolves a message with parameters, with arguments
	 * passed
	 */
	@Test
	public void testGetMessageKeyInBundleWithParamsWithArgs()
	{
		String message = classToBeTested.getMessage("key.with.params", null, new Object[] { "bob", 1, true });

		assertEquals("This bob message 1 has true 3 params", message);
	}


}
