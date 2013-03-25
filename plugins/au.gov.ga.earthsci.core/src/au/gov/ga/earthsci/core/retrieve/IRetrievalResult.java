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
	 * Whether this data's content was read from a cached version of the
	 * resource. If there's no cached version of the resource, this returns
	 * false.
	 * <p/>
	 * If a previous result was read from a cache, and the remote resource has
	 * not been modified since the cached version was last updated, the cached
	 * version is not updated, and this returns true.
	 * <p/>
	 * If this method returns false, either the resource was not available in
	 * the cache, or the cached version was updated by this retrieval. The
	 * latest resource data is available via {@link #getData()}.
	 * <p/>
	 * If the retrieval caller has already used a version of the resource read
	 * from the cache, and this returns true, then there's no reason to use the
	 * data in this result.
	 * 
	 * @return True if this data is from a cache.
	 */
	boolean isFromCache();

	/**
	 * The best, most up-to-date data to use when reading the retrieved
	 * resource. If a cached version is available and was not modified, this
	 * returns the cached data. Otherwise it returns the retrieved data if
	 * available.
	 * 
	 * @return The resource data, if available.
	 */
	IRetrievalData getData();
}
