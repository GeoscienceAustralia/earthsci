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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper {@link IRetrievalListener} that contains a list of listeners that this
 * object delegates to.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CompoundRetrievalListener implements IRetrievalListener
{
	private final List<IRetrievalListener> listeners = new ArrayList<IRetrievalListener>();

	public void addListener(IRetrievalListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}

	public void removeListener(IRetrievalListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}

	private IRetrievalListener[] copy()
	{
		synchronized (listeners)
		{
			return listeners.toArray(new IRetrievalListener[listeners.size()]);
		}
	}

	@Override
	public void statusChanged(IRetrieval retrieval)
	{
		for (IRetrievalListener listener : copy())
		{
			listener.statusChanged(retrieval);
		}
	}

	@Override
	public void progress(IRetrieval retrieval)
	{
		for (IRetrievalListener listener : copy())
		{
			listener.progress(retrieval);
		}
	}

	@Override
	public void cached(IRetrieval retrieval)
	{
		for (IRetrievalListener listener : copy())
		{
			listener.cached(retrieval);
		}
	}

	@Override
	public void complete(IRetrieval retrieval)
	{
		for (IRetrievalListener listener : copy())
		{
			listener.complete(retrieval);
		}
	}

	@Override
	public void paused(IRetrieval retrieval)
	{
		for (IRetrievalListener listener : copy())
		{
			listener.paused(retrieval);
		}
	}

	@Override
	public void callersChanged(IRetrieval retrieval)
	{
		for (IRetrievalListener listener : copy())
		{
			listener.callersChanged(retrieval);
		}
	}
}
