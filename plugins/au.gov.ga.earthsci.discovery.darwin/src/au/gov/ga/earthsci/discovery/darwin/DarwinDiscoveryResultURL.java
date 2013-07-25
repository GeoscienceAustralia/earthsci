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

import java.net.URL;

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
}
