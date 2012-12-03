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
package au.gov.ga.earthsci.core.retrieve.retriever;

import gov.nasa.worldwind.util.WWIO;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.retrieve.IRetriever;
import au.gov.ga.earthsci.core.retrieve.IRetrieverMonitor;
import au.gov.ga.earthsci.core.retrieve.RetrievalStatus;
import au.gov.ga.earthsci.core.retrieve.RetrieverResult;
import au.gov.ga.earthsci.core.retrieve.RetrieverResultStatus;
import au.gov.ga.earthsci.core.retrieve.result.ByteBufferRetrievalResult;
import au.gov.ga.earthsci.core.util.ConfigurationUtil;

/**
 * {@link IRetriever} implementation used for retrieving HTTP URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HttpRetriever implements IRetriever
{
	private final static Logger logger = LoggerFactory.getLogger(HttpRetriever.class);
	private final static File cacheDirectory;

	static
	{
		File cacheDir;
		try
		{
			cacheDir = ConfigurationUtil.getWorkspaceFile("retriever/http"); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			cacheDir = null;
			logger.warn("Could not initialize cache directory: " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
		cacheDirectory = cacheDir;
	}

	@Override
	public boolean supports(URL url)
	{
		return "http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public RetrieverResult retrieve(URL url, IRetrieverMonitor monitor, boolean cache, boolean refresh)
			throws Exception
	{
		if (cacheDirectory == null)
		{
			cache = false;
		}

		monitor.updateStatus(RetrievalStatus.STARTED);

		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url.toString());

		if (cache)
		{
			//TODO
			int position = 0;
			if (position > 0)
			{
				method.addRequestHeader("Range", "bytes=" + position + "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}

		try
		{
			monitor.updateStatus(RetrievalStatus.CONNECTING);
			int responseCode = client.executeMethod(method);
			monitor.updateStatus(RetrievalStatus.CONNECTED);
			if (responseCode != HttpStatus.SC_OK && responseCode != HttpStatus.SC_PARTIAL_CONTENT)
			{
				// response not ok
				throw new IOException("Received " + responseCode + " when requesting url: " + url); //$NON-NLS-1$ //$NON-NLS-2$
			}

			Header contentLengthHeader = method.getResponseHeader("content-length"); //$NON-NLS-1$
			if (contentLengthHeader != null)
			{
				try
				{
					int length = Integer.parseInt(contentLengthHeader.getValue());
					monitor.setLength(length);
				}
				catch (NumberFormatException e)
				{
					logger.debug("Error parsing content length", e); //$NON-NLS-1$
				}
			}

			monitor.updateStatus(RetrievalStatus.READING);
			if (cache)
			{
				//TODO
				return new RetrieverResult(null, RetrieverResultStatus.ERROR);
			}
			else
			{
				InputStream is = null;
				try
				{
					is = new BufferedInputStream(new MonitorInputStream(method.getResponseBodyAsStream(), monitor));
					ByteBuffer buffer = WWIO.readStreamToBuffer(is);
					return new RetrieverResult(new ByteBufferRetrievalResult(buffer), RetrieverResultStatus.COMPLETE);
				}
				catch (MonitorCancelledOrPausedException e)
				{
					return new RetrieverResult(null, monitor.isPaused() ? RetrieverResultStatus.PAUSED
							: RetrieverResultStatus.CANCELED);
				}
				finally
				{
					is.close();
				}
			}
		}
		finally
		{
			method.releaseConnection();
		}
	}
}
