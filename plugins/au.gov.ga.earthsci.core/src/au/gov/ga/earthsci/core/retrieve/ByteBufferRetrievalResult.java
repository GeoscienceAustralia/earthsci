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
package au.gov.ga.earthsci.core.retrieve;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An implementation of {@link IRetrievalResult} which stores the retrieved data in a
 * ByteBuffer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ByteBufferRetrievalResult implements IRetrievalResult
{
	private final Exception error;
	private final String message;
	private final ByteBuffer buffer;
	private final boolean successful;

	/**
	 * Create a new retrieval result with the given buffer. Optionally, an exception and success flag may also be specified.
	 * 
	 * @param buffer The buffer to use to back this result
	 * @param error An optional exception to attach to this result
	 * @param successful Whether or not this represents a successful retrieval
	 */
	public ByteBufferRetrievalResult(ByteBuffer buffer, Exception error, boolean successful, String message)
	{
		this.buffer = buffer;
		this.error = error;
		this.successful = successful;
		
		if (message == null)
		{
			this.message = successful ? null : error.getLocalizedMessage();
		}
		else
		{
			this.message = message;
		}
	}
	
	/**
	 * Create a new successful retrieval result with the given buffer.
	 * 
	 * @param buffer A byte buffer containing the data for this result
	 */
	public ByteBufferRetrievalResult(ByteBuffer buffer)
	{
		this(buffer, null, true, null);
	}

	/**
	 * Create a new unsuccessful retrieval result with the given exception
	 * 
	 * @param e The exception causing the failure
	 */
	public ByteBufferRetrievalResult(Exception e)
	{
		this(null, e, false, null);
	}
	
	/**
	 * Create a new unsuccessful retrieval result with the given exception and message
	 * 
	 * @param e The exception causing the failure
	 * @param message A user friendly message to provide to the user
	 */
	public ByteBufferRetrievalResult(Exception e, String message)
	{
		this(null, e, false, message);
	}
	
	/**
	 * Create a new unsuccessful retrieval result with the given message
	 * 
	 * @param message A user friendly message to provide to the user
	 */
	public ByteBufferRetrievalResult(String message)
	{
		this(null, null, false, message);
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
		{
			return null;
		}
		return new ByteArrayInputStream(getArray());
	}

	@Override
	public String getAsString()
	{
		if (buffer == null)
		{
			return null;
		}
		return new String(getArray());
	}

	private byte[] getArray()
	{
		byte[] array;
		if (buffer.hasArray())
		{
			array = buffer.array();
		}
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
	public Exception getException()
	{
		return error;
	}
	
	@Override
	public String getMessage()
	{
		return message;
	}
	
	@Override
	public boolean isSuccessful()
	{
		return successful;
	}
}