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
package au.gov.ga.earthsci.worldwind.common.layers.geometry;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.provider.ShapefileShapeProvider;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.types.airspace.AirspaceGeometryLayer;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndAttributeFactory;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * A factory class used to create {@link GeometryLayer}s from XML layer
 * definition files
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GeometryLayerFactory
{
	/**
	 * Create a new {@link GeometryLayer} from an XML definition.
	 */
	public static GeometryLayer createGeometryLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		GeometryLayer layer;

		String type = WWXML.getText(domElement, "RenderType");
		if ("Airspace".equalsIgnoreCase(type))
		{
			layer = new AirspaceGeometryLayer(params);
		}
		else
		{
			throw new IllegalArgumentException("Could not find layer for GeometryLayer: " + type);
		}

		DataLayerFactory.setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Fill the params with the values in the {@link GeometryLayer} specific XML
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
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.DATA_TYPE, "RenderType", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.SHAPE_TYPE, "ShapeType", xpath);

		setupShapeProvider(domElement, xpath, params);

		Element styles = WWXML.getElement(domElement, "Styles", xpath);
		StyleAndAttributeFactory.addStyles(styles, xpath, AVKeyMore.DATA_LAYER_STYLES, params);

		Element attributes = WWXML.getElement(domElement, "Attributes", xpath);
		StyleAndAttributeFactory.addAttributes(attributes, xpath, AVKeyMore.DATA_LAYER_ATTRIBUTES, params);

		return params;
	}

	/**
	 * Adds a {@link ShapeProvider} to params matching the 'DataFormat' XML
	 * element.
	 */
	protected static void setupShapeProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("Shapefile".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new ShapefileShapeProvider());
		}
		else
		{
			throw new IllegalArgumentException("Could not find shape provider for DataFormat: " + format);
		}
	}
}
