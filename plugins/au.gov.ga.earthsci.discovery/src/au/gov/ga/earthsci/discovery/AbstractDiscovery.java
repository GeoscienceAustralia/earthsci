/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.discovery;

/**
 * Abstract implementation of {@link IDiscovery}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractDiscovery<S extends IDiscoveryService, P extends IDiscoveryParameters> implements
		IDiscovery
{
	protected final DiscoveryListenerList listeners = new DiscoveryListenerList();
	protected final S service;
	protected final P parameters;
	protected boolean loading = false;
	protected Exception error;
	protected Integer resultCount;
	protected int pageSize = DEFAULT_PAGE_SIZE;

	public AbstractDiscovery(S service, P parameters)
	{
		this.service = service;
		this.parameters = parameters;
	}

	@Override
	public S getService()
	{
		return service;
	}

	@Override
	public P getParameters()
	{
		return parameters;
	}

	@Override
	public void addListener(IDiscoveryListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeListener(IDiscoveryListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public boolean isLoading()
	{
		return loading;
	}

	protected void setLoading(boolean loading)
	{
		this.loading = loading;
	}

	@Override
	public Exception getError()
	{
		return error;
	}

	protected void setError(Exception error)
	{
		this.error = error;
	}

	@Override
	public int getResultCount()
	{
		return resultCount != null ? resultCount : 0;
	}

	protected void setResultCount(Integer resultCount)
	{
		this.resultCount = resultCount;
	}

	@Override
	public int getPageSize()
	{
		return pageSize;
	}

	@Override
	public int getCustomPageSize()
	{
		return pageSize;
	}

	@Override
	public void setCustomPageSize(int customPageSize)
	{
		this.pageSize = Math.max(1, customPageSize);
	}
}
