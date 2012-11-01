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
import java.net.URL;
import java.net.URLConnection;
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
 * A base implementation for {@link IRetriever}s
 * <p/>
 * Provides a {@link #doRetrieve(URL, IRetrievalMonitor)} method that uses a basic lifecycle for URL resource retrieval.
 * Subclasses can optionally override lifecycle hooks to change the behaviour of each step in the process, or 
 * can override {@link #doRetrieve(URL, IRetrievalMonitor)} itself to change the lifecycle entirely. 
 * <p/>
 * The available lifecycle hooks are:
 * <ul>
 * 	<li>{@link #openConnection(URL)}
 *  <li>{@link #validateConnection(URLConnection)}
 *  <li>{@link #getMessageForInvalidConnection(URLConnection)}
 *  <li>{@link #readFromConnection(URLConnection)}
 *  <li>{@link #getMessageForRetrievalException(URLConnection, Exception)}
 *  <li>{@link #doCleanup(URLConnection)}
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractRetriever implements IRetriever
{

	@Inject
	private Logger logger;
	
	@Override
	public final IRetrievalResult retrieve(URL url, IRetrievalMonitor monitor)
	{
		if (url == null || !supports(url))
		{
			throw new IllegalArgumentException(getClass() + " does not support the URL: " + url); //$NON-NLS-1$
		}
		if (monitor == null)
		{
			throw new IllegalArgumentException("A monitor is required for retrieval"); //$NON-NLS-1$
		}
		
		return doRetrieve(url, monitor);
	}
	
	/**
	 * Perform the actual retrieval. URL is guaranteed to be supported and non-null.
	 * <p/>
	 * The default implementation provides the following lifecycle:
	 * <ol>
	 * 	<li>open connection
	 *  <li>validate connection
	 *  <li>connect
	 *  <li>read from connection
	 *  <li>perform cleanup
	 *  <li>return result
	 * </ol>
	 * <p/>
	 * This implementation also provides error handling mechanisms as appropriate.
	 * <p/>
	 * Subclasses can either override this method to implement their own lifecycle, or override the lifecycle hooks provided
	 * to handle specific tasks within the lifecycle.
	 * 
	 * @param url The url to retrieve from
	 * @param monitor The progress monitor to report progress to
	 */
	protected IRetrievalResult doRetrieve(URL url, IRetrievalMonitor monitor)
	{
		monitor.notifyStarted();

		URLConnection connection = null;
		try
		{
			monitor.notifyConnecting();
			connection = openConnection(url);
			if (!validateConnection(connection))
			{
				return new ByteBufferRetrievalResult(getMessageForInvalidConnection(connection));
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
			if (getLogger() != null)
			{
				getLogger().debug(e, "Exception during retrieval of resource at URL " + url); //$NON-NLS-1$
			}
			
			monitor.notifyCompleted(false);
			
			try
			{
				return new ByteBufferRetrievalResult(e, getMessageForRetrievalException(connection, e));
			}
			catch (Exception e2)
			{
				return new ByteBufferRetrievalResult(e);
			}
		}
		finally
		{
			doCleanup(connection);
		}
	}

	/**
	 * Open a connection to the provided URL
	 * 
	 * @param url The url to connect to
	 */
	protected URLConnection openConnection(URL url) throws Exception
	{
		return url.openConnection();
	}
	
	/**
	 * Validate that the provided connection is valid.
	 * <p/>
	 * Subclasses may override to validate specific response headers etc.
	 * 
	 * @param connection The connection to validate
	 * 
	 * @return <code>true</code> if the connection is in a valid state for reading, <code>false</code> otherwise
	 */
	protected boolean validateConnection(URLConnection connection) throws Exception
	{
		return true;
	}

	/**
	 * Provide a message regarding an invalid connection. This message may be displayed to the user
	 * and should provide enough information to correct the problem if appropriate.
	 * 
	 * @param connection The invalid connection
	 * 
	 * @return A message to display to the user regarding the invalid connection.
	 */
	protected String getMessageForInvalidConnection(URLConnection connection) throws Exception
	{
		return NLS.bind(Messages.AbstractRetriever_GenericInvalidConnectionMessage, connection.getURL());
	}
	
	/**
	 * Provide a message regarding the provided exception encountered during retrieval. This
	 * message may be displayed to the user and should provide enough information to correct
	 * the problem if available.
	 * 
	 * @param connection The connection used for retrieval
	 * @param e The exception encountered
	 * 
	 * @return The mssage to display to the user regarding the invalid connection
	 */
	protected String getMessageForRetrievalException(URLConnection connection, Exception e)
	{
		return e.getLocalizedMessage();
	}
	
	/**
	 * Perform required connection cleanup.
	 * <p/>
	 * Subclasses should override this method as required to close connections etc. as appropriate.
	 */
	protected void doCleanup(URLConnection connection)
	{
		// Subclasses override to perform connection cleanups as required
	}
	
	/**
	 * Read content from the provided connection and return the result as a byte buffer.
	 * <p/>
	 * The result buffer should contain as much of the content as possible (in the case of a failed read), and
	 * should be sized to the content length (in the case of a successful read).
	 * 
	 * @param connection The connection to read from
	 * 
	 * @return A buffer containing the contents read from the provided connection
	 */
	protected ByteBuffer readFromConnection(URLConnection connection) throws Exception
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

	private ByteBuffer readFromConnectionWithKnownContentLength(URLConnection connection) throws IOException
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
	
	private ByteBuffer readFromConnectionWithUnknownContentLength(URLConnection connection) throws Exception
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
	
	public Logger getLogger()
	{
		return logger;
	}
}
