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
package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.borehole.providers.ShapefileBoreholeProvider;
import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndAttributeFactory;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Helper class for the creation of {@link BoreholeLayer}s. Contains XML parsing
 * functionality, as well as factory methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BoreholeLayerFactory
{
	/**
	 * Create a new {@link BoreholeLayer} from an XML definition.
	 * 
	 * @return New {@link BoreholeLayer}.
	 */
	public static BoreholeLayer createBoreholeLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		BoreholeLayer layer = new BasicBoreholeLayer(params);
		DataLayerFactory.setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Fill the params with the values in the {@link BoreholeLayer} specific XML
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

		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.BOREHOLE_UNIQUE_IDENTIFIER_ATTRIBUTE,
				"UniqueBoreholeIdentifier", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.BOREHOLE_SAMPLE_DEPTH_FROM_ATTRIBUTE,
				"SampleDepthAttributes/@from", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.BOREHOLE_SAMPLE_DEPTH_TO_ATTRIBUTE,
				"SampleDepthAttributes/@to", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.BOREHOLE_SAMPLE_DEPTH_ATTRIBUTES_POSITIVE,
				"SampleDepthAttributes/@positive", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.LINE_WIDTH, "LineWidth", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.MINIMUM_DISTANCE, "MinimumDistance", xpath);

		setupBoreholeProvider(domElement, xpath, params);

		Element styles = WWXML.getElement(domElement, "BoreholeStyles", xpath);
		StyleAndAttributeFactory.addStyles(styles, xpath, AVKeyMore.DATA_LAYER_STYLES, params);

		Element attributes = WWXML.getElement(domElement, "BoreholeAttributes", xpath);
		StyleAndAttributeFactory.addAttributes(attributes, xpath, AVKeyMore.DATA_LAYER_ATTRIBUTES, params);

		Element sampleStyles = WWXML.getElement(domElement, "SampleStyles", xpath);
		StyleAndAttributeFactory.addStyles(sampleStyles, xpath, AVKeyMore.BOREHOLE_SAMPLE_STYLES, params);

		Element sampleAttributes = WWXML.getElement(domElement, "SampleAttributes", xpath);
		StyleAndAttributeFactory.addAttributes(sampleAttributes, xpath, AVKeyMore.BOREHOLE_SAMPLE_ATTRIBUTES, params);

		return params;
	}

	/**
	 * Adds a {@link BoreholeProvider} to params matching the 'DataFormat' XML
	 * element.
	 */
	protected static void setupBoreholeProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("Shapefile".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new ShapefileBoreholeProvider());
		}
		else
		{
			throw new IllegalArgumentException("Could not find borehole provider for DataFormat: " + format);
		}
	}
}
