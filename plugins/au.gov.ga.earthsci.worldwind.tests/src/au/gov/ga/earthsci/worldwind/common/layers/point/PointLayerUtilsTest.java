package au.gov.ga.earthsci.worldwind.common.layers.point;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.WWXML;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import au.gov.ga.earthsci.worldwind.common.layers.styled.Attribute;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StringWithPlaceholderGetter;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Unit tests for the {@link PointLayerFactory} class
 */
public class PointLayerUtilsTest
{
	@Test
	public void testCreatePointLayerNoAttributes()
	{
		Document doc = WWXML.openDocument(getClass().getResourceAsStream("pointLayerNoAttributes.xml"));
		PointLayer pointLayer = PointLayerFactory.createPointLayer(doc.getDocumentElement(), new AVListImpl());
		
		AVList params = (AVList)pointLayer.getValue(AVKeyMore.CONSTRUCTION_PARAMETERS);
		assertNotNull(params);
		
		// Attributes should be empty
		@SuppressWarnings("unchecked")
		List<Attribute> attributes = (List<Attribute>)params.getValue(AVKeyMore.DATA_LAYER_ATTRIBUTES);
		assertNotNull(attributes);
		assertTrue(attributes.size() == 0);
		
		// Test regular parameters
		assertEquals("PointLayerNoAttributes", pointLayer.getName());
		assertEquals("GA/TEST/testpoints.zip", pointLayer.getDataCacheName());
	}
	
	@Test
	public void testCreatePointLayerWithAttributes()
	{
		Document doc = WWXML.openDocument(getClass().getResourceAsStream("pointLayerWithAttributes.xml"));
		PointLayer pointLayer = PointLayerFactory.createPointLayer(doc.getDocumentElement(), new AVListImpl());
		
		AVList params = (AVList)pointLayer.getValue(AVKeyMore.CONSTRUCTION_PARAMETERS);
		assertNotNull(params);
		
		// Attributes should not be empty
		@SuppressWarnings("unchecked")
		List<Attribute> attributes = (List<Attribute>)params.getValue(AVKeyMore.DATA_LAYER_ATTRIBUTES);
		assertNotNull(attributes);
		assertTrue(attributes.size() == 1);
		
		assertEquals("NAME", attributes.get(0).getName());
		assertEquals("Name: %v%", StringWithPlaceholderGetter.getTextString(attributes.get(0)));
		assertEquals("%v%", StringWithPlaceholderGetter.getTextPlaceholder(attributes.get(0)));
		
		// Test regular parameters
		assertEquals("PointLayerWithAttributes", pointLayer.getName());
		assertEquals("GA/TEST/testpoints.zip", pointLayer.getDataCacheName());
	}
}
