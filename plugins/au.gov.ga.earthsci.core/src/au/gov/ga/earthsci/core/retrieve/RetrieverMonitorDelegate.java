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

import java.io.Closeable;

/**
 * Delegate class for an {@link IRetrieverMonitor}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RetrieverMonitorDelegate implements IRetrieverMonitor
{
	protected final IRetrieverMonitor delegate;

	public RetrieverMonitorDelegate(IRetrieverMonitor delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void updateStatus(RetrievalStatus status)
	{
		delegate.updateStatus(status);
	}

	@Override
	public void progress(long amount)
	{
		delegate.progress(amount);
	}

	@Override
	public void setPosition(long position)
	{
		delegate.setPosition(position);
	}

	@Override
	public void setLength(long length)
	{
		delegate.setLength(length);
	}

	@Override
	public boolean isCanceled()
	{
		return delegate.isCanceled();
	}

	@Override
	public boolean isPaused()
	{
		return delegate.isPaused();
	}

	@Override
	public void setCloseable(Closeable closeable)
	{
		delegate.setCloseable(closeable);
	}
}
