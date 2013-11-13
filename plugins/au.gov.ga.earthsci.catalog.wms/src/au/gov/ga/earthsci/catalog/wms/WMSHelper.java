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

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerInfoURL;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import au.gov.ga.earthsci.common.util.UTF8URLEncoder;
import au.gov.ga.earthsci.common.util.Util;

/**
 * Class containing helper methods for the WMS catalog.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WMSHelper
{
	/**
	 * Content type ID for a content type that represents WMS layers in
	 * EarthSci.
	 */
	public final static String WMS_LAYER_CONTENT_TYPE_ID = "au.gov.ga.earthsci.catalog.wms.wmslayer"; //$NON-NLS-1$

	/**
	 * URI scheme used for WMS layers.
	 */
	public final static String WMS_LAYER_URI_SCHEME = "wmslayer"; //$NON-NLS-1$

	/**
	 * URI query key to use for the url parameter.
	 */
	public final static String WMS_LAYER_URI_URL_PARAMETER = "url"; //$NON-NLS-1$

	/**
	 * URI query key to use for the layer parameter.
	 */
	public final static String WMS_LAYER_URI_LAYERS_PARAMETER = "layers"; //$NON-NLS-1$

	/**
	 * URI query key to use for the style parameter.
	 */
	public final static String WMS_LAYER_URI_STYLES_PARAMETER = "styles"; //$NON-NLS-1$

	/**
	 * Add a subpath to the path of a URI.
	 * 
	 * @param uri
	 *            URI to add the subpath to
	 * @param subpath
	 *            Subpath to add
	 * @return URI with the given subpath added
	 */
	public static URI uriSubpath(URI uri, String subpath)
	{
		if (uri == null)
		{
			return null;
		}

		if (subpath == null || subpath.length() == 0)
		{
			//if the layer name is blank, use a UUID for guaranteed uniqueness
			subpath = UUID.randomUUID().toString();
		}

		//encode special characters
		subpath = UTF8URLEncoder.encode(subpath);

		//prepend old path
		String path = uri.getPath();
		if (path != null && path.length() > 0)
		{
			subpath = path + "/" + subpath; //$NON-NLS-1$
		}

		//make absolute uri if required (if scheme is non-null)
		if (uri.getScheme() != null && subpath.charAt(0) != '/')
		{
			subpath = "/" + subpath; //$NON-NLS-1$
		}

		try
		{
			return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), subpath, uri.getQuery(),
					uri.getFragment());
		}
		catch (URISyntaxException e)
		{
			//not possible, as parent is a valid URI
			return null;
		}
	}

	/**
	 * URL pointing to information about the given WMS layer.
	 * 
	 * @param capabilities
	 *            WMS Capabilities document the information URL is read from
	 * @param layerName
	 *            Layer name to get the information URL for
	 * @return Information URL for the WMS layer
	 */
	public static URL getInformationURL(WMSCapabilities capabilities, String layerName)
	{
		if (capabilities == null || Util.isEmpty(layerName))
		{
			return null;
		}
		return getInformationURL(capabilities.getLayerByName(layerName));
	}

	/**
	 * URL pointing to information about the given WMS layer.
	 * 
	 * @param layer
	 *            Layer to get the information URL for
	 * @return Information URL for the WMS layer
	 */
	public static URL getInformationURL(WMSLayerCapabilities layer)
	{
		if (layer == null)
		{
			return null;
		}
		return getFirstURL(layer.getDataURLs());
	}

	/**
	 * URL pointing to the legend for the given WMS layer/style. If
	 * <code>styleName</code> is null or blank, the layer's first style with a
	 * valid legend URL is used.
	 * 
	 * @param capabilities
	 *            WMS Capabilities document the legend URL is read from
	 * @param layerName
	 *            Layer name to get the legend URL for
	 * @param styleName
	 *            Style name to get the legend URL for
	 * @return URL pointing to the legend for the WMS layer/style
	 */
	public static URL getLegendURL(WMSCapabilities capabilities, String layerName, String styleName)
	{
		if (capabilities == null || Util.isEmpty(layerName))
		{
			return null;
		}

		WMSLayerCapabilities layer = capabilities.getLayerByName(layerName);
		if (layer == null)
		{
			return null;
		}

		if (!Util.isEmpty(styleName))
		{
			return getLegendURL(layer.getStyleByName(styleName));
		}

		for (WMSLayerStyle style : layer.getStyles())
		{
			URL url = getLegendURL(style);
			if (url != null)
			{
				return url;
			}
		}
		return null;
	}

	/**
	 * Get the first legend URL in the given style.
	 * 
	 * @param style
	 *            Style to get the legend from
	 * @return URL pointing to a legend for the style
	 */
	public static URL getLegendURL(WMSLayerStyle style)
	{
		if (style == null)
		{
			return null;
		}
		return getFirstURL(style.getLegendURLs());
	}

	/**
	 * Get the first valid {@link URL} from a collection of
	 * {@link WMSLayerInfoURL}s.
	 * 
	 * @param urls
	 *            URLs to search
	 * @return First valid URL in urls
	 */
	public static URL getFirstURL(Collection<? extends WMSLayerInfoURL> urls)
	{
		if (urls != null)
		{
			for (WMSLayerInfoURL url : urls)
			{
				if (url == null || url.getOnlineResource() == null || Util.isEmpty(url.getOnlineResource().getHref()))
				{
					continue;
				}
				try
				{
					return new URL(url.getOnlineResource().getHref());
				}
				catch (MalformedURLException e)
				{
				}
			}
		}
		return null;
	}
}
