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
package au.gov.ga.earthsci.worldwind.common.layers.volume;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayerFactory;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * Factory used for creating {@link VolumeLayer} instances an XML definition.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class VolumeLayerFactory
{
	/**
	 * Create a new {@link VolumeLayer} from an XML definition.
	 * 
	 * @return New {@link VolumeLayer}.
	 */
	public static VolumeLayer createVolumeLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		VolumeLayer layer = new BasicVolumeLayer(params);
		DataLayerFactory.setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Fill the params with the values in the {@link VolumeLayer} specific XML
	 * elements.
	 */
	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME, "ExpiryTime", xpath);
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate", DataLayerFactory.DATE_TIME_PATTERN, xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DATA_CACHE_NAME, "DataCacheName", xpath);

		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.MAX_VARIANCE, "MaxVariance", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.MINIMUM_DISTANCE, "MinimumDistance", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.COORDINATE_SYSTEM, "CoordinateSystem", xpath);

		WWXML.checkAndSetColorParam(domElement, params, AVKeyMore.NO_DATA_COLOR, "NoDataColor", xpath);

		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.INITIAL_OFFSET_MIN_U, "InitialOffset/@minU", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.INITIAL_OFFSET_MAX_U, "InitialOffset/@maxU", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.INITIAL_OFFSET_MIN_V, "InitialOffset/@minV", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.INITIAL_OFFSET_MAX_V, "InitialOffset/@maxV", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.INITIAL_OFFSET_MIN_W, "InitialOffset/@minW", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.INITIAL_OFFSET_MAX_W, "InitialOffset/@maxW", xpath);

		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.REVERSE_NORMALS, "ReverseNormals", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.ORDERED_RENDERING, "OrderedRendering", xpath);
		
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.PAINTED_VARIABLE, "PaintedVariable", xpath);

		ColorMap colorMap = XMLUtil.getColorMap(domElement, "ColorMap", xpath);
		params.setValue(AVKeyMore.COLOR_MAP, colorMap);

		setupVolumeDataProvider(domElement, xpath, params);

		return params;
	}

	/**
	 * Adds a {@link VolumeProvider} to params matching the 'DataFormat' XML
	 * element.
	 */
	protected static void setupVolumeDataProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("GOCAD SGrid".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new SGridVolumeDataProvider());
		}
		else if ("Array".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new ArrayVolumeDataProvider());
		}
		else if ("Position Array".equalsIgnoreCase(format))
		{
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new ArrayWithPositionsVolumeDataProvider());
		}
		else
		{
			throw new IllegalArgumentException("Could not find volume data provider for DataFormat: " + format);
		}
	}
}
