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
package au.gov.ga.earthsci.core.retrieve.retriever;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.osgi.util.NLS;

import au.gov.ga.earthsci.core.retrieve.IRetriever;

/**
 * An implementation of {@link IRetriever} that supports HTTP URLs
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class HTTPRetriever extends AbstractRetriever implements IRetriever
{

	public static final String HTTP_PROTOCOL = "http"; //$NON-NLS-1$
	public static final String HTTPS_PROTOCOL = "https"; //$NON-NLS-1$
	
	@Override
	public boolean supports(URL url)
	{
		if (url == null)
		{
			return false;
		}
		
		return HTTP_PROTOCOL.equalsIgnoreCase(url.getProtocol()) || 
				HTTPS_PROTOCOL.equalsIgnoreCase(url.getProtocol()); 
	}

	
	@Override
	protected boolean validateConnection(URLConnection connection) throws Exception
	{
		HttpURLConnection httpConnection = (HttpURLConnection) connection;
		if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
		{
			return false;
			
		}
		return true;
	}
	
	@Override
	protected String getMessageForInvalidConnection(URLConnection connection) throws Exception
	{
		HttpURLConnection httpConnection = (HttpURLConnection) connection;
		return NLS.bind(Messages.HTTPRetriever_ServerErrorMessage, new Object[] {httpConnection.getResponseCode(), httpConnection.getResponseMessage(), connection.getURL()});
	}
	
	@Override
	protected String getMessageForRetrievalException(URLConnection connection, Exception e)
	{
		return e.getLocalizedMessage();
	}
	
	@Override
	protected void doCleanup(URLConnection connection)
	{
		if (connection != null)
		{
			((HttpURLConnection)connection).disconnect();
		}
	}
	
}
