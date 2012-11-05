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
		assertEquals(new BigDecimal("5.3"), quake.magnitude);
		assertEquals("Vanuatu Islands", quake.title);
		assertEquals("http://www.ga.gov.au/earthquakes/getQuakeDetails.do?quakeId=3038401&orid=417060&sta=NFK", quake.link);
		assertEquals(-78000, quake.position.elevation, 0.0001);
		assertEquals(-20.218, quake.position.latitude.degrees, 0.0001);
		assertEquals(168.33, quake.position.longitude.degrees, 0.0001);
		assertEquals("2011-05-11 09:16:30 +0000", TestUtils.formatDateInTimezone(quake.date, "UTC"));
	}
	
	@Test
	/** Tests creating an earthquake from a single 'item' element of an RSS feed, with a four-letter month date */
	public void testNewEarthquakeWithFourLetterMonthCase()
	{
		Earthquake quake = new Earthquake(loadXmlElementFromResource("juneRssElementExample.xml"));
		
		assertNotNull(quake);
		assertEquals(new BigDecimal("5.0"), quake.magnitude);
		assertEquals("Southern Sumatra, Indonesia, Sunda Arc", quake.title);
		assertEquals("http://www.ga.gov.au/earthquakes/getQuakeDetails.do?quakeId=3063176&orid=430273&sta=XMIS", quake.link);
		assertEquals(-200000, quake.position.elevation, 0.0001);
		assertEquals(-1.911, quake.position.latitude.degrees, 0.0001);
		assertEquals(101.989, quake.position.longitude.degrees, 0.0001);
		assertEquals("2011-06-15 03:06:45 +0000", TestUtils.formatDateInTimezone(quake.date, "UTC"));
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
		assertEquals(new BigDecimal("5.3"), quake.magnitude);
		assertEquals("Loyalty Islands, Indonesia", quake.title);
		assertEquals("http://www.ga.gov.au/earthquakes/getQuakeDetails.do?quakeId=3044320&orid=419517&sta=EIDS", quake.link);
		assertEquals(-60000, quake.position.elevation, 0.0001);
		assertEquals(-20.491, quake.position.latitude.degrees, 0.0001);
		assertEquals(168.402, quake.position.longitude.degrees, 0.0001);
		assertEquals("2011-05-16 19:18:28 +0000", TestUtils.formatDateInTimezone(quake.date, "UTC"));
	}
	
	private Element loadXmlElementFromResource(String resourceName)
	{
		Document doc = XMLUtil.openDocument(this.getClass().getResourceAsStream(resourceName));
		return doc.getDocumentElement();
	}
}
