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
package au.gov.ga.earthsci.discovery.csw;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for getting access to icon URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Icons
{
	public static final URL MAP_SERVER;

	static
	{
		MAP_SERVER = getURL("icons/map_server.gif"); //$NON-NLS-1$
	}

	private static final Logger logger = LoggerFactory.getLogger(Icons.class);

	private static URL getURL(String resourceName)
	{
		try
		{
			return new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/" + resourceName); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (MalformedURLException e)
		{
			logger.error("Error creating icon url", e); //$NON-NLS-1$
			return null;
		}
	}

	private Icons()
	{
	}
}
