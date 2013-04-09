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
 * Collection of properties for an {@link IRetrieval}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrievalProperties
{
	/**
	 * Whether the retriever should check a cache for a result. If this is true,
	 * and a result is retrieved from a remote source, this also tells the
	 * retriever to cache the retrieved result.
	 * <p/>
	 * If this is true, and a cached result exists, the retriever should still
	 * check the remote source for an updated version (if the retrieval protocol
	 * supports this).
	 * 
	 * @return Should the retriever use a cache?
	 */
	boolean isUseCache();

	/**
	 * Whether a cached result should be forced to be refreshed by the resource
	 * at the remote end.
	 * <p/>
	 * This value is only used if {@link #isUseCache()} is <code>true</code>.
	 * 
	 * @return Should a cached result be refreshed?
	 */
	boolean isRefreshCache();

	/**
	 * @return The connect timeout to use when retrieving.
	 */
	int getConnectTimeout();

	/**
	 * @return The read timeout to use when retrieving.
	 */
	int getReadTimeout();

	/**
	 * Whether the result should be saved to a file, so that the caller can
	 * treat the retrieved resource as a file.
	 * <p/>
	 * This is useful if there is some requirement for the result to be a File;
	 * for example, passing the result to an external library that supports
	 * Files only.
	 * 
	 * @return Should the retriever save the result to a file?
	 */
	boolean isFileRequired();
}
