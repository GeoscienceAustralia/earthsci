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
package au.gov.ga.earthsci.worldwind.common.downloader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * An implementation of RetrievalResult which stores the downloaded data in a
 * ByteBuffer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ByteBufferRetrievalResult implements RetrievalResult
{
	private final URL sourceURL;
	private final Exception error;
	private final ByteBuffer buffer;
	private final boolean fromCache;
	private final boolean notModified;
	private final String contentType;

	public ByteBufferRetrievalResult(URL sourceURL, ByteBuffer buffer, boolean fromCache,
			boolean notModified, Exception error, String contentType)
	{
		this.sourceURL = sourceURL;
		this.buffer = buffer;
		this.fromCache = fromCache;
		this.notModified = notModified;
		this.error = error;
		this.contentType = contentType;
	}

	@Override
	public URL getSourceURL()
	{
		return sourceURL;
	}

	@Override
	public ByteBuffer getAsBuffer()
	{
		return buffer;
	}

	@Override
	public InputStream getAsInputStream()
	{
		if (buffer == null)
			return null;
		return new ByteArrayInputStream(getArray());
	}

	@Override
	public String getAsString()
	{
		if (buffer == null)
			return null;
		return new String(getArray());
	}

	private byte[] getArray()
	{
		byte[] array;
		if (buffer.hasArray())
			array = buffer.array();
		else
		{
			array = new byte[buffer.limit()];
			buffer.rewind();
			buffer.get(array);
		}
		return array;
	}

	@Override
	public boolean hasData()
	{
		return buffer != null;
	}

	@Override
	public boolean isFromCache()
	{
		return fromCache;
	}

	@Override
	public boolean isNotModified()
	{
		return notModified;
	}
	
	@Override
	public String getContentType()
	{
		return contentType;
	}

	@Override
	public Exception getError()
	{
		return error;
	}
}
