/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.sphere;


import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.net.URL;

import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * Unit tests for the {@link SphereLayerFactory} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SphereLayerFactoryTest
{

	@Test
	public void testCreateFromXML() throws Exception
	{
		URL layerDefinition = getClass().getResource("innerCore.xml");
		
		SphereLayer sphereLayer = SphereLayerFactory.createSphereLayer(XMLUtil.openDocument(layerDefinition).getDocumentElement(), null);
		
		assertEquals("Inner Core", sphereLayer.getValue(AVKeyMore.DISPLAY_NAME));
		assertEquals(new Color(150,0,150), sphereLayer.getColor());
		assertEquals(100, sphereLayer.getSlices());
		assertEquals(150, sphereLayer.getStacks());
		assertEquals(1210000, sphereLayer.getRadius(), 0.001);
	}
	
}
