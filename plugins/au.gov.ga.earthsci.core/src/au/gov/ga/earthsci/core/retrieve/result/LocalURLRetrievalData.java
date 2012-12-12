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

import gov.nasa.worldwind.util.WWIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;

/**
 * {@link IRetrievalData} implementation that reads data from a URL using
 * {@link URL#openStream()}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LocalURLRetrievalData implements IRetrievalData
{
	private final URL url;
	private final long contentLength;
	private final String contentType;

	public LocalURLRetrievalData(URL url, long contentLength, String contentType)
	{
		this.url = url;
		this.contentLength = contentLength;
		this.contentType = contentType;
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

	@Override
	public InputStream getInputStream() throws IOException
	{
		return url.openStream();
	}

	@Override
	public ByteBuffer getByteBuffer() throws IOException
	{
		InputStream is = getInputStream();
		try
		{
			return WWIO.readStreamToBuffer(is);
		}
		finally
		{
			is.close();
		}
	}
}
