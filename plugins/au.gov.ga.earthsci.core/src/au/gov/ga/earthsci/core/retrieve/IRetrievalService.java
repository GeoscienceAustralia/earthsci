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
 * A service for retrieving resources.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrievalService
{
	/**
	 * Retrieve the given URL. If the URL is already currently being retrieved,
	 * the caller is added to the old {@link IRetrieval}.
	 * <p/>
	 * The {@link IRetriever} used to retrieve the URL is asked to cache the
	 * result if supported.
	 * <p/>
	 * The returned retrieval object is not automatically started;
	 * {@link IRetrieval#start()} should be called to begin retrieval.
	 * 
	 * @param caller
	 *            Object requesting the retrieval
	 * @param url
	 *            URL to retrieve
	 * @return IRetrieval used to retrieve the resource from the URL
	 */
	IRetrieval retrieve(Object caller, URL url);

	/**
	 * Retrieve the given URL. If the URL is already currently being retrieved,
	 * the caller is added to the old {@link IRetrieval}.
	 * <p/>
	 * The returned retrieval object is not automatically started;
	 * {@link IRetrieval#start()} should be called to begin retrieval.
	 * 
	 * @param caller
	 *            Object requesting the retrieval
	 * @param url
	 *            URL to retrieve
	 * @param retrievalProperties
	 *            Properties to use when retrieving the URL
	 * @return {@link IRetrieval} used to retrieve the resource from the URL
	 */
	IRetrieval retrieve(Object caller, URL url, IRetrievalProperties retrievalProperties);

	/**
	 * Get the {@link IRetrieval} that is currently retrieving the given URL, if
	 * it exists.
	 * 
	 * @param url
	 *            URL to get the retrieval for
	 * @return {@link IRetrieval} being used to retrieve the resource from the
	 *         URL, or null if it doesn't exist.
	 */
	IRetrieval getRetrieval(URL url);

	/**
	 * Get an array of {@link IRetrieval}s that the given caller has requested.
	 * 
	 * @param caller
	 *            Caller to get retrievals for
	 * @return {@link IRetrieval}s requested by the caller, empty array if none.
	 */
	IRetrieval[] getRetrievals(Object caller);
}
