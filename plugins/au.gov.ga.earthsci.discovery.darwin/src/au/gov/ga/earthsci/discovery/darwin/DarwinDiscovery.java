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
package au.gov.ga.earthsci.discovery.darwin;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalListener;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;
import au.gov.ga.earthsci.core.retrieve.retriever.HttpRetrievalProperties;
import au.gov.ga.earthsci.discovery.AbstractDiscovery;
import au.gov.ga.earthsci.discovery.DiscoveryIndexOutOfBoundsException;
import au.gov.ga.earthsci.discovery.DiscoveryResultNotFoundException;
import au.gov.ga.earthsci.discovery.IDiscovery;
import au.gov.ga.earthsci.discovery.IDiscoveryParameters;
import au.gov.ga.earthsci.discovery.IDiscoveryResultLabelProvider;

/**
 * {@link IDiscovery} implementation for DARWIN.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DarwinDiscovery extends AbstractDiscovery<DarwinDiscoveryService, IDiscoveryParameters>
{
	private final DarwinDiscoveryResultLabelProvider labelProvider = new DarwinDiscoveryResultLabelProvider();
	private final Map<Integer, DarwinDiscoveryResult> results = new HashMap<Integer, DarwinDiscoveryResult>();

	private final Map<IRetrieval, String> retrievals = new HashMap<IRetrieval, String>();
	private final Map<IRetrieval, Integer> retrievalStarts = new HashMap<IRetrieval, Integer>();
	private final Set<String> retrievalIds = new HashSet<String>();

	public DarwinDiscovery(DarwinDiscoveryService service, IDiscoveryParameters parameters)
	{
		super(service, parameters);
	}

	@Override
	public IDiscoveryResultLabelProvider getLabelProvider()
	{
		return labelProvider;
	}

	@Override
	public void start()
	{
		retrieve(0, 0);
	}

	@Override
	public void cancel()
	{
		synchronized (retrievals)
		{
			for (IRetrieval retrieval : retrievals.keySet())
			{
				retrieval.cancel();
			}
			retrievals.clear();
			retrievalIds.clear();
			retrievalStarts.clear();
		}
	}

	@Override
	public DarwinDiscoveryResult getResult(int index) throws DiscoveryResultNotFoundException,
			DiscoveryIndexOutOfBoundsException
	{
		synchronized (retrievals)
		{
			if (index < 0 || (resultCount != null && index >= resultCount))
			{
				throw new DiscoveryIndexOutOfBoundsException();
			}

			if (results.containsKey(index))
			{
				return results.get(index);
			}

			int pageSize = getPageSize();
			int start = (index / pageSize) * pageSize;
			retrieve(start, pageSize);

			return null;
		}
	}

	@Override
	public boolean supportsCustomPageSize()
	{
		return true;
	}

	protected void retrieve(int start, int length)
	{
		synchronized (retrievals)
		{
			String id = start + "_" + length; //$NON-NLS-1$

			if (retrievalIds.contains(id))
			{
				//already retrieving this range
				return;
			}

			HttpRetrievalProperties retrievalProperties = new HttpRetrievalProperties();
			retrievalProperties.setRequestMethod("POST"); //$NON-NLS-1$
			retrievalProperties.setUseCache(false);
			retrievalProperties.setContentType("application/json"); //$NON-NLS-1$

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("searchTerm", parameters.getQuery()); //$NON-NLS-1$
			jsonObject.put("startPosition", start); //$NON-NLS-1$
			jsonObject.put("count", length); //$NON-NLS-1$
			String json = jsonObject.toString();

			try
			{
				byte[] payload = json.getBytes("UTF-8"); //$NON-NLS-1$
				retrievalProperties.setRequestPayload(payload);
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}

			IRetrieval retrieval =
					RetrievalServiceFactory.getServiceInstance().retrieve(this, service.getServiceURL(),
							retrievalProperties, true);
			retrievalIds.add(id);
			retrievals.put(retrieval, id);
			retrievalStarts.put(retrieval, start);
			retrieval.addListener(retrievalListener);
			retrieval.start();
			loading = true;
		}
	}

	private IRetrievalListener retrievalListener = new RetrievalAdapter()
	{
		@Override
		public void complete(IRetrieval retrieval)
		{
			synchronized (retrievals)
			{
				String id = retrievals.remove(retrieval);
				if (id == null)
				{
					return;
				}
				retrievalIds.remove(id);
				int startIndex = retrievalStarts.remove(retrieval);
				loading = !retrievals.isEmpty();

				if (retrieval.getResult().isSuccessful())
				{
					InputStream is = null;
					try
					{
						is = retrieval.getData().getInputStream();
						JSONTokener tokener = new JSONTokener(is);
						JSONObject json = new JSONObject(tokener);

						int totalResultCount = json.getInt("numberOfRecordsMatched"); //$NON-NLS-1$
						int numberOfRecordsReturned = json.getInt("numberOfRecordsReturned"); //$NON-NLS-1$

						if (resultCount == null || resultCount != totalResultCount)
						{
							resultCount = totalResultCount;
							listeners.resultCountChanged(DarwinDiscovery.this);
						}
						if (numberOfRecordsReturned > 0)
						{
							JSONArray results = json.optJSONArray("searchResults"); //$NON-NLS-1$
							if (results == null)
							{
								throw new Exception("JSON response doesn't contain searchResults array"); //$NON-NLS-1$
							}
							else if (results.length() != numberOfRecordsReturned)
							{
								throw new Exception(
										"Number of record elements in the JSON response doesn't match the number of records attribute"); //$NON-NLS-1$
							}
							for (int i = 0; i < numberOfRecordsReturned; i++)
							{
								JSONObject resultJson = results.getJSONObject(i);
								int index = startIndex + i;
								DarwinDiscoveryResult result =
										new DarwinDiscoveryResult(DarwinDiscovery.this, index, resultJson);
								DarwinDiscovery.this.results.put(index, result);
								listeners.resultAdded(DarwinDiscovery.this, result);
							}
						}
					}
					catch (Exception e)
					{
						error = e;
						e.printStackTrace();
					}
					finally
					{
						if (is != null)
						{
							try
							{
								is.close();
							}
							catch (IOException e)
							{
							}
						}
					}
				}
				else if (error == null)
				{
					error = retrieval.getResult().getError();
				}
			}
		}
	};
}
