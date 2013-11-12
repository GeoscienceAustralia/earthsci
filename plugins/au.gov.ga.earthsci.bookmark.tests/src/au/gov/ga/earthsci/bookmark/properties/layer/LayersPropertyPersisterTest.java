/*******************************************************************************
 * Copyright 2013 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.bookmark.properties.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.util.WWXML;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.util.XmlUtil;

/**
 * Unit tests for the {@link LayersPropertyPersister}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayersPropertyPersisterTest
{

	private LayersPropertyPersister classUnderTest;

	@Before
	public void setup()
	{
		classUnderTest = new LayersPropertyPersister();
	}

	@Test
	public void testExportToXmlNullProperty() throws Exception
	{
		LayersProperty property = null;
		Element parent = createPropertyElement();

		classUnderTest.exportToXML(property, parent);

		assertEquals(0, parent.getChildNodes().getLength());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExportToXmlNullElement() throws Exception
	{
		LayersProperty property = createLayersProperty(new String[0], new Double[0]);
		Element parent = null;

		classUnderTest.exportToXML(property, parent);
	}

	@Test
	public void testExportToXmlEmptyProperty() throws Exception
	{
		LayersProperty property = createLayersProperty(new String[0], new Double[0]);
		Element parent = createPropertyElement();

		classUnderTest.exportToXML(property, parent);

		assertEquals(1, XmlUtil.getElements(parent, "layerState", null).length);
		assertEquals(0, XmlUtil.getElements(parent, "layerState/layer", null).length);
	}

	@Test
	public void testExportToXmlSingleLayer() throws Exception
	{
		LayersProperty property =
				createLayersProperty(new String[] { "id1" }, new Double[] { 0.5d });
		Element parent = createPropertyElement();

		classUnderTest.exportToXML(property, parent);

		assertEquals(1, XmlUtil.getElements(parent, "layerState", null).length);
		assertEquals(1, XmlUtil.getElements(parent, "layerState/layer", null).length);

		assertEquals("id1", XmlUtil.getText(parent, "layerState/layer[1]/@id"));
		assertEquals(0.5, XmlUtil.getDouble(parent, "layerState/layer[1]/@opacity", -1), 0.001);
	}

	@Test
	public void testExportToXmlMultipleLayers() throws Exception
	{
		LayersProperty property =
				createLayersProperty(new String[] { "id1", "id2" }, new Double[] { 0.5d, 0.8d });
		Element parent = createPropertyElement();

		classUnderTest.exportToXML(property, parent);

		assertEquals(1, XmlUtil.getElements(parent, "layerState", null).length);
		assertEquals(2, XmlUtil.getElements(parent, "layerState/layer", null).length);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateFromXmlNullElement() throws Exception
	{
		Element parent = null;

		classUnderTest.createFromXML(LayersProperty.TYPE, parent);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateFromXmlNullType() throws Exception
	{
		Element parent = createLayersPropertyElement(new String[0], new Double[0]);

		classUnderTest.createFromXML(null, parent);
	}

	@Test
	public void testCreateFromXmlEmptyProperty() throws Exception
	{
		Element parent = createLayersPropertyElement(new String[0], new Double[0]);

		IBookmarkProperty result = classUnderTest.createFromXML(LayersProperty.TYPE, parent);

		assertNotNull(result);
		assertTrue(result instanceof LayersProperty);
		assertTrue(((LayersProperty) result).getLayerState().isEmpty());
	}

	@Test
	public void testCreateFromXmlNonEmptyProperty() throws Exception
	{
		Element parent =
				createLayersPropertyElement(new String[] { "id1", "id2" }, new Double[] { 0.5, 0.8 });

		IBookmarkProperty result = classUnderTest.createFromXML(LayersProperty.TYPE, parent);

		assertNotNull(result);
		assertTrue(result instanceof LayersProperty);

		LayersProperty layersProperty = (LayersProperty) result;
		assertEquals(2, layersProperty.getLayerState().size());

		assertEquals(0.5, layersProperty.getLayerState().get("id1"), 0.001);
		assertEquals(0.8, layersProperty.getLayerState().get("id2"), 0.001);

	}

	private Element createPropertyElement()
	{
		Document d = WWXML.createDocumentBuilder(false).newDocument();
		return d.createElement("property");
	}

	private LayersProperty createLayersProperty(String[] ids, Double[] opacities)
	{
		LayersProperty p = new LayersProperty();

		for (int i = 0; i < ids.length; i++)
		{
			p.addLayer(ids[i], opacities[i]);
		}

		return p;
	}

	private Element createLayersPropertyElement(String[] ids, Double[] opacities)
	{
		Element property = createPropertyElement();

		Element layerState = WWXML.appendElement(property, "layerState");

		for (int i = 0; i < ids.length; i++)
		{
			Element layer = WWXML.appendElement(layerState, "layer");
			layer.setAttribute("id", ids[i]);
			layer.setAttribute("opacity", Double.toString(opacities[i]));
		}

		return property;
	}
}
