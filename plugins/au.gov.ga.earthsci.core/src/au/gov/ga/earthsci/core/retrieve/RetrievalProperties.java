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
package au.gov.ga.earthsci.core.retrieve;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

/**
 * Basic {@link IRetrievalProperties} implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RetrievalProperties implements IRetrievalProperties
{
	private final boolean useCache;
	private final boolean refreshCache;
	private final int connectTimeout;
	private final int readTimeout;
	private final boolean fileRequired;

	/**
	 * Create a new properties object with the default values (cache = true,
	 * refresh = false, timeouts read from the WorldWind {@link Configuration}
	 * class).
	 */
	public RetrievalProperties()
	{
		this(true, false);
	}

	/**
	 * Create a new properties object with control over the caching properties.
	 * 
	 * @param useCache
	 *            Value to return from {@link #isUseCache()}
	 * @param refreshCache
	 *            Value to return from {@link #isRefreshCache()}
	 */
	public RetrievalProperties(boolean useCache, boolean refreshCache)
	{
		this(useCache, refreshCache, Configuration.getIntegerValue(AVKey.URL_CONNECT_TIMEOUT, 8000), Configuration
				.getIntegerValue(AVKey.URL_READ_TIMEOUT, 5000));
	}

	/**
	 * Create a new properties object.
	 * 
	 * @param useCache
	 *            Value to return from {@link #isUseCache()}
	 * @param refreshCache
	 *            Value to return from {@link #isRefreshCache()}
	 * @param connectTimeout
	 *            Value to return from {@link #getConnectTimeout()}
	 * @param readTimeout
	 *            Value to return from {@link #getReadTimeout()}
	 */
	public RetrievalProperties(boolean useCache, boolean refreshCache, int connectTimeout, int readTimeout)
	{
		this(useCache, refreshCache, connectTimeout, readTimeout, false);
	}

	/**
	 * Create a new properties object.
	 * 
	 * @param useCache
	 *            Value to return from {@link #isUseCache()}
	 * @param refreshCache
	 *            Value to return from {@link #isRefreshCache()}
	 * @param connectTimeout
	 *            Value to return from {@link #getConnectTimeout()}
	 * @param readTimeout
	 *            Value to return from {@link #getReadTimeout()}
	 * @param fileRequired
	 *            Value to return from {@link #isFileRequired()}
	 */
	public RetrievalProperties(boolean useCache, boolean refreshCache, int connectTimeout, int readTimeout,
			boolean fileRequired)
	{
		this.useCache = useCache;
		this.refreshCache = refreshCache;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.fileRequired = fileRequired;
	}

	@Override
	public boolean isUseCache()
	{
		return useCache;
	}

	@Override
	public boolean isRefreshCache()
	{
		return refreshCache;
	}

	@Override
	public int getConnectTimeout()
	{
		return connectTimeout;
	}

	@Override
	public int getReadTimeout()
	{
		return readTimeout;
	}

	@Override
	public boolean isFileRequired()
	{
		return fileRequired;
	}
}
