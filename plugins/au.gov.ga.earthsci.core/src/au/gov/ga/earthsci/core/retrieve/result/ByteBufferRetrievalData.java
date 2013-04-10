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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;

/**
 * {@link IRetrievalData} implementation containing the retrieved resource in
 * memory in the form of a {@link ByteBuffer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ByteBufferRetrievalData extends AbstractRetrievalData
{
	private final ByteBuffer buffer;

	public ByteBufferRetrievalData(URL url, ByteBuffer buffer, String contentType)
	{
		super(url, buffer.limit(), contentType);
		this.buffer = buffer;
	}

	@Override
	public InputStream getInputStream()
	{
		if (buffer.hasArray())
		{
			return new ByteArrayInputStream(buffer.array(), 0, buffer.limit());
		}
		synchronized (buffer)
		{
			byte[] array = new byte[buffer.limit()];
			buffer.rewind();
			buffer.get(array);
			return new ByteArrayInputStream(array);
		}
	}

	@Override
	public ByteBuffer getByteBuffer()
	{
		synchronized (buffer)
		{
			buffer.rewind();
			return buffer.slice();
		}
	}
}
