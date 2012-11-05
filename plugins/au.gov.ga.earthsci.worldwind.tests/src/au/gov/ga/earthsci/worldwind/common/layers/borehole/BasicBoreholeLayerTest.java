package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.WWXML;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import au.gov.ga.earthsci.worldwind.common.layers.styled.Attribute;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StringWithPlaceholderGetter;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Unit tests for the {@link BasicBoreholeLayer} class
 */
public class BasicBoreholeLayerTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateBoreholeLayer()
	{
		Document doc = WWXML.openDocument(getClass().getResourceAsStream("boreholeLayer.xml"));
		BoreholeLayer boreholeLayer =
				BoreholeLayerFactory.createBoreholeLayer(doc.getDocumentElement(), new AVListImpl());

		AVList params = (AVList) boreholeLayer.getValue(AVKeyMore.CONSTRUCTION_PARAMETERS);
		assertNotNull(params);

		// Attributes should not be empty
		List<Attribute> attributes = (List<Attribute>) params.getValue(AVKeyMore.DATA_LAYER_ATTRIBUTES);
		assertNotNull(attributes);
		assertTrue(attributes.size() == 1);

		// Attributes should not be empty
		attributes = (List<Attribute>) params.getValue(AVKeyMore.BOREHOLE_SAMPLE_ATTRIBUTES);
		assertNotNull(attributes);
		assertTrue(attributes.size() == 2);

		assertEquals("NAME", attributes.get(1).getName());
		assertEquals("Name: %v%", StringWithPlaceholderGetter.getTextString(attributes.get(1)));
		assertEquals("%v%", StringWithPlaceholderGetter.getTextPlaceholder(attributes.get(1)));

		// Test regular parameters
		assertEquals("Borehole Layer", boreholeLayer.getName());
		assertEquals("GA/Boreholes.zip", boreholeLayer.getDataCacheName());
		assertEquals(Double.valueOf(1e4), boreholeLayer.getMinimumDistance());
		
		// Test adding a borehole sample
		AVList attributeValues = new AVListImpl();
		attributeValues.setValue("HOLE_ID", "id1");
		attributeValues.setValue("FROM", 50.0);
		attributeValues.setValue("TO", 60.0);
		boreholeLayer.addBoreholeSample(Position.ZERO, attributeValues);
		boreholeLayer.loadComplete();
		
		assertEquals(1, ((BasicBoreholeLayer)boreholeLayer).boreholes.size());
		assertEquals(1, ((BasicBoreholeLayer)boreholeLayer).boreholes.get(0).getSamples().size());
	}
}
