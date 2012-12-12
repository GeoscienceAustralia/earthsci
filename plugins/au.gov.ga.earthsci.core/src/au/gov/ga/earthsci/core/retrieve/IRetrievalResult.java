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

/**
 * Result of a {@link IRetrieval} after the retrieval is completed.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrievalResult
{
	/**
	 * The success status of this retrieval.
	 * <p/>
	 * <ul>
	 * <li>If this function returns <code>true</code>, {@link #getError()} will
	 * return null.</li>
	 * <li>If this function returns <code>false</code>, {@link #getError()} will
	 * return the error that occurred.</li>
	 * </ul>
	 * 
	 * @return True if the retrieval was successful
	 */
	boolean isSuccessful();

	/**
	 * If an error occurs during resource retrieval, it is available via this
	 * method. If the retrieval was successful, this method will return null.
	 * 
	 * @return The Exception thrown during resource retrieval if there was an
	 *         error.
	 */
	Exception getError();

	/**
	 * Does this result contain data read from a cache?
	 * <p/>
	 * The cached data can be accessed using the {@link #getCachedData()}
	 * method.
	 * 
	 * @return True if this result has data read from a cache.
	 */
	boolean hasCachedData();

	/**
	 * The retrieval data if the result existed in a cache.
	 * 
	 * @return Retrieval data read from a cache.
	 */
	IRetrievalData getCachedData();

	/**
	 * The retrieval data from the remote source.
	 * <p/>
	 * If the retrieval was successful and the result was not read from a cache
	 * or the cache was updated, this will return data. Otherwise it will return
	 * null.
	 * <p/>
	 * This method will return null in the following situations:
	 * <ul>
	 * <li>Retrieval failed.</li>
	 * <li>Caching for the retrieval was enabled, and a resource existed in the
	 * cache that is up to date (ie not modified).</li>
	 * </ul>
	 * 
	 * @return The data retrieved from the remote source.
	 */
	IRetrievalData getRetrievedData();

	/**
	 * The best data to use when reading the retrieved resource. If the cached
	 * version was not modified, this returns the cached data. Otherwise it
	 * returns the retrieved data.
	 * 
	 * @return The resource data, if available.
	 */
	IRetrievalData getData();

	/**
	 * The modified state of this retrieval. If a previous result was read from
	 * a cache, and the remote resource has not been modified since the cached
	 * version was last updated, this returns true.
	 * <p/>
	 * If this returns true, the content is only accessible via the
	 * {@link #getCachedData()} method. {@link #getRetrievedData()} will return
	 * null.
	 * 
	 * @return True if the cached resource is up to date.
	 */
	boolean cacheNotModified();

	/**
	 * @return The length of the content; or the value read from the
	 *         content-length response header. -1 if unknown.
	 */
	long getContentLength();

	/**
	 * @return The value read from the content-type response header, or null if
	 *         this is unavailable.
	 */
	String getContentType();
}
