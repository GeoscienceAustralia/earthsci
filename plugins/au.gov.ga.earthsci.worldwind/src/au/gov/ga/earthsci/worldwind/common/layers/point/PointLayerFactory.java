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
package au.gov.ga.earthsci.worldwind.common.layers.point;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.point.providers.ShapefilePointProvider;
import au.gov.ga.earthsci.worldwind.common.layers.point.providers.XMLPointProvider;
import au.gov.ga.earthsci.worldwind.common.layers.point.types.AnnotationPointLayer;
import au.gov.ga.earthsci.worldwind.common.layers.point.types.IconPointLayer;
import au.gov.ga.earthsci.worldwind.common.layers.point.types.MarkerPointLayer;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndAttributeFactory;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Helper class for the creation of {@link PointLayer}s. Contains XML parsing
 * functionality, as well as factory methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PointLayerFactory
{
	/**
	 * Create a new {@link PointLayer} from an XML definition.
	 * 
	 * @return New {@link PointLayer}.
	 */
	public static PointLayer createPointLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		PointLayerHelper helper = new PointLayerHelper(params);

		PointLayer layer;

		String type = WWXML.getText(domElement, "PointType");
		if ("Marker".equalsIgnoreCase(type))
		{
			layer = new MarkerPointLayer(helper);
		}
		else if ("Annotation".equalsIgnoreCase(type))
		{
			layer = new AnnotationPointLayer(helper);
		}
		else if ("Icon".equalsIgnoreCase(type))
		{
			layer = new IconPointLayer(helper);
		}
		else
		{
			throw new IllegalArgumentException("Could not find layer for PointType: " + type);
		}

		DataLayerFactory.setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Fill the params with the values in the {@link PointLayer} specific XML
	 * elements.
	 */
	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME, "ExpiryTime", xpath);
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate",
				DataLayerFactory.DATE_TIME_PATTERN, xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DATA_CACHE_NAME, "DataCacheName", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.DATA_TYPE, "PointType", xpath);

		setupPointProvider(domElement, xpath, params);

		Element styles = WWXML.getElement(domElement, "Styles", xpath);
		StyleAndAttributeFactory.addStyles(styles, xpath, AVKeyMore.DATA_LAYER_STYLES, params);

		Element attributes = WWXML.getElement(domElement, "Attributes", xpath);
		StyleAndAttributeFactory.addAttributes(attributes, xpath, AVKeyMore.DATA_LAYER_ATTRIBUTES, params);

		return params;
	}

	/**
	 * Adds a {@link PointProvider} to params matching the 'DataFormat' XML
	 * element.
	 */
	protected static void setupPointProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("Shapefile".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new ShapefilePointProvider());
		}
		else if ("XML".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new XMLPointProvider(domElement));
		}
		else
		{
			throw new IllegalArgumentException("Could not find point provider for DataFormat: " + format);
		}
	}
}
