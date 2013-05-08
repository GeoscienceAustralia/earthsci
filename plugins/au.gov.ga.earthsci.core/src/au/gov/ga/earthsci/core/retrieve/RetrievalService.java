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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.collection.ArrayListHashMap;
import au.gov.ga.earthsci.common.collection.HashSetAndArray;
import au.gov.ga.earthsci.common.collection.HashSetAndArrayHashMap;
import au.gov.ga.earthsci.common.collection.ListMap;
import au.gov.ga.earthsci.common.collection.SetAndArray;
import au.gov.ga.earthsci.common.collection.SetAndArrayMap;

/**
 * Basic implementation of {@link IRetrievalService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
@Creatable
public class RetrievalService implements IRetrievalService
{
	private final static Logger logger = LoggerFactory.getLogger(RetrievalService.class);

	@Inject
	private IRetrieverFactory retrieverFactory;

	private final Map<URL, Retrieval> urlToRetrieval = new HashMap<URL, Retrieval>();
	private final SetAndArrayMap<Object, IRetrieval> callerToRetrievals =
			new HashSetAndArrayHashMap<Object, IRetrieval>();
	private final SetAndArray<IRetrieval> EMPTY_RETRIEVAL_COLLECTION = new HashSetAndArray<IRetrieval>();

	private final List<IRetrievalServiceListener> listeners = new ArrayList<IRetrievalServiceListener>();
	private final ListMap<Object, IRetrievalServiceListener> callerListeners =
			new ArrayListHashMap<Object, IRetrievalServiceListener>();

	@PreDestroy
	public void cancelAll()
	{
		synchronized (urlToRetrieval)
		{
			Collection<Retrieval> retrievals = urlToRetrieval.values();
			for (Retrieval retrieval : retrievals)
			{
				retrieval.cancel();
			}
		}
	}

	@Override
	public IRetrieval retrieve(Object caller, URL url)
	{
		return retrieve(caller, url, new RetrievalProperties());
	}

	@Override
	public IRetrieval retrieve(Object caller, URL url, IRetrievalProperties retrievalProperties)
	{
		return retrieve(caller, url, retrievalProperties, false);
	}

	@Override
	public IRetrieval retrieve(Object caller, URL url, IRetrievalProperties retrievalProperties,
			boolean ignoreDuplicates)
	{
		if (url == null)
		{
			throw new NullPointerException("Retrieval URL is null"); //$NON-NLS-1$
		}

		synchronized (urlToRetrieval)
		{
			Retrieval retrieval = ignoreDuplicates ? null : urlToRetrieval.get(url);
			if (retrieval == null)
			{
				//create a retriever to retrieve the url
				IRetriever retriever = retrieverFactory.getRetriever(url);
				if (retriever == null)
				{
					logger.error("Unsupported retrieval URL: " + url); //$NON-NLS-1$
					return null;
				}

				//create a retrieval object
				retrieval = new Retrieval(caller, url, retrievalProperties, retriever);
				if (!ignoreDuplicates)
				{
					urlToRetrieval.put(url, retrieval);
				}
				fireRetrievalAdded(retrieval);

				//add a listener to remove the retrieval after it's complete
				retrieval.addListener(new RetrievalAdapter()
				{
					@Override
					public void complete(IRetrieval retrieval)
					{
						retrieval.removeListener(this);
						removeRetrieval(retrieval);
					}
				});
			}
			else
			{
				retrieval.addCaller(caller);
			}
			callerToRetrievals.putSingle(caller, retrieval);
			fireRetrievalAdded(caller, retrieval);
			return retrieval;
		}
	}

	private void removeRetrieval(IRetrieval retrieval)
	{
		synchronized (urlToRetrieval)
		{
			urlToRetrieval.remove(retrieval.getURL());
			fireRetrievalRemoved(retrieval);
			Object[] callers = retrieval.getCallers();
			for (Object caller : callers)
			{
				callerToRetrievals.removeSingle(caller, retrieval);
				fireRetrievalRemoved(caller, retrieval);
			}
		}
	}

	@Override
	public IRetrieval getRetrieval(URL url)
	{
		synchronized (urlToRetrieval)
		{
			return urlToRetrieval.get(url);
		}
	}

	@Override
	public IRetrieval[] getRetrievals(Object caller)
	{
		synchronized (urlToRetrieval)
		{
			SetAndArray<IRetrieval> retrievals = callerToRetrievals.get(caller);
			if (retrievals == null)
			{
				retrievals = EMPTY_RETRIEVAL_COLLECTION;
			}
			return retrievals.getArray(IRetrieval.class);
		}
	}

	@Override
	public void addListener(IRetrievalServiceListener listener)
	{
		synchronized (listeners)
		{
			synchronized (urlToRetrieval)
			{
				listeners.add(listener);

				//notify the newly added listener of all current retrievals:
				for (Retrieval retrieval : urlToRetrieval.values())
				{
					listener.retrievalAdded(retrieval);
				}
			}
		}
	}

	@Override
	public void addListener(IRetrievalServiceListener listener, Object caller)
	{
		synchronized (callerListeners)
		{
			synchronized (urlToRetrieval)
			{
				callerListeners.putSingle(caller, listener);

				//notify the newly added listener of all current retrievals for this caller:
				for (Retrieval retrieval : urlToRetrieval.values())
				{
					for (Object retrievalCaller : retrieval.getCallers())
					{
						if (retrievalCaller.equals(caller))
						{
							listener.retrievalAdded(retrieval);
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void removeListener(IRetrievalServiceListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}

	@Override
	public void removeListener(IRetrievalServiceListener listener, Object caller)
	{
		synchronized (callerListeners)
		{
			callerListeners.removeSingle(caller, listener);
		}
	}

	private void fireRetrievalAdded(IRetrieval retrieval)
	{
		synchronized (listeners)
		{
			for (int i = listeners.size() - 1; i >= 0; i--)
			{
				listeners.get(i).retrievalAdded(retrieval);
			}
		}
	}

	private void fireRetrievalAdded(Object caller, IRetrieval retrieval)
	{
		synchronized (callerListeners)
		{
			List<IRetrievalServiceListener> listeners = callerListeners.get(caller);
			if (listeners != null)
			{
				for (int i = listeners.size() - 1; i >= 0; i--)
				{
					listeners.get(i).retrievalAdded(retrieval);
				}
			}
		}
	}

	private void fireRetrievalRemoved(IRetrieval retrieval)
	{
		synchronized (listeners)
		{
			for (int i = listeners.size() - 1; i >= 0; i--)
			{
				listeners.get(i).retrievalRemoved(retrieval);
			}
		}
	}

	private void fireRetrievalRemoved(Object caller, IRetrieval retrieval)
	{
		synchronized (callerListeners)
		{
			List<IRetrievalServiceListener> listeners = callerListeners.get(caller);
			if (listeners != null)
			{
				for (int i = listeners.size() - 1; i >= 0; i--)
				{
					listeners.get(i).retrievalRemoved(retrieval);
				}
			}
		}
	}
}
