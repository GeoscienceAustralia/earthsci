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
import gov.nasa.worldwind.geom.Sector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import au.gov.ga.earthsci.worldwind.common.layers.styled.Attribute;
import au.gov.ga.earthsci.worldwind.common.layers.styled.BasicStyleProvider;
import au.gov.ga.earthsci.worldwind.common.layers.styled.Style;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndText;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleProvider;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Helper class for {@link PointLayer}s. Contains common functionality for the
 * different point layer types. Contains the {@link PointProvider}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PointLayerHelper
{
	protected final StyleProvider styleProvider = new BasicStyleProvider();
	protected final PointProvider pointProvider;
	protected final URL context;
	protected final String url;
	protected final String dataCacheName;

	@SuppressWarnings("unchecked")
	public PointLayerHelper(AVList params)
	{
		//retrieve the globals from the params

		context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		url = params.getStringValue(AVKey.URL);
		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
		pointProvider = (PointProvider) params.getValue(AVKeyMore.DATA_LAYER_PROVIDER);

		styleProvider.setStyles((List<Style>) params.getValue(AVKeyMore.DATA_LAYER_STYLES));
		styleProvider.setAttributes((List<Attribute>) params.getValue(AVKeyMore.DATA_LAYER_ATTRIBUTES));

		// Disable validation of URL and DataCacheName, because some point providers
		// (such as the XMLPointProvider) don't require them:
		//Validate.notBlank(url, "Point data url not set");
		//Validate.notBlank(dataCacheName, "Point data cache name not set");

		Validate.notNull(pointProvider, "Point data provider is null");
		Validate.notNull(styleProvider.getStyles(), "Point style list is null");
		Validate.notNull(styleProvider.getAttributes(), "Point attribute list is null");
	}

	public URL getContext()
	{
		return context;
	}

	public URL getUrl() throws MalformedURLException
	{
		return new URL(context, url);
	}

	public String getDataCacheName()
	{
		return dataCacheName;
	}

	public Sector getSector()
	{
		return pointProvider.getSector();
	}

	/**
	 * Request this layer's points, using the {@link PointProvider}. Should be
	 * called by the layer in the render method.
	 * 
	 * @param layer
	 *            Layer for which to request points
	 */
	public void requestPoints(PointLayer layer)
	{
		pointProvider.requestData(layer);
	}

	/**
	 * Delegates to the {@link StyleProvider}.
	 * 
	 * @param attributeValues
	 *            Attribute values to find a matching style for
	 * @return A matching style for the provided attributes
	 */
	public StyleAndText getStyle(AVList attributeValues)
	{
		return styleProvider.getStyle(attributeValues);
	}

	public PointProvider getPointProvider()
	{
		return pointProvider;
	}
}
