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

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link RetrievalPostProcessor} which notifies at least one
 * {@link RetrievalHandler} passing it the {@link RetrievalResult}. Multiple
 * {@link RetrievalHandler}s can be added, and each one will be notified when
 * the result is downloaded.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HandlerPostProcessor implements RetrievalPostProcessor
{
	private URL sourceURL;
	private Object lock = new Object();
	private final List<RetrievalHandler> handlers = new ArrayList<RetrievalHandler>();
	private RetrievalResult result = null;

	/**
	 * @param sourceURL
	 *            Download URL
	 * @param handler
	 *            Default {@link RetrievalHandler} to notify when this is run
	 */
	public HandlerPostProcessor(URL sourceURL, RetrievalHandler handler)
	{
		this.sourceURL = sourceURL;
		handlers.add(handler);
	}

	/**
	 * Add a {@link RetrievalHandler} to call when this is run. If this has
	 * already been run (ie already has a result), handler will be notified
	 * immediately in the caller's thread.
	 * 
	 * @param handler
	 *            {@link RetrievalHandler} to add
	 */
	public void addHandler(RetrievalHandler handler)
	{
		synchronized (lock)
		{
			if (result == null)
				//result has not been calculated yet, so wait for run() to be called
				handlers.add(handler);
			else
				handler.handle(result);
		}
	}

	@Override
	public ByteBuffer run(Retriever retriever)
	{
		Exception error = null;
		boolean notModified = false;
		if (retriever instanceof ExtendedRetriever)
		{
			ExtendedRetriever er = (ExtendedRetriever) retriever;
			error = er.getError();
			notModified = er.isNotModified();
		}

		ByteBuffer buffer = retriever.getBuffer();
		int size;
		synchronized (lock)
		{
			//synchronized with the addHandler method to ensure thread safety
			result =
					new ByteBufferRetrievalResult(sourceURL, buffer, false, notModified, error,
							retriever.getContentType());
			size = handlers.size();
		}
		//iterate through handlers, ending at size (which was calculated in the synchronzied
		//block), so that any added in addHandler() in the meantime will not be notified (they
		//will instead be notified immediately by addHandler())
		for (int i = 0; i < size; i++)
			handlers.get(i).handle(result);
		return buffer;
	}
}
