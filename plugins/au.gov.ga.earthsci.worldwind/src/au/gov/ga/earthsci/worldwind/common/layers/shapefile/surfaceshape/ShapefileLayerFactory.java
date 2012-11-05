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
package au.gov.ga.earthsci.worldwind.common.layers.shapefile.surfaceshape;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwindx.examples.util.ShapefileLoader;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Layer factory which creates a {@link Layer} from an AVList or XML element.
 * The parameters must contain a URL which points to a ESRI shapefile.
 */
public class ShapefileLayerFactory
{
	/**
	 * Create a new {@link Layer}.
	 * 
	 * @param params
	 *            Parameters describing the layer
	 * @return A new {@link Layer}
	 */
	public static Layer createLayer(AVList params)
	{
		String s = params.getStringValue(AVKey.URL);

		if (s == null)
		{
			String message = "Shapefile URL not specified";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		URL url;
		try
		{
			url = new URL(context, s);
		}
		catch (MalformedURLException e)
		{
			String message = "Shapefile URL malformed";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		//read the shapefile, and use the standard ShapefileLoader to create a layer
		Shapefile shapefile = new UrlShapefile(url);
		Layer layer = new ShapefileLoader().createLayerFromShapefile(shapefile);

		//set the sector parameter so that the extents can be zoomed to
		if (params.getValue(AVKey.SECTOR) == null)
		{
			layer.setValue(AVKey.SECTOR, Sector.fromDegrees(shapefile.getBoundingRectangle()));
		}

		s = params.getStringValue(AVKey.DISPLAY_NAME);
		if (s != null)
		{
			layer.setName(s);
		}

		Long l = (Long) params.getValue(AVKey.EXPIRY_TIME);
		if (l != null)
		{
			layer.setExpiryTime(l);
		}

		//set the shape drawing attributes
		ShapeAttributes attributes = new BasicShapeAttributes();
		setAttributesFromParams(attributes, params);

		//also set the attributes on any renderables within the layer
		if (layer instanceof RenderableLayer)
		{
			RenderableLayer rl = (RenderableLayer) layer;
			for (Renderable renderable : rl.getRenderables())
			{
				if (renderable instanceof SurfaceShape)
				{
					((SurfaceShape) renderable).setAttributes(attributes);
				}
			}
		}

		//disable picking by default
		layer.setPickEnabled(false);

		return layer;
	}

	/**
	 * Create a new {@link Layer} from an XML definition.
	 * 
	 * @param domElement
	 *            XML element
	 * @param params
	 *            Extra parameters describing the layer
	 * @return A new {@link Layer}
	 */
	public static Layer createLayer(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		XPath xpath = WWXML.makeXPath();

		AbstractLayer.getLayerConfigParams(domElement, params);
		getAttributeParams(domElement, params);

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);

		return createLayer(params);
	}

	/**
	 * Put any shape drawing attributes defined in the AVList into the
	 * attributes.
	 * 
	 * @param attributes
	 *            Destination attributes object
	 * @param params
	 *            Source parameters
	 */
	protected static void setAttributesFromParams(ShapeAttributes attributes, AVList params)
	{
		Boolean b = (Boolean) params.getValue(ShapeAttributeKey.DRAW_INTERIOR);
		if (b != null)
		{
			attributes.setDrawInterior(b);
		}

		b = (Boolean) params.getValue(ShapeAttributeKey.DRAW_OUTLINE);
		if (b != null)
		{
			attributes.setDrawOutline(b);
		}

		b = (Boolean) params.getValue(ShapeAttributeKey.ANTIALIASING);
		if (b != null)
		{
			attributes.setEnableAntialiasing(b);
		}

		Color c = (Color) params.getValue(ShapeAttributeKey.INTERIOR_COLOR);
		if (c != null)
		{
			attributes.setInteriorMaterial(new Material(c));
		}

		c = (Color) params.getValue(ShapeAttributeKey.OUTLINE_COLOR);
		if (c != null)
		{
			attributes.setOutlineMaterial(new Material(c));
		}

		Double d = (Double) params.getValue(ShapeAttributeKey.INTERIOR_OPACITY);
		if (d != null)
		{
			attributes.setInteriorOpacity(d);
		}

		d = (Double) params.getValue(ShapeAttributeKey.OUTLINE_OPACITY);
		if (d != null)
		{
			attributes.setOutlineOpacity(d);
		}

		d = (Double) params.getValue(ShapeAttributeKey.OUTLINE_WIDTH);
		if (d != null)
		{
			attributes.setOutlineWidth(d);
		}

		Integer i = (Integer) params.getValue(ShapeAttributeKey.STIPPLE_FACTOR);
		if (i != null)
		{
			attributes.setOutlineStippleFactor(i);
		}

		i = (Integer) params.getValue(ShapeAttributeKey.STIPPLE_PATTERN);
		if (i != null)
		{
			attributes.setOutlineStipplePattern(i.shortValue());
		}
	}

	/**
	 * Pull any shape drawing attributes from an XML element and store them in
	 * the AVList.
	 * 
	 * @param domElement
	 *            Source XML element
	 * @param params
	 *            Destination AVList
	 * @return params
	 */
	protected static AVList getAttributeParams(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetBooleanParam(domElement, params, ShapeAttributeKey.DRAW_INTERIOR, "DrawInterior", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, ShapeAttributeKey.DRAW_OUTLINE, "DrawOutline", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, ShapeAttributeKey.ANTIALIASING, "Antialiasing", xpath);
		WWXML.checkAndSetColorParam(domElement, params, ShapeAttributeKey.INTERIOR_COLOR, "InteriorColor", xpath);
		WWXML.checkAndSetColorParam(domElement, params, ShapeAttributeKey.OUTLINE_COLOR, "OutlineColor", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, ShapeAttributeKey.INTERIOR_OPACITY, "InteriorOpacity", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, ShapeAttributeKey.OUTLINE_OPACITY, "OutlineOpacity", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, ShapeAttributeKey.OUTLINE_WIDTH, "OutlineWidth", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, ShapeAttributeKey.STIPPLE_FACTOR, "StippleFactor", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, ShapeAttributeKey.STIPPLE_PATTERN, "StipplePattern", xpath);

		return params;
	}
}
