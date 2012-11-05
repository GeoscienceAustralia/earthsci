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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;

public class UrlShapefile extends Shapefile
{
	public UrlShapefile(Object source)
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

	//OVERRIDDEN to create a UrlDBaseFile instead of a DBaseFile
	
	@Override
	protected void initializeFromURL(URL url, AVList params) throws IOException
	{
		// Opening the Shapefile URL as a URL connection. Throw an IOException if the URL connection cannot be opened,
		// or if it's an invalid Shapefile connection.
		URLConnection connection = url.openConnection();

		String message = this.validateURLConnection(connection, SHAPE_CONTENT_TYPES);
		if (message != null)
		{
			throw new IOException(message);
		}

		this.shpChannel = Channels.newChannel(WWIO.getBufferedInputStream(connection.getInputStream()));

		// Attempt to open the optional index and projection resources associated with the Shapefile. Ignore exceptions
		// thrown while attempting to open these optional resource streams, but log a warning if the URL connection is
		// invalid. We wrap each source InputStream in a BufferedInputStream because this increases read performance,
		// even when the stream is wrapped in an NIO Channel.
		URLConnection shxConnection = this.getURLConnection(WWIO.replaceSuffix(url.toString(), INDEX_FILE_SUFFIX));
		if (shxConnection != null)
		{
			message = this.validateURLConnection(shxConnection, INDEX_CONTENT_TYPES);
			if (message != null)
				Logging.logger().warning(message);
			else
			{
				InputStream shxStream = this.getURLStream(shxConnection);
				if (shxStream != null)
					this.shxChannel = Channels.newChannel(WWIO.getBufferedInputStream(shxStream));
			}
		}

		URLConnection prjConnection = this.getURLConnection(WWIO.replaceSuffix(url.toString(), PROJECTION_FILE_SUFFIX));
		if (prjConnection != null)
		{
			message = this.validateURLConnection(prjConnection, PROJECTION_CONTENT_TYPES);
			if (message != null)
				Logging.logger().warning(message);
			else
			{
				InputStream prjStream = this.getURLStream(prjConnection);
				if (prjStream != null)
					this.prjChannel = Channels.newChannel(WWIO.getBufferedInputStream(prjStream));
			}
		}

		// Open the shapefile attribute source as a DBaseFile. We let the DBaseFile determine how to handle source URL.
		URL dbfURL = WWIO.makeURL(WWIO.replaceSuffix(url.toString(), ATTRIBUTE_FILE_SUFFIX));
		if (dbfURL != null)
		{
			try
			{
				//MODIFIED
				this.attributeFile = new UrlDBaseFile(dbfURL);
				//MODIFIED
			}
			catch (Exception e)
			{
				// Exception already logged by DBaseFile constructor.
			}
		}

		this.setValue(AVKey.DISPLAY_NAME, url.toString());
		this.initialize(params);
	}
}
