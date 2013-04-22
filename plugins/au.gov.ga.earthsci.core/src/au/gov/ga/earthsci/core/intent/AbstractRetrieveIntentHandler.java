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
package au.gov.ga.earthsci.core.intent;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;

/**
 * Abstract superclass of IntentHandlers that use the IRetrievalService system
 * for retrieving the Intent resource data before processing.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractRetrieveIntentHandler implements IIntentHandler
{
	@Inject
	private IRetrievalService retrievalService;

	@Override
	public void handle(final Intent intent, final IIntentCallback callback)
	{
		try
		{
			final URL url = getRetrievalURL(intent);
			if (url == null)
			{
				throw new IllegalArgumentException("Intent URL is null"); //$NON-NLS-1$
			}

			IRetrieval retrieval = retrievalService.retrieve(this, url);
			retrieval.addListener(new RetrievalAdapter()
			{
				@Override
				public void cached(IRetrieval retrieval)
				{
					handle(retrieval.getCachedData(), url, intent, callback);
				}

				@Override
				public void complete(IRetrieval retrieval)
				{
					IRetrievalResult result = retrieval.getResult();
					if (result.isSuccessful())
					{
						if (!result.isFromCache())
						{
							handle(result.getData(), url, intent, callback);
						}
					}
					else
					{
						callback.error(result.getError(), intent);
					}
				}
			});
			retrieval.start();
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

	/**
	 * Return the URL to retrieve for the given intent. Returns
	 * {@link Intent#getURL()} by default. Subclasses can override this method
	 * to return custom URLs.
	 * 
	 * @param intent
	 *            Intent to get the URL for
	 * @return URL to retrieve for the intent
	 * @throws MalformedURLException
	 */
	protected URL getRetrievalURL(Intent intent) throws MalformedURLException
	{
		return intent.getURL();
	}

	/**
	 * Handle the retrieved data.
	 * <p/>
	 * It is possible that this could be called twice, once for a cached version
	 * of the data, and once for the updated data.
	 * <p/>
	 * Must notify the callback at least once when completed (or failed).
	 * 
	 * @param data
	 *            Retrieved data
	 * @param URL
	 *            the data was retrieved from
	 * @param intent
	 *            Intent for which the data was retrieved
	 * @param callback
	 *            Intent callback to notify once the data has been handled
	 */
	protected abstract void handle(IRetrievalData data, URL url, Intent intent, IIntentCallback callback);
}
