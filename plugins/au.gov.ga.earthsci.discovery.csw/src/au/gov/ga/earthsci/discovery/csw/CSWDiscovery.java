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
package au.gov.ga.earthsci.discovery.csw;

import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryResultLabelProvider;

/**
 * {@link IDiscovery} implementation for a CSW service.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CSWDiscovery extends AbstractDiscovery<CSWDiscoveryService, IDiscoveryParameters>
{
	private final IDiscoveryResultLabelProvider labelProvider = new CSWDiscoveryResultLabelProvider();
	private final Map<Integer, IDiscoveryResult> results = new HashMap<Integer, IDiscoveryResult>();

	private final Map<IRetrieval, String> retrievals = new HashMap<IRetrieval, String>();
	private final Map<IRetrieval, Integer> retrievalStarts = new HashMap<IRetrieval, Integer>();
	private final Set<String> retrievalIds = new HashSet<String>();

	public CSWDiscovery(CSWDiscoveryService service, IDiscoveryParameters parameters)
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
	public IDiscoveryResult getResult(int index) throws DiscoveryResultNotFoundException,
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

			CSWRequestParameters requestParameters = new CSWRequestParameters();
			requestParameters.any = parameters.getQuery();
			String query = service.getFormat().generateRequest(requestParameters, start + 1, length);

			try
			{
				byte[] payload = query.getBytes("UTF-8"); //$NON-NLS-1$
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
						String transformed = service.getFormat().transformResponse(is);

						if (transformed != null)
						{
							DocumentBuilder builder = WWXML.createDocumentBuilder(false);
							Document document = builder.parse(new InputSource(new StringReader(transformed)));

							XPath xpath = WWXML.makeXPath();
							Double totalRecordCount =
									(Double) xpath.compile("/GetRecordsResponse/numberOfRecordsMatched") //$NON-NLS-1$
											.evaluate(document, XPathConstants.NUMBER);
							Double numberOfRecordsReturned =
									(Double) xpath.compile("/GetRecordsResponse/numberOfRecordsReturned") //$NON-NLS-1$
											.evaluate(document, XPathConstants.NUMBER);
							if (!Double.isNaN(totalRecordCount))
							{
								int newResultCount = totalRecordCount.intValue();
								if (resultCount == null || resultCount != newResultCount)
								{
									resultCount = newResultCount;
									listeners.resultCountChanged(CSWDiscovery.this);
								}
							}
							if (!Double.isNaN(numberOfRecordsReturned) && numberOfRecordsReturned > 0)
							{
								int count = numberOfRecordsReturned.intValue();
								NodeList recordElements =
										(NodeList) xpath.compile("/GetRecordsResponse/Record").evaluate( //$NON-NLS-1$
												document, XPathConstants.NODESET);
								if (recordElements.getLength() != count)
								{
									throw new Exception(
											"Number of record elements in the CSW response doesn't match the number of records attribute"); //$NON-NLS-1$
								}
								for (int i = 0; i < count; i++)
								{
									Element recordElement = (Element) recordElements.item(i);
									int index = startIndex + i;
									CSWDiscoveryResult result =
											new CSWDiscoveryResult(CSWDiscovery.this, index, recordElement);
									results.put(index, result);
									listeners.resultAdded(CSWDiscovery.this, result);
								}
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
