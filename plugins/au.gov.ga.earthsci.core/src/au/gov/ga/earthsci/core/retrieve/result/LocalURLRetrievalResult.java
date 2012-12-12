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
package au.gov.ga.earthsci.core.retrieve.result;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;

/**
 * {@link IRetrievalResult} used for local URLs, such as file and resource URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LocalURLRetrievalResult implements IRetrievalResult
{
	private Exception error = null;
	private long contentLength = -1;
	private String contentType = null;
	private final IRetrievalData data;

	public LocalURLRetrievalResult(URL url)
	{
		//try opening a connection to this url to see if it is valid
		try
		{
			URLConnection connection = url.openConnection();
			connection.connect();
			contentLength = connection.getContentLength();
			contentType = connection.getContentType();

			//try and call a disconnect method if it exists via reflection
			try
			{
				Method disconnect = connection.getClass().getMethod("disconnect"); //$NON-NLS-1$
				disconnect.invoke(connection);
			}
			catch (Exception e)
			{
				//ignore reflection invoked disconnect method errors
			}
		}
		catch (Exception e)
		{
			error = e;
		}
		finally
		{
		}

		this.data = error == null ? new LocalURLRetrievalData(url) : null;
	}

	@Override
	public boolean isSuccessful()
	{
		return error == null;
	}

	@Override
	public Exception getError()
	{
		return error;
	}

	@Override
	public boolean hasCachedData()
	{
		return false;
	}

	@Override
	public IRetrievalData getCachedData()
	{
		return null;
	}

	@Override
	public IRetrievalData getRetrievedData()
	{
		return data;
	}

	@Override
	public IRetrievalData getData()
	{
		return data;
	}

	@Override
	public boolean cacheNotModified()
	{
		return false;
	}

	@Override
	public long getContentLength()
	{
		return contentLength;
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}
}
