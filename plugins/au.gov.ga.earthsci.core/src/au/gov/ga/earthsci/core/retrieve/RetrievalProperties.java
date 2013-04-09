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
	private boolean useCache = true;
	private boolean refreshCache = false;
	private int connectTimeout = Configuration.getIntegerValue(AVKey.URL_CONNECT_TIMEOUT, 8000);
	private int readTimeout = Configuration.getIntegerValue(AVKey.URL_READ_TIMEOUT, 5000);
	private boolean fileRequired = false;

	@Override
	public boolean isUseCache()
	{
		return useCache;
	}

	public void setUseCache(boolean useCache)
	{
		this.useCache = useCache;
	}

	@Override
	public boolean isRefreshCache()
	{
		return refreshCache;
	}

	public void setRefreshCache(boolean refreshCache)
	{
		this.refreshCache = refreshCache;
	}

	@Override
	public int getConnectTimeout()
	{
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout)
	{
		this.connectTimeout = connectTimeout;
	}

	@Override
	public int getReadTimeout()
	{
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout)
	{
		this.readTimeout = readTimeout;
	}

	@Override
	public boolean isFileRequired()
	{
		return fileRequired;
	}

	public void setFileRequired(boolean fileRequired)
	{
		this.fileRequired = fileRequired;
	}
}
