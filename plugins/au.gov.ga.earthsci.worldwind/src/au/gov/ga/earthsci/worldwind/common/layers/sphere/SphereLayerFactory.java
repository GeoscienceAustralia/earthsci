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

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * 
 * A factory class used to create {@link SphereLayer} instances
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SphereLayerFactory
{
	/**
	 * Create a new {@link SphereLayer} from an XML definition.
	 * 
	 * @return the new {@link SphereLayer} created from the provided XML definition
	 */
	public static SphereLayer createSphereLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		SphereLayer layer = new SphereLayer(params);
		return layer;
	}
	
	/**
	 * Extract SphereLayer-specific params from the provided XML document
	 */
	private static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		XPath xpath = WWXML.makeXPath();
		
		XMLUtil.checkAndSetDoubleParam(domElement, params, AVKeyMore.SPHERE_RADIUS, "Radius", xpath);
		XMLUtil.checkAndSetIntegerParam(domElement, params, AVKeyMore.SPHERE_SLICES, "Slices", xpath);
		XMLUtil.checkAndSetIntegerParam(domElement, params, AVKeyMore.SPHERE_STACKS, "Stacks", xpath);
		XMLUtil.checkAndSetColorParam(domElement, params, AVKeyMore.COLOR, "Color", xpath);
		
		return params;
	}

	private SphereLayerFactory(){}
}
