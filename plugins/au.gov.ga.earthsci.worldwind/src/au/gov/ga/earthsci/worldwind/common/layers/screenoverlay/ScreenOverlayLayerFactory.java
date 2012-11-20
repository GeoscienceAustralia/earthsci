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
package au.gov.ga.earthsci.worldwind.common.layers.screenoverlay;

import static gov.nasa.worldwind.layers.AbstractLayer.getLayerConfigParams;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.Validate;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;


/**
 * A factory class for creating instances of {@link ScreenOverlayLayer}s
 * from xml documents etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ScreenOverlayLayerFactory
{
	/**
	 * Create and return a new {@link ScreenOverlayLayer} from the provided XML document and parameters.
	 * <p/>
	 * Where the XML document and params define the same initialisation parameter, the one in the
	 * params list will be used. 
	 * <p/>
	 * If the provided XML document is <code>null</code>, the provided params will be used to fully initialise
	 * the {@link ScreenOverlayLayer}.
	 */
	public static ScreenOverlayLayer createScreenOverlayLayer(Element domElement, AVList params)
	{
		Validate.isTrue(domElement != null || params != null, "Either an XML document or params must be provided.");
		
		if (domElement == null)
		{
			return new ScreenOverlayLayer(params);
		}
		
		if (params == null)
		{
			params = new AVListImpl();
		}
		
		params = getLayerConfigParams(domElement, params);
		params = getScreenOverlayParams(domElement, params);
		
		return new ScreenOverlayLayer(params);
	}

	private static AVList getScreenOverlayParams(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		XPath xpath = WWXML.makeXPath();

		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.URL, "URL", xpath);
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.OVERLAY_CONTENT, "Content", xpath);
		
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.POSITION, "Position", xpath);
		
		XMLUtil.checkAndSetIntegerParam(domElement, params, ScreenOverlayKeys.BORDER_WIDTH, "BorderWidth", xpath);
		XMLUtil.checkAndSetColorParam(domElement, params, ScreenOverlayKeys.BORDER_COLOR, "BorderColor", xpath);
		XMLUtil.checkAndSetBooleanParam(domElement, params, ScreenOverlayKeys.DRAW_BORDER, "DrawBorder", xpath);
		
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.MIN_HEIGHT, "MinHeight", xpath);
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.MAX_HEIGHT, "MaxHeight", xpath);
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.MIN_WIDTH, "MinWidth", xpath);
		XMLUtil.checkAndSetStringParam(domElement, params, ScreenOverlayKeys.MAX_WIDTH, "MaxWidth", xpath);

		return params;
	}
}
