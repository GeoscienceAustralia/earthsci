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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.retrieve.JarRetriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.WWUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.RetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;
import au.gov.ga.earthsci.worldwind.common.retrieve.RetrievalListenerHelper;

/**
 * {@link URLRetriever} that performs URL retrieval using the
 * {@link IRetrievalService} instead of performing its own retrieving.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URLRetrieverWrapper extends JarRetriever
{
	//XXX unfortunately the AbstractRetrievalPostProcessor's validateResponseCode method checks
	//for certain subclasses, so we have to extend JarRetriever and return expected results for
	//the getResponseCode method

	private final URLRetriever wrapped;

	private int responseCode = -1;
	private String responseMessage = "FAILED"; //$NON-NLS-1$

	public URLRetrieverWrapper(URLRetriever wrapped)
	{
		super(wrapped.getUrl(), wrapped.getPostProcessor());
		this.wrapped = wrapped;
	}

	@Override
	protected URLConnection openConnection() throws IOException
	{
		return null;
	}

	@Override
	protected ByteBuffer read() throws Exception
	{
		Object caller = RetrievalListenerHelper.getLayer(wrapped);
		if (caller == null)
		{
			caller = wrapped;
		}
		IRetrievalService service = RetrievalServiceFactory.getServiceInstance();
		RetrievalProperties retrievalProperties = new RetrievalProperties();
		retrievalProperties.setConnectTimeout(getConnectTimeout());
		retrievalProperties.setReadTimeout(getReadTimeout());
		retrievalProperties.setUseCache(false);
		IRetrieval retrieval = service.retrieve(caller, getUrl(), retrievalProperties);
		retrieval.start();
		IRetrievalResult result = retrieval.waitAndGetResult();

		//TODO handle case when result is null (job manager shut down? cancelled download?)

		if (result != null)
		{
			IRetrievalData data = result.getData();
			if (data != null)
			{
				//set the response properties so that validateResponseCode succeeds:
				this.responseCode = HttpURLConnection.HTTP_OK;
				this.responseMessage = "OK"; //$NON-NLS-1$

				this.contentType = data.getContentType();
				this.contentLength = (int) data.getContentLength();
				this.setContentLengthRead(this.contentLength);

				//see URLRetriever:
				if ("application/zip".equalsIgnoreCase(contentType) && !WWUtil.isEmpty(this.getValue(EXTRACT_ZIP_ENTRY))) //$NON-NLS-1$
				{
					InputStream is = data.getInputStream();
					try
					{
						return readZipStream(is, getUrl());
					}
					finally
					{
						is.close();
					}
				}

				return data.getByteBuffer();
			}
			else if (result.getError() != null)
			{
				throw result.getError();
			}
		}
		else if (retrieval.isCanceled())
		{
			return null;
		}
		throw new IllegalStateException("Could not retrieve url: " + getUrl()); //$NON-NLS-1$
	}

	@Override
	public int getResponseCode()
	{
		return responseCode;
	}

	@Override
	public String getResponseMessage()
	{
		return responseMessage;
	}
}
