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
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.util.collection.HashSetAndArray;
import au.gov.ga.earthsci.core.util.collection.HashSetAndArrayHashMap;
import au.gov.ga.earthsci.core.util.collection.SetAndArray;
import au.gov.ga.earthsci.core.util.collection.SetAndArrayMap;

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

	@Override
	public IRetrieval retrieve(Object caller, URL url)
	{
		return retrieve(caller, url, true, false);
	}

	@Override
	public IRetrieval retrieve(Object caller, URL url, boolean cache, boolean refresh)
	{
		if (url == null)
		{
			throw new NullPointerException("Retrieval URL is null"); //$NON-NLS-1$
		}

		synchronized (urlToRetrieval)
		{
			Retrieval retrieval = urlToRetrieval.get(url);
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
				retrieval = new Retrieval(caller, url, cache, refresh, retriever);
				urlToRetrieval.put(url, retrieval);

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
			return retrieval;
		}
	}

	private void removeRetrieval(IRetrieval retrieval)
	{
		synchronized (urlToRetrieval)
		{
			urlToRetrieval.remove(retrieval.getURL());
			Object[] callers = retrieval.getCallers();
			for (Object caller : callers)
			{
				callerToRetrievals.removeSingle(caller, retrieval);
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
			return retrievals.getArray();
		}
	}
}
