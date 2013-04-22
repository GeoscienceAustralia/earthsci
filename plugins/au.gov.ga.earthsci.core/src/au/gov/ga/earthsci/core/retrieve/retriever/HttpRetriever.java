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
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetriever;
import au.gov.ga.earthsci.core.retrieve.IRetrieverMonitor;
import au.gov.ga.earthsci.core.retrieve.RetrievalStatus;
import au.gov.ga.earthsci.core.retrieve.RetrieverResult;
import au.gov.ga.earthsci.core.retrieve.RetrieverResultStatus;
import au.gov.ga.earthsci.core.retrieve.cache.FileURLCache;
import au.gov.ga.earthsci.core.retrieve.cache.IURLCache;
import au.gov.ga.earthsci.core.retrieve.result.BasicRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.result.ByteBufferRetrievalData;
import au.gov.ga.earthsci.core.retrieve.result.FileRetrievalData;
import au.gov.ga.earthsci.core.retrieve.result.URLCacheRetrievalData;
import au.gov.ga.earthsci.core.util.ConfigurationUtil;

/**
 * {@link IRetriever} implementation used for retrieving HTTP URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HttpRetriever implements IRetriever
{
	private final static Logger logger = LoggerFactory.getLogger(HttpRetriever.class);
	private final static IURLCache urlCache;
	private final static int REDOWNLOAD_BYTES = 1024;

	static
	{
		File cacheDir;
		try
		{
			cacheDir = ConfigurationUtil.getWorkspaceFile("retriever/http"); //$NON-NLS-1$
			//cacheDir = new File("c:/Temp/HTTPCache");
			logger.info("Initialized http cache directory: " + cacheDir); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			cacheDir = null;
			logger.warn("Could not initialize http cache directory: " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
		urlCache = cacheDir == null ? null : new FileURLCache(cacheDir);
	}

	@Override
	public boolean supports(URL url)
	{
		return "http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public IRetrievalData checkCache(URL url)
	{
		if (urlCache.isComplete(url))
		{
			return new URLCacheRetrievalData(urlCache, url);
		}
		return null;
	}

	@Override
	public RetrieverResult retrieve(URL url, IRetrieverMonitor monitor, IRetrievalProperties retrievalProperties,
			IRetrievalData cachedData) throws Exception
	{
		monitor.updateStatus(RetrievalStatus.STARTED);

		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(retrievalProperties.getConnectTimeout());
			connection.setReadTimeout(retrievalProperties.getReadTimeout());

			final HttpURLConnection closeableConnection = connection;
			monitor.setCloseable(new Closeable()
			{
				@Override
				public void close() throws IOException
				{
					closeableConnection.disconnect();
				}
			});

			checkMonitor(monitor);

			long position = 0;
			if (retrievalProperties.isUseCache())
			{
				//if the resource has a cached version, set the if modified header
				if (cachedData != null)
				{
					if (!retrievalProperties.isRefreshCache())
					{
						connection.setIfModifiedSince(urlCache.getLastModified(url));
					}
				}

				//if the resource is partially cached, set the range header to resume the download
				if (urlCache.isPartial(url))
				{
					if (!retrievalProperties.isRefreshCache())
					{
						position = Math.max(0, urlCache.getPartialLength(url) - REDOWNLOAD_BYTES);
					}
				}

				if (position > 0)
				{
					connection.setRequestProperty("Range", "bytes=" + position + "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}

			checkMonitor(monitor);

			//connect
			monitor.updateStatus(RetrievalStatus.CONNECTING);
			connection.connect();

			checkMonitor(monitor);

			//read the response code
			int responseCode = connection.getResponseCode();
			monitor.updateStatus(RetrievalStatus.CONNECTED);

			checkMonitor(monitor);

			if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED)
			{
				return new RetrieverResult(new BasicRetrievalResult(cachedData, true), RetrieverResultStatus.COMPLETE);
			}
			else if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL)
			{
				// response not ok
				throw new IOException("Received " + responseCode + " when requesting url: " + url); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (responseCode != HttpURLConnection.HTTP_PARTIAL)
			{
				position = 0;
			}

			int contentLength = connection.getContentLength();
			if (contentLength >= 0)
			{
				contentLength += position;
				monitor.setLength(contentLength);
			}
			if (position > 0)
			{
				monitor.setPosition(position);
			}

			String contentType = connection.getContentType();
			long lastModified = connection.getLastModified();

			checkMonitor(monitor);

			monitor.updateStatus(RetrievalStatus.READING);
			InputStream is = null;
			try
			{
				IRetrievalData retrievedData;
				is = new BufferedInputStream(new MonitorInputStream(connection.getInputStream(), monitor));
				if (retrievalProperties.isUseCache())
				{
					OutputStream os = null;
					try
					{
						os = urlCache.writePartial(url, position);
						Util.writeInputStreamToOutputStream(is, os);
					}
					finally
					{
						if (os != null)
						{
							os.close();
						}
					}
					boolean updated = urlCache.writeComplete(url, lastModified, contentType);
					if (!updated)
					{
						return new RetrieverResult(new BasicRetrievalResult(cachedData, true),
								RetrieverResultStatus.COMPLETE);
					}
					else
					{
						retrievedData = new URLCacheRetrievalData(urlCache, url);
					}
				}
				else if (retrievalProperties.isFileRequired())
				{
					String prefix = getClass().getSimpleName();
					String suffix = Util.getExtension(url.getPath());
					suffix = suffix != null ? suffix : ""; //$NON-NLS-1$
					File file = Util.writeInputStreamToTemporaryFile(is, prefix, suffix);
					retrievedData = new FileRetrievalData(file, contentType);
				}
				else
				{
					ByteBuffer buffer = WWIO.readStreamToBuffer(is);
					retrievedData = new ByteBufferRetrievalData(url, buffer, contentType);
				}
				IRetrievalResult result = new BasicRetrievalResult(retrievedData, false);
				return new RetrieverResult(result, RetrieverResultStatus.COMPLETE);
			}
			finally
			{
				if (is != null)
				{
					is.close();
				}
			}
		}
		catch (MonitorCancelledOrPausedException e)
		{
			return new RetrieverResult(null, monitor.isPaused() ? RetrieverResultStatus.PAUSED
					: RetrieverResultStatus.CANCELED);
		}
		finally
		{
			connection.disconnect();
		}
	}

	private void checkMonitor(IRetrieverMonitor monitor) throws MonitorCancelledOrPausedException
	{
		if (monitor.isCanceled() || monitor.isPaused())
		{
			throw new MonitorCancelledOrPausedException();
		}
	}
}
