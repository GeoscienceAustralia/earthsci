package au.gov.ga.earthsci.worldwind.common.layers.model;

import static org.junit.Assert.*;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.net.URL;

import org.junit.Test;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

public class ModelLayerFactoryTest
{
	@Test
	public void testCreateModelLayer()
	{
		URL url = this.getClass().getResource("dummyModelLayer.xml");
		Element element = XMLUtil.getElementFromSource(url);
		AVList params = new AVListImpl();
		ModelLayer layer = ModelLayerFactory.createModelLayer(element, params);
		assertNotNull(layer);
		assertNotNull(params.getValue(AVKeyMore.COLOR_MAP));
		assertNotNull(params.getValue(AVKey.BYTE_ORDER));
	}
}
