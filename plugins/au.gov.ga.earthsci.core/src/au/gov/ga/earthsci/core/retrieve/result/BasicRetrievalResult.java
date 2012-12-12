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

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;

/**
 * An {@link IRetrievalResult} representing a successful retrieval.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicRetrievalResult implements IRetrievalResult
{
	private final IRetrievalData cachedData;
	private final boolean cacheNotModified;
	private final IRetrievalData retrievedData;
	private final long contentLength;
	private final String contentType;

	/**
	 * Create a new result.
	 * 
	 * @param cachedData
	 *            Resource data read from a cache
	 * @param cacheNotModified
	 *            True if the retrieved data would not modify the cache (ie the
	 *            cache is up to date)
	 * @param retrievedData
	 *            Retrieved remote resource data
	 * @param contentLength
	 *            Content length of the data
	 * @param contentType
	 *            Content type of the data
	 */
	public BasicRetrievalResult(IRetrievalData cachedData, boolean cacheNotModified, IRetrievalData retrievedData,
			long contentLength, String contentType)
	{
		this.cachedData = cachedData;
		this.cacheNotModified = cacheNotModified;
		this.retrievedData = retrievedData;
		this.contentLength = contentLength;
		this.contentType = contentType;
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
		return cachedData != null;
	}

	@Override
	public boolean cacheNotModified()
	{
		return cacheNotModified;
	}

	@Override
	public IRetrievalData getCachedData()
	{
		return cachedData;
	}

	@Override
	public IRetrievalData getRetrievedData()
	{
		return retrievedData;
	}

	@Override
	public IRetrievalData getData()
	{
		return cachedData != null && cacheNotModified() ? cachedData : retrievedData;
	}

	@Override
	public long getContentLength()
	{
		return contentLength;
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}
}
