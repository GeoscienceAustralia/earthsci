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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.osgi.util.NLS;

import au.gov.ga.earthsci.core.retrieve.ByteBufferRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetrievalMonitor;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetriever;

/**
 * An implementation of {@link IRetriever} that supports HTTP URLs
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class HTTPRetriever implements IRetriever
{

	private static final String HTTP_PROTOCOL = "http"; //$NON-NLS-1$
	private static final String HTTPS_PROTOCOL = "https"; //$NON-NLS-1$
	
	@Inject
	private Logger logger;
	
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
	public IRetrievalResult retrieve(URL url, IRetrievalMonitor monitor)
	{
		if (!supports(url))
		{
			throw new IllegalArgumentException(getClass() + " does not support the URL: " + url); //$NON-NLS-1$
		}
		
		monitor.notifyStarted();

		HttpURLConnection connection = null;
		try
		{
			monitor.notifyConnecting();
			connection = openConnection(url);
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
			{
				throw new IOException("Server responded with error " + connection.getResponseCode() + " '" + connection.getResponseMessage() + "' for URL " + url); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			
			connection.connect();
			monitor.notifyConnected();
			
			monitor.notifyReading();
			ByteBuffer buffer = readFromConnection(connection);
			
			monitor.notifyCompleted(true);
			
			return new ByteBufferRetrievalResult(buffer);
		}
		catch (Exception e)
		{
			if (logger != null)
			{
				logger.debug(e, "Exception during retrieval of resource at URL " + url); //$NON-NLS-1$
			}
			
			monitor.notifyCompleted(false);
			
			try
			{
				if (connection == null || connection.getResponseCode() == HttpURLConnection.HTTP_OK)
				{
					return new ByteBufferRetrievalResult(e);
				}
				else
				{
					return new ByteBufferRetrievalResult(e, NLS.bind(Messages.HTTPRetriever_ServerErrorMessage, 
																	 new Object[] {connection.getResponseCode(), connection.getResponseMessage(), url}));
				}
			}
			catch (Exception e2)
			{
				return new ByteBufferRetrievalResult(e);
			}
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}
	
	private HttpURLConnection openConnection(URL url) throws Exception
	{
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		return  connection;
	}
	
	private ByteBuffer readFromConnection(HttpURLConnection connection) throws Exception
	{
		 if (connection.getContentLength() < 1)
        {
            return readFromConnectionWithUnknownContentLength(connection);
        }
		else
		{
			return readFromConnectionWithKnownContentLength(connection);
		}
	}

	private ByteBuffer readFromConnectionWithKnownContentLength(HttpURLConnection connection) throws IOException
	{
		ReadableByteChannel channel = Channels.newChannel(connection.getInputStream());
        ByteBuffer buffer = ByteBuffer.allocate(connection.getContentLength());

        int numBytesRead = 0;
        while (!Thread.currentThread().isInterrupted() && numBytesRead >= 0 && numBytesRead < buffer.limit())
        {
            int count = channel.read(buffer);
            if (count > 0)
            {
                numBytesRead += count;
            }
            if (count < 0)
			{
				throw new IllegalStateException("Premature end of stream from server. Expected " + connection.getContentLength() + " bytes but got " + numBytesRead); //$NON-NLS-1$ //$NON-NLS-2$
			}
        }

        if (buffer != null)
		{
			buffer.flip();
		}

        return buffer;
	}
	
	private ByteBuffer readFromConnectionWithUnknownContentLength(HttpURLConnection connection) throws Exception
	{
		final int pageSize = (int) Math.ceil(Math.pow(2, 15));

        ReadableByteChannel channel = Channels.newChannel(connection.getInputStream());
        ByteBuffer buffer = ByteBuffer.allocate(pageSize);

        int totalRead = 0;
        while (!Thread.currentThread().isInterrupted())
        {
        	int read = channel.read(buffer);
            if (read <= 0)
            {
            	break;
            }
            
            totalRead += read;
            
            // Expand the buffer if we haven't reached the end of the stream
            if (read > 0 && !buffer.hasRemaining())
            {
                ByteBuffer biggerBuffer = ByteBuffer.allocate(buffer.limit() + pageSize);
                biggerBuffer.put((ByteBuffer) buffer.rewind());
                buffer = biggerBuffer;
            }
        }

        if (buffer != null)
		{
			buffer.flip();
			buffer.limit(totalRead);
			
			// Trim the buffer to correct size
			if (totalRead < buffer.capacity())
			{
				ByteBuffer correctBuffer = ByteBuffer.allocate(totalRead);
				correctBuffer.put((ByteBuffer)buffer.rewind());
				buffer = correctBuffer;
			}
		}

        return buffer;
	}
	
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}
}
