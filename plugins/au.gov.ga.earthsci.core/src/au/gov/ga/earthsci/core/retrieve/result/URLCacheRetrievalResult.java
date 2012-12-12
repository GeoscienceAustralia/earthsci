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

import java.net.URL;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.cache.IURLCache;

/**
 * {@link IRetrievalResult} implementation that reads data from an
 * {@link IURLCache}. Only contains cached data, not retrieved data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URLCacheRetrievalResult implements IRetrievalResult
{
	private final IURLCache cache;
	private final URL url;
	private final IRetrievalData cachedData;

	public URLCacheRetrievalResult(IURLCache cache, URL url)
	{
		this(cache, url, new URLCacheRetrievalData(cache, url));
	}

	public URLCacheRetrievalResult(IURLCache cache, URL url, IRetrievalData cachedData)
	{
		this.cache = cache;
		this.url = url;
		this.cachedData = cachedData;
	}

	@Override
	public boolean isSuccessful()
	{
		return true;
	}

	@Override
	public Exception getError()
	{
		return null;
	}

	@Override
	public boolean hasCachedData()
	{
		return true;
	}

	@Override
	public IRetrievalData getCachedData()
	{
		return cachedData;
	}

	@Override
	public IRetrievalData getRetrievedData()
	{
		return null;
	}

	@Override
	public IRetrievalData getData()
	{
		return cachedData;
	}

	@Override
	public boolean cacheNotModified()
	{
		return true;
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
}
