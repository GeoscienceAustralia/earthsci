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

import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import au.gov.ga.earthsci.core.util.QueryString;
import au.gov.ga.earthsci.core.util.UTF8URLEncoder;

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
	 * Generate a URI that represents a WMS layer.
	 * 
	 * @param capabilitiesURI
	 *            URI pointing to the WMS capabilities document
	 * @param layer
	 *            WMS layer
	 * @param style
	 *            WMS style
	 * @return URI for the WMS layer
	 * @throws URISyntaxException
	 */
	public static URI generateLayerURI(URI capabilitiesURI, WMSLayerCapabilities layer, WMSLayerStyle style)
			throws URISyntaxException
	{
		String scheme = WMS_LAYER_URI_SCHEME;
		String authority = ""; //$NON-NLS-1$
		String query = WMS_LAYER_URI_URL_PARAMETER + "=" + UTF8URLEncoder.encode(capabilitiesURI.toString()); //$NON-NLS-1$
		if (layer != null)
		{
			query += "&" + WMS_LAYER_URI_LAYERS_PARAMETER + "=" + UTF8URLEncoder.encode(layer.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (style != null)
		{
			query += "&" + WMS_LAYER_URI_STYLES_PARAMETER + "=" + UTF8URLEncoder.encode(style.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return new URI(scheme, authority, null, query, null);
	}

	public static URI getCapabilitiesURIFromLayerURI(URI layerURI) throws URISyntaxException
	{
		QueryString query = new QueryString(layerURI.getQuery());
		List<String> urls = query.get(WMS_LAYER_URI_URL_PARAMETER);
		if (urls == null || urls.isEmpty())
		{
			return null;
		}
		return new URI(urls.get(0));
	}
}
