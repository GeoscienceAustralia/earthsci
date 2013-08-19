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
package au.gov.ga.earthsci.discovery.darwin;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.earthsci.worldwind.common.util.URLUtil;

/**
 * URI that describes some data linked to a DARWIN discovery record.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DarwinDiscoveryResultURL
{
	private final String name;
	private final URL url;
	private final String protocol;

	public DarwinDiscoveryResultURL(String name, URL url, String protocol)
	{
		this.name = name;
		this.url = url;
		this.protocol = protocol;
	}

	public String getName()
	{
		return name;
	}

	public URL getUrl()
	{
		return url;
	}

	public String getProtocol()
	{
		return protocol;
	}

	public URL getUrlWithRequiredParameters() throws MalformedURLException
	{
		String lowerProtocol = protocol.toLowerCase();
		String stringUrl = url.toString();

		boolean ogc = false;
		if (lowerProtocol.contains("wms"))
		{
			ogc = true;
			stringUrl = URLUtil.addQueryParameterIfMissing(stringUrl, "service=WMS", true);
		}
		else if (lowerProtocol.contains("wfs"))
		{
			ogc = true;
			stringUrl = URLUtil.addQueryParameterIfMissing(stringUrl, "service=WFS", true);
		}
		else if (lowerProtocol.contains("wcs"))
		{
			ogc = true;
			stringUrl = URLUtil.addQueryParameterIfMissing(stringUrl, "service=WCS", true);
		}
		if (ogc)
		{
			stringUrl = URLUtil.addQueryParameterIfMissing(stringUrl, "request=GetCapabilities", true);
		}

		return new URL(stringUrl);
	}
}
