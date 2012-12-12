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

import java.net.URL;

/**
 * An object that can retrieve a resource from a supported URL.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetriever
{
	/**
	 * Does this retriever know how to retrieve a resource from the given URL?
	 * 
	 * @param url
	 *            URL to test
	 * @return True if this retriever can retrieve a resource from the URL
	 */
	boolean supports(URL url);

	/**
	 * Check this retriever's cache for a cached resource for the given URL.
	 * 
	 * @param url
	 *            URL to check the cache for
	 * @return A result if the cached resource exists, or null.
	 */
	IRetrievalData checkCache(URL url);

	/**
	 * Retrieve the resource from the given URL, and return a result. The
	 * retrieval should be performed synchronously on the calling thread.
	 * <p/>
	 * The returned result cannot be null; however, the {@link IRetrievalResult}
	 * in the return value can be null if the monitor indicated that the
	 * retrieval should be paused or cancelled.
	 * 
	 * @param url
	 *            URL to retrieve
	 * @param monitor
	 *            Monitor to update during retrieval
	 * @param cache
	 *            Should the retrieved resource be cached?
	 * @param refresh
	 *            Should a cached resource be refreshed?
	 * @param cachedData
	 *            The data returned from an earlier call to
	 *            {@link #checkCache(URL)}, for inserting into the returned
	 *            result
	 * @return The result of the retrieval, cannot be null.
	 * @throws Exception
	 *             If the retrieval could not be completed
	 */
	RetrieverResult retrieve(URL url, IRetrieverMonitor monitor, boolean cache, boolean refresh,
			IRetrievalData cachedData) throws Exception;
}
