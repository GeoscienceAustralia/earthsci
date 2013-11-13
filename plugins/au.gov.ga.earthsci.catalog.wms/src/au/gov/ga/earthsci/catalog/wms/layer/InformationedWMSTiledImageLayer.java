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
package au.gov.ga.earthsci.catalog.wms.layer;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwind.wms.WMSTiledImageLayer.URLBuilder;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.earthsci.common.util.IInformationed;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.DelegatorTiledImageLayer;

/**
 * {@link WMSTiledImageLayer} subclass that implements {@link IInformationed}.
 * Also fixes the superclass' URLBuilder implementation where the version
 * parameter is set to "1.3" instead of "1.3.0" if capabilities version is 1.3.0
 * or greater.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InformationedWMSTiledImageLayer extends DelegatorTiledImageLayer implements IInformationed
{
	private final URL informationURL;

	public InformationedWMSTiledImageLayer(WMSCapabilities caps, AVList params, URL informationURL)
	{
		super(fixUrlBuilder(WMSTiledImageLayer.wmsGetParamsFromCapsDoc(caps, params), caps));
		this.informationURL = informationURL;
	}

	@Override
	public URL getInformationURL()
	{
		return informationURL;
	}

	@Override
	public String getInformationString()
	{
		return null;
	}

	protected static AVList fixUrlBuilder(AVList params, WMSCapabilities caps)
	{
		params.setValue(AVKey.TILE_URL_BUILDER, new VersionURLBuilder(params, caps));
		return params;
	}

	public static class VersionURLBuilder extends URLBuilder
	{
		private final String capsVersion;

		public VersionURLBuilder(AVList params, WMSCapabilities caps)
		{
			super(params);
			capsVersion = caps.getVersion();
		}

		@Override
		public URL getURL(Tile tile, String altImageFormat) throws MalformedURLException
		{
			URL url = super.getURL(tile, altImageFormat);
			if (capsVersion == null)
			{
				return url;
			}
			String urlString = url.toExternalForm();
			urlString = urlString.replaceAll("&version=[^&]*&", "&version=" + capsVersion + "&"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return new URL(urlString);
		}
	}
}
