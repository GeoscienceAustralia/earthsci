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
package au.gov.ga.earthsci.worldwind.common.layers.shapefile.surfaceshape;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import gov.nasa.worldwind.formats.shapefile.DBaseFile;
import gov.nasa.worldwind.util.Logging;

public class UrlDBaseFile extends DBaseFile
{
	public UrlDBaseFile(Object source)
	{
		super(source);
	}

	@Override
	protected String validateURLConnection(URLConnection connection, String[] acceptedContentTypes)
	{
		try
		{
			if (connection instanceof HttpURLConnection
					&& ((HttpURLConnection) connection).getResponseCode() != HttpURLConnection.HTTP_OK)
			{
				return Logging.getMessage("HTTP.ResponseCode", ((HttpURLConnection) connection).getResponseCode(),
						connection.getURL());
			}
		}
		catch (Exception e)
		{
			return Logging.getMessage("URLRetriever.ErrorOpeningConnection", connection.getURL());
		}

		//ignore content type checking

		return null;
	}
}
