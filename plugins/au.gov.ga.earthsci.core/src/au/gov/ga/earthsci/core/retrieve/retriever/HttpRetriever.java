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
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.retrieve.IRetriever;
import au.gov.ga.earthsci.core.retrieve.IRetrieverMonitor;
import au.gov.ga.earthsci.core.retrieve.RetrievalStatus;
import au.gov.ga.earthsci.core.retrieve.RetrieverResult;
import au.gov.ga.earthsci.core.retrieve.RetrieverResultStatus;
import au.gov.ga.earthsci.core.retrieve.result.ByteBufferRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.result.URLRetrievalResult;
import au.gov.ga.earthsci.core.util.ConfigurationUtil;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * {@link IRetriever} implementation used for retrieving HTTP URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HttpRetriever implements IRetriever
{
	private final static Logger logger = LoggerFactory.getLogger(HttpRetriever.class);
	private final static File cacheDirectory;
	private final static int REDOWNLOAD_BYTES = 1024;
	private final static String INCOMPLETE_SUFFIX = ".incomplete"; //$NON-NLS-1$

	static
	{
		File cacheDir;
		try
		{
			cacheDir = ConfigurationUtil.getWorkspaceFile("retriever/http"); //$NON-NLS-1$
			//cacheDir = new File("c:/Temp/HTTPCache");
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
		monitor.updateStatus(RetrievalStatus.STARTED);

		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection) url.openConnection();

			File cacheFile = null, cacheIncompleteFile = null;
			if (cache)
			{
				cacheFile = getCacheFile(url);
				cacheIncompleteFile = getCacheIncompleteFile(cacheFile);
				if (cacheFile == null)
				{
					cache = false;
				}
			}

			long position = 0;
			if (cache)
			{
				if (cacheFile.exists())
				{
					if (refresh)
					{
						cacheFile.delete();
					}
					else
					{
						//TODO return cached result to user here, but still check the server; 
						//if the server responds not modified, tell the user
						//if the server responds modified, download, cache, and give the data to the user
						connection.setIfModifiedSince(cacheFile.lastModified());
					}
				}

				if (cacheIncompleteFile.exists())
				{
					if (refresh)
					{
						cacheIncompleteFile.delete();
					}
					else
					{
						position = Math.max(0, cacheIncompleteFile.length() - REDOWNLOAD_BYTES);
					}
				}
				if (position > 0)
				{
					connection.setRequestProperty("Range", "bytes=" + position + "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}

			monitor.updateStatus(RetrievalStatus.CONNECTING);
			connection.connect();
			int responseCode = connection.getResponseCode();
			monitor.updateStatus(RetrievalStatus.CONNECTED);

			if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED)
			{
				return new RetrieverResult(new URLRetrievalResult(cacheFile.toURI().toURL()),
						RetrieverResultStatus.COMPLETE);
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
				monitor.setLength(position + contentLength);
			}
			if (position > 0)
			{
				monitor.setPosition(position);
			}

			monitor.updateStatus(RetrievalStatus.READING);
			InputStream is = null;
			try
			{
				is = new BufferedInputStream(new MonitorInputStream(connection.getInputStream(), monitor));
				if (cache)
				{
					writeInputStreamToFile(is, cacheIncompleteFile, position);
					cacheIncompleteFile.renameTo(cacheFile);
					return new RetrieverResult(new URLRetrievalResult(cacheFile.toURI().toURL()),
							RetrieverResultStatus.COMPLETE);
				}
				else
				{
					ByteBuffer buffer = WWIO.readStreamToBuffer(is);
					return new RetrieverResult(new ByteBufferRetrievalResult(buffer), RetrieverResultStatus.COMPLETE);
				}
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
		finally
		{
			connection.disconnect();
		}
	}

	private static File getCacheFile(URL url)
	{
		if (cacheDirectory == null)
		{
			return null;
		}
		return new File(cacheDirectory, filenameForURL(url));
	}

	private static String filenameForURL(URL url)
	{
		String filename;
		if (!Util.isBlank(url.getHost()) && !Util.isBlank(url.getPath()))
		{
			filename = fixForFilename(url.getHost()) + File.separator + fixForFilename(url.getPath());
			if (url.getQuery() != null)
			{
				filename += "#" + url.getQuery(); //$NON-NLS-1$
			}
		}
		else
		{
			filename = fixForFilename(url.toExternalForm());
		}
		return filename;
	}

	private static String fixForFilename(String s)
	{
		// need to replace the following invalid filename characters: \/:*?"<>|
		// replace them with exclamation points, because that is cool
		return s.replaceAll("!", "!!").replaceAll("[\\/:*?\"<>|]", "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	private static File getCacheIncompleteFile(File cacheFile)
	{
		if (cacheFile == null)
		{
			return null;
		}
		return new File(cacheFile.getParent(), cacheFile.getName() + INCOMPLETE_SUFFIX);
	}

	private static void writeInputStreamToFile(InputStream is, File output, long startPosition) throws IOException
	{
		if (!output.getParentFile().exists())
		{
			output.getParentFile().mkdirs();
		}
		RandomAccessFile raf = new RandomAccessFile(output, "rw"); //$NON-NLS-1$
		output.setReadable(true, false);
		output.setWritable(true, false);
		try
		{
			raf.seek(startPosition);
			byte[] page = new byte[8096];
			int len;
			while ((len = is.read(page)) >= 0)
			{
				raf.write(page, 0, len);
			}
		}
		finally
		{
			raf.close();
		}
	}

	public static void main(String[] args) throws Exception
	{
		System.setProperty("java.net.useSystemProxies", "true");

		IRetrieverMonitor monitor = new IRetrieverMonitor()
		{
			@Override
			public void updateStatus(RetrievalStatus status)
			{
				System.out.println("Status changed to: " + status);
			}

			@Override
			public void setPosition(long position)
			{
				System.out.println("Position updated: " + position);
			}

			@Override
			public void setLength(long length)
			{
				System.out.println("Length updated: " + length);
			}

			@Override
			public void progress(long amount)
			{
				System.out.println("Progressed: " + amount);
			}

			@Override
			public boolean isPaused()
			{
				return false;
			}

			@Override
			public boolean isCanceled()
			{
				return false;
			}
		};

		//URL url = new URL("http://ict.icrar.org/store/staff/derek/whd/anims/dkg-whd-scivis-music-1080p.mov");
		URL url = new URL("http://www.cloudsourced.com/wp-content/uploads/2010/01/londonlanduse.jpg");
		//URL url = new URL("http://www.ga.gov.au");

		HttpRetriever retriever = new HttpRetriever();
		retriever.retrieve(url, monitor, true, false);
	}
}
