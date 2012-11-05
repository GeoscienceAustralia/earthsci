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
package au.gov.ga.earthsci.worldwind.common.layers.model;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.util.WWXML;

import java.nio.ByteOrder;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.model.gdal.GDALRasterModelParameters;
import au.gov.ga.earthsci.worldwind.common.layers.model.gdal.GDALRasterModelProvider;
import au.gov.ga.earthsci.worldwind.common.layers.model.gocad.GocadModelProvider;
import au.gov.ga.earthsci.worldwind.common.layers.model.gocad.GocadReaderParameters;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * Factory used for creating {@link ModelLayer} instances an XML definition.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ModelLayerFactory
{
	/**
	 * Create a new {@link ModelLayer} from an XML definition.
	 * 
	 * @return New {@link ModelLayer}.
	 */
	public static ModelLayer createModelLayer(Element domElement, AVList params)
	{
		params = AbstractLayer.getLayerConfigParams(domElement, params);
		params = getParamsFromDocument(domElement, params);

		ModelLayer layer = new BasicModelLayer(params);
		DataLayerFactory.setLayerParams(layer, params);
		return layer;
	}

	/**
	 * Fill the params with the values in the {@link ModelLayer} specific XML
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
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKey.EXPIRY_TIME, "LastUpdate",
				DataLayerFactory.DATE_TIME_PATTERN, xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DATA_CACHE_NAME, "DataCacheName", xpath);

		WWXML.checkAndSetColorParam(domElement, params, AVKey.COLOR, "Color", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.LINE_WIDTH, "LineWidth", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_SIZE, "PointSize", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.MINIMUM_DISTANCE, "MinimumDistance", xpath);

		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.REVERSE_NORMALS, "ReverseNormals", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.ORDERED_RENDERING, "OrderedRendering", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.POINT_SPRITE, "PointSprite", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_MIN_SIZE, "PointMinSize", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_MAX_SIZE, "PointMaxSize", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_CONSTANT_ATTENUATION,
				"PointSizeAttenuation/@constant", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_LINEAR_ATTENUATION,
				"PointSizeAttenuation/@linear", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_QUADRATIC_ATTENUATION,
				"PointSizeAttenuation/@quadratic", xpath);

		String byteOrder = WWXML.getText(domElement, "ByteOrder", xpath);
		if (byteOrder != null)
		{
			if (byteOrder.equals(AVKey.LITTLE_ENDIAN) || byteOrder.toLowerCase().startsWith("little"))
			{
				params.setValue(AVKey.BYTE_ORDER, ByteOrder.LITTLE_ENDIAN);
			}
			else if (byteOrder.equals(AVKey.BIG_ENDIAN) || byteOrder.toLowerCase().startsWith("big"))
			{
				params.setValue(AVKey.BYTE_ORDER, ByteOrder.BIG_ENDIAN);
			}
		}

		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.BILINEAR_MINIFICATION, "BilinearMinification",
				xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.SUBSAMPLING_U, "Subsampling/@u", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.SUBSAMPLING_V, "Subsampling/@v", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.SUBSAMPLING_W, "Subsampling/@w", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.DYNAMIC_SUBSAMPLING, "DynamicSubsampling/@enabled",
				xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.DYNAMIC_SUBSAMPLING_SAMPLES_PER_AXIS,
				"DynamicSubsampling/@samples", xpath);

		WWXML.checkAndSetStringParam(domElement, params, AVKey.COORDINATE_SYSTEM, "CoordinateSystem", xpath);

		ColorMap colorMap = XMLUtil.getColorMap(domElement, "ColorMap", xpath);
		params.setValue(AVKeyMore.COLOR_MAP, colorMap);

		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.MAX_VARIANCE, "MaxVariance", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.PAINTED_VARIABLE, "PaintedVariable", xpath);
		
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.SCALE, "ScaleFactor", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.OFFSET, "Offset", xpath);

		setupModelProvider(domElement, xpath, params);

		return params;
	}

	/**
	 * Adds a {@link ModelProvider} to params matching the 'DataFormat' XML
	 * element.
	 */
	protected static void setupModelProvider(Element domElement, XPath xpath, AVList params)
	{
		String format = WWXML.getText(domElement, "DataFormat", xpath);

		if ("GOCAD".equalsIgnoreCase(format))
		{
			GocadReaderParameters parameters = new GocadReaderParameters(params);
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new GocadModelProvider(parameters));
		}
		else if ("GDAL".equalsIgnoreCase(format))
		{
			GDALRasterModelParameters parameters = new GDALRasterModelParameters(params);
			params.setValue(AVKeyMore.DATA_LAYER_PROVIDER, new GDALRasterModelProvider(parameters));
		}
		else
		{
			throw new IllegalArgumentException("Could not find model provider for DataFormat: " + format);
		}
	}
}
