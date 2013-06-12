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

import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalListener;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;
import au.gov.ga.earthsci.core.retrieve.retriever.HttpRetrievalProperties;
import au.gov.ga.earthsci.discovery.DiscoveryIndexOutOfBoundsException;
import au.gov.ga.earthsci.discovery.DiscoveryListenerList;
import au.gov.ga.earthsci.discovery.DiscoveryResultNotFoundException;
import au.gov.ga.earthsci.discovery.IDiscovery;
import au.gov.ga.earthsci.discovery.IDiscoveryResultLabelProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryListener;
import au.gov.ga.earthsci.discovery.IDiscoveryParameters;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * {@link IDiscovery} implementation for a CSW service.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CSWDiscovery implements IDiscovery
{
	private final IDiscoveryParameters parameters;
	private final IDiscoveryService service;
	private final DiscoveryListenerList listeners = new DiscoveryListenerList();
	private final IDiscoveryResultLabelProvider labelProvider = new CSWDiscoveryResultLabelProvider();
	private int pageSize = DEFAULT_PAGE_SIZE;

	private Integer resultCount;
	private boolean loading;
	private Exception error;

	private final Map<Integer, IDiscoveryResult> results = new HashMap<Integer, IDiscoveryResult>();

	private final Map<IRetrieval, String> retrievals = new HashMap<IRetrieval, String>();
	private final Set<String> retrievalIds = new HashSet<String>();

	public CSWDiscovery(IDiscoveryParameters parameters, IDiscoveryService service)
	{
		this.parameters = parameters;
		this.service = service;
	}

	@Override
	public IDiscoveryService getService()
	{
		return service;
	}

	@Override
	public IDiscoveryParameters getParameters()
	{
		return parameters;
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
		}
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

	@Override
	public Exception getError()
	{
		return error;
	}

	@Override
	public int getResultCount()
	{
		return resultCount != null ? resultCount : 0;
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
	public int getPageSize()
	{
		return pageSize;
	}

	@Override
	public boolean supportsCustomPageSize()
	{
		return true;
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

			//TEMP
			String prefix =
					"<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" version=\"2.0.2\" service=\"CSW\" resultType=\"results\" startPosition=\""
							+ (start + 1)
							+ "\" maxRecords=\""
							+ length
							+ "\"><csw:Query typeNames=\"csw:Record\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><csw:ElementSetName>full</csw:ElementSetName><csw:Constraint version=\"1.1.0\"><ogc:Filter><ogc:And><ogc:PropertyIsLike wildCard=\"*\" escape=\"\\\" singleChar=\"?\"><ogc:PropertyName>AnyText</ogc:PropertyName><ogc:Literal>";
			String suffix =
					"</ogc:Literal></ogc:PropertyIsLike></ogc:And></ogc:Filter></csw:Constraint></csw:Query></csw:GetRecords>";
			String query = prefix + parameters.getQuery() + suffix;
			//TEMP

			try
			{
				byte[] payload = query.getBytes("UTF-8");
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
				retrievalIds.remove(id);
				loading = !retrievals.isEmpty();

				if (retrieval.getResult().isSuccessful())
				{
					try
					{
						DocumentBuilder builder = WWXML.createDocumentBuilder(false);
						Document document = builder.parse(retrieval.getData().getInputStream());

						XPath xpath = WWXML.makeXPath();
						Double totalRecordCount =
								(Double) xpath.compile("/GetRecordsResponse/SearchResults/@numberOfRecordsMatched") //$NON-NLS-1$
										.evaluate(document, XPathConstants.NUMBER);
						Double nextRecordIndex =
								(Double) xpath.compile("/GetRecordsResponse/SearchResults/@nextRecord").evaluate( //$NON-NLS-1$
										document, XPathConstants.NUMBER);
						Double numberOfRecordsReturned =
								(Double) xpath.compile("/GetRecordsResponse/SearchResults/@numberOfRecordsReturned") //$NON-NLS-1$
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
						if (!Double.isNaN(numberOfRecordsReturned) && !Double.isNaN(nextRecordIndex)
								&& numberOfRecordsReturned > 0)
						{
							int count = numberOfRecordsReturned.intValue();
							int startIndex = nextRecordIndex.intValue() - 1 - count;
							NodeList recordElements =
									(NodeList) xpath.compile("/GetRecordsResponse/SearchResults/Record").evaluate( //$NON-NLS-1$
											document, XPathConstants.NODESET);
							if (recordElements.getLength() != count)
							{
								throw new Exception(
										"Number of record elements in the CSW response doesn't match the number of records attribute");
							}
							for (int i = 0; i < count; i++)
							{
								Element recordElement = (Element) recordElements.item(i);
								int index = startIndex + i;
								CSWDiscoveryResult result = new CSWDiscoveryResult(index, recordElement);
								results.put(index, result);
								listeners.resultAdded(CSWDiscovery.this, result);
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace(); //TODO
						error = e;
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
