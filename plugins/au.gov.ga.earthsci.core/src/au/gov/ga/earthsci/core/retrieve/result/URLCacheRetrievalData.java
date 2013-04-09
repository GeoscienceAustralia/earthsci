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
package au.gov.ga.earthsci.core.retrieve.result;

import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.cache.IURLCache;

/**
 * {@link IRetrievalData} implementation that reads data from an
 * {@link IURLCache}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URLCacheRetrievalData implements IRetrievalData
{
	private final IURLCache cache;
	private final URL url;

	public URLCacheRetrievalData(IURLCache cache, URL url)
	{
		this.cache = cache;
		this.url = url;
	}

	@Override
	public long getContentLength()
	{
		return cache.getLength(url);
	}

	@Override
	public String getContentType()
	{
		return cache.getContentType(url);
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return cache.read(url);
	}

	@Override
	public ByteBuffer getByteBuffer() throws IOException
	{
		InputStream is = getInputStream();
		try
		{
			return WWIO.readStreamToBuffer(is);
		}
		finally
		{
			is.close();
		}
	}

	@Override
	public File getFile()
	{
		return cache.getFile(url);
	}
}
