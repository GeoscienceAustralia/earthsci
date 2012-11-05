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

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import au.gov.ga.earthsci.worldwind.common.util.URLUtil;

/**
 * Extension of {@link FileRetriever} which implements {@link ExtendedRetriever}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExtendedFileRetriever extends FileRetriever implements ExtendedRetriever
{
	private Long ifModifiedSince;
	private Exception error;
	private boolean notModified = false;
	private boolean unzip;

	public ExtendedFileRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor)
	{
		this(url, ifModifiedSince, postProcessor, true);
	}

	public ExtendedFileRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor, boolean unzip)
	{
		super(url, postProcessor);
		this.ifModifiedSince = ifModifiedSince;
		this.unzip = unzip;
	}

	@Override
	protected ByteBuffer doRead(URLConnection connection) throws Exception
	{
		//overridden to catch exceptions and set the notModified flag

		try
		{
			if (ifModifiedSince != null)
			{
				notModified = checkIfModified(connection.getURL(), ifModifiedSince);
				if (notModified)
					return null;
			}

			return super.doRead(connection);
		}
		catch (Exception e)
		{
			error = e;
			throw e;
		}
	}

	protected boolean checkIfModified(URL url, long ifModifiedSince)
	{
		File file = URLUtil.urlToFile(url);
		return file != null && file.exists() && file.lastModified() <= ifModifiedSince;
	}

	@Override
	public Exception getError()
	{
		return error;
	}

	@Override
	public boolean isNotModified()
	{
		return notModified;
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
