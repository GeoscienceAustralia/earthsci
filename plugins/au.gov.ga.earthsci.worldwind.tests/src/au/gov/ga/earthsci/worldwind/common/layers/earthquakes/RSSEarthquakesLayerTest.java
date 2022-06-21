package au.gov.ga.earthsci.worldwind.common.layers.earthquakes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gov.nasa.worldwind.util.WWXML;

import java.math.BigDecimal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.earthquakes.RSSEarthquakesLayer.Earthquake;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;
import au.gov.ga.earthsci.worldwind.test.util.TestUtils;

/**
 * Unit tests for the {@link RSSEarthquakesLayer} and it's inner classes
 */
public class RSSEarthquakesLayerTest
{
	@Test
	public void testNewEarthquakeWithNull()
	{
		try
		{
			new Earthquake(null);
			fail("Expected an illegal argument exception. Got none.");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}

	@Test
	/** Tests creating an earthquake from a single 'item' element of an RSS feed */
	public void testNewEarthquakeWithBasicCase()
	{
		Earthquake quake = new Earthquake(loadXmlElementFromResource("basicRssElementExample.xml"));

		assertNotNull(quake);
		assertEquals(new BigDecimal("5.0"), quake.magnitude);
		assertEquals("Santa Cruz Islands", quake.title);
		assertEquals("http://earthquakes.ga.gov.au/event/ga2018pnyqfi", quake.link);
		assertEquals(-80000, quake.position.elevation, 0.0001);
		assertEquals(-11.270, quake.position.latitude.degrees, 0.0001);
		assertEquals(166.173, quake.position.longitude.degrees, 0.0001);
		assertEquals("2018-08-09 01:26:31 +0000", TestUtils.formatDateInTimezone(quake.date, "UTC"));
	}

	@Test
	/** Tests creating an earthquake from a single 'item' element of an RSS feed, with a four-letter month date */
	public void testNewEarthquakeWithFourLetterMonthCase()
	{
		Earthquake quake = new Earthquake(loadXmlElementFromResource("juneRssElementExample.xml"));

		assertNotNull(quake);
		assertEquals(new BigDecimal("5.0"), quake.magnitude);
		assertEquals("Santa Cruz Islands", quake.title);
		assertEquals("http://earthquakes.ga.gov.au/event/ga2018pnyqfi", quake.link);
		assertEquals(-80000, quake.position.elevation, 0.0001);
		assertEquals(-11.270, quake.position.latitude.degrees, 0.0001);
		assertEquals(166.173, quake.position.longitude.degrees, 0.0001);
		assertEquals("2018-08-09 01:26:31 +0000", TestUtils.formatDateInTimezone(quake.date, "UTC"));
	}

	@Test
	/** Tests creating an earthquake from an 'item' element sourced from an RSS feed */
	public void testNewEarthquakeFromDummyFeed() throws Exception
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(this.getClass().getResourceAsStream("dummyRssFeedExample.xml"));

		Element[] items = WWXML.getElements(document.getDocumentElement(), "//item", null);

		Earthquake quake = new Earthquake(items[0]);
		assertEquals(new BigDecimal("5.0"), quake.magnitude);
		assertEquals("Santa Cruz Islands", quake.title);
		assertEquals("http://earthquakes.ga.gov.au/event/ga2018pnyqfi", quake.link);
		assertEquals(-80000, quake.position.elevation, 0.0001);
		assertEquals(-11.270, quake.position.latitude.degrees, 0.0001);
		assertEquals(166.173, quake.position.longitude.degrees, 0.0001);
		assertEquals("2018-08-09 01:26:31 +0000", TestUtils.formatDateInTimezone(quake.date, "UTC"));
	}

	private Element loadXmlElementFromResource(String resourceName)
	{
		Document doc = XMLUtil.openDocument(this.getClass().getResourceAsStream(resourceName));
		return doc.getDocumentElement();
	}
}
