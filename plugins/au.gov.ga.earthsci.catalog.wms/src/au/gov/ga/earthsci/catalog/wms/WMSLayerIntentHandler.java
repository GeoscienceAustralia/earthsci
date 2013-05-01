/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.catalog.wms;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import au.gov.ga.earthsci.core.intent.AbstractRetrieveIntentHandler;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.util.QueryString;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * {@link IIntentHandler} that creates a {@link WMSTiledImageLayer} from a
 * wmslayer:// intent.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WMSLayerIntentHandler extends AbstractRetrieveIntentHandler
{
	@Override
	protected void handle(IRetrievalData data, URL url, Intent intent, IIntentCallback callback)
	{
		try
		{
			InputStream is = data.getInputStream();
			try
			{
				WMSCapabilities wmsCapabilities = new WMSCapabilities(is).parse();
				if (wmsCapabilities == null)
				{
					throw new Exception("Error parsing WMS_Capabilities document from URL: " + url); //$NON-NLS-1$
				}

				QueryString query = new QueryString(intent.getURI().getQuery());
				List<String> layerNames = commaDelimited(query.get(WMSHelper.WMS_LAYER_URI_LAYERS_PARAMETER));
				List<String> styleNames = commaDelimited(query.get(WMSHelper.WMS_LAYER_URI_STYLES_PARAMETER));

				AVList params = new AVListImpl();
				params.setValue(AVKey.LAYER_NAMES, commaSeparated(layerNames));
				params.setValue(AVKey.STYLE_NAMES, commaSeparated(styleNames));

				URL legendURL = getLegendURL(wmsCapabilities, layerNames, styleNames);
				if (legendURL != null)
				{
					params.setValue(AVKeyMore.LEGEND_URL, legendURL);
				}
				URL informationURL = getInformationURL(wmsCapabilities, layerNames);

				Layer layer = new InformationedWMSTiledImageLayer(wmsCapabilities, params, informationURL);
				if (legendURL != null)
				{
					layer.setValue(AVKeyMore.LEGEND_URL, legendURL);
				}

				callback.completed(layer, intent);
			}
			finally
			{
				is.close();
			}
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

	@Override
	protected URL getRetrievalURL(Intent intent) throws MalformedURLException
	{
		try
		{
			return WMSHelper.getCapabilitiesURIFromLayerURI(intent.getURI()).toURL();
		}
		catch (URISyntaxException e)
		{
			throw new MalformedURLException(e.getLocalizedMessage());
		}
	}

	protected static URL getInformationURL(WMSCapabilities capabilities, Collection<String> layerNames)
	{
		if (layerNames != null)
		{
			for (String layerName : layerNames)
			{
				URL url = WMSHelper.getInformationURL(capabilities, layerName);
				if (url != null)
				{
					return url;
				}
			}
		}
		return null;
	}

	protected static URL getLegendURL(WMSCapabilities capabilities, Collection<String> layerNames,
			Collection<String> styleNames)
	{
		if (layerNames != null)
		{
			for (String layerName : layerNames)
			{
				if (styleNames == null || styleNames.isEmpty())
				{
					URL url = WMSHelper.getLegendURL(capabilities, layerName, null);
					if (url != null)
					{
						return url;
					}
				}
				else
				{
					for (String styleName : styleNames)
					{
						URL url = WMSHelper.getLegendURL(capabilities, layerName, styleName);
						if (url != null)
						{
							return url;
						}
					}
				}
			}
		}
		return null;
	}

	protected static List<String> commaDelimited(Collection<String> strings)
	{
		if (strings == null)
		{
			return null;
		}
		List<String> all = new ArrayList<String>();
		for (String string : strings)
		{
			all.addAll(commaDelimited(string));
		}
		return all;
	}

	protected static List<String> commaDelimited(String string)
	{
		return Arrays.asList(string.split(",")); //$NON-NLS-1$
	}

	protected static String commaSeparated(Collection<String> strings)
	{
		if (strings == null || strings.isEmpty())
		{
			return ""; //$NON-NLS-1$
		}
		StringBuilder sb = new StringBuilder();
		for (String string : strings)
		{
			sb.append(',');
			sb.append(string);
		}
		return sb.substring(1);
	}
}
