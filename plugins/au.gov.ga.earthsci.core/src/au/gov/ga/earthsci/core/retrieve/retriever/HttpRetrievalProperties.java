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
package au.gov.ga.earthsci.core.retrieve.retriever;

import au.gov.ga.earthsci.core.retrieve.RetrievalProperties;

/**
 * Basic implementation of {@link IHttpRetrievalProperties}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HttpRetrievalProperties extends RetrievalProperties implements IHttpRetrievalProperties
{
	private String requestMethod;
	private byte[] requestPayload;
	private String contentType;

	@Override
	public String getRequestMethod()
	{
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod)
	{
		this.requestMethod = requestMethod;
	}

	@Override
	public byte[] getRequestPayload()
	{
		return requestPayload;
	}

	public void setRequestPayload(byte[] requestPayload)
	{
		this.requestPayload = requestPayload;
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
}
