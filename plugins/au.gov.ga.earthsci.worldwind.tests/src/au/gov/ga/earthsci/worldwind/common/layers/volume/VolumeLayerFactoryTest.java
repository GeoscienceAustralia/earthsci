package au.gov.ga.earthsci.worldwind.common.layers.volume;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.awt.Color;
import java.net.URL;

import org.junit.Test;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

public class VolumeLayerFactoryTest
{
	@Test
	public void testCreateVolumeLayer()
	{
		URL url = this.getClass().getResource("dummyVolumeLayer.xml");
		Element element = XMLUtil.getElementFromSource(url);
		AVList params = new AVListImpl();
		VolumeLayer layer = VolumeLayerFactory.createVolumeLayer(element, params);
		
		assertNotNull(layer);
		assertNotNull(params.getValue(AVKeyMore.COLOR_MAP));
		
		assertNotNull(params.getValue(AVKeyMore.NO_DATA_COLOR));
		assertEquals(new Color(255,255,255,255), params.getValue(AVKeyMore.NO_DATA_COLOR));
		
		assertNotNull(params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_U));
		assertEquals(1, params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_U));
		
		assertNotNull(params.getValue(AVKeyMore.MAX_VARIANCE));
		assertEquals(1.0, params.getValue(AVKeyMore.MAX_VARIANCE));
		
		assertNotNull(params.getValue(AVKeyMore.MINIMUM_DISTANCE));
		assertEquals(1e5, (Double)params.getValue(AVKeyMore.MINIMUM_DISTANCE), 0.001);
		
		assertNotNull(params.getValue(AVKeyMore.DATA_LAYER_PROVIDER));
		assertTrue(params.getValue(AVKeyMore.DATA_LAYER_PROVIDER) instanceof ArrayVolumeDataProvider);
		
		assertNotNull(params.getValue(AVKeyMore.PAINTED_VARIABLE));
		assertEquals("var1", params.getValue(AVKeyMore.PAINTED_VARIABLE));
		assertEquals(layer.getPaintedVariableName(), "var1");
	}
}
