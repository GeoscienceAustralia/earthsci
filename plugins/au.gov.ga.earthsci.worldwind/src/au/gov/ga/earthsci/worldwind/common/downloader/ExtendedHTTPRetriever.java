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
package au.gov.ga.earthsci.worldwind.common.downloader;

import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Extension of {@link HTTPRetriever} which implements {@link ExtendedRetriever}
 * .
 * <p/>
 * Allows modification dates to be set on read, and uses the java
 * {@link ProxySelector} mechanism rather than the WWIO configuration mechanism.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ExtendedHTTPRetriever extends HTTPRetriever implements ExtendedRetriever
{
	private Long ifModifiedSince;
	private Exception error;
	private boolean unzip;

	public ExtendedHTTPRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor)
	{
		this(url, ifModifiedSince, postProcessor, true);
	}

	public ExtendedHTTPRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor, boolean unzip)
	{
		super(url, postProcessor);
		this.ifModifiedSince = ifModifiedSince;
		this.unzip = unzip;
	}

	@Override
	protected ByteBuffer doRead(URLConnection connection) throws Exception
	{
		// Overridden to catch exceptions and set the modification date in the URLConnection

		if (ifModifiedSince != null)
			connection.setIfModifiedSince(ifModifiedSince.longValue());
		try
		{
			ByteBuffer buffer = super.doRead(connection);
			if (buffer == null && !isOk() && !isNotModified())
			{
				throw new HttpException(getResponseCode() + ": " + getResponseMessage(), getResponseCode());
			}
			return buffer;
		}
		catch (Exception e)
		{
			error = e;
			throw e;
		}
	}

	@Override
	protected URLConnection openConnection() throws IOException
	{
		// Overridden to use the Java proxy selector mechanism rather than the World Wind WWIO configuration
		// This gives access to the nonProxyHosts mechanism for bypassing proxies for internal addresses
		// Falls back to the original behaviour if the proxy selector mechanism fails

		try
		{
			List<Proxy> proxies = ProxySelector.getDefault().select(url.toURI());
			this.connection = this.url.openConnection(proxies.get(0));
			this.connection.setConnectTimeout(this.connectTimeout);
			this.connection.setReadTimeout(this.readTimeout);
			return connection;
		}
		catch (Exception e)
		{
			// If the proxy selector failed, revert to superclass behaviour
			return super.openConnection();
		}
	}

	/**
	 * Was the response status code from the server a HTTP_OK (200)?
	 * 
	 * @return True if the http server returned status 200
	 */
	public boolean isOk()
	{
		return getResponseCode() == HttpURLConnection.HTTP_OK;
	}

	@Override
	public boolean isNotModified()
	{
		return getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED;
	}

	@Override
	public Exception getError()
	{
		return error;
	}

	@Override
	protected ByteBuffer readZipStream(InputStream inputStream, URL url) throws IOException
	{
		if (unzip)
		{
			return super.readZipStream(inputStream, url);
		}
		return readNonSpecificStream(inputStream, getConnection());
	}
}
