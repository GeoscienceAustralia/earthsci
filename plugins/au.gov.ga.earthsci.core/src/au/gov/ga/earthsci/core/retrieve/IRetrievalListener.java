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
 * Object that listens for changes to the {@link IRetrievalState}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrievalListener
{
	/**
	 * Fired when the retrieval status changes.
	 * 
	 * @param retrieval
	 *            Retrieval for which the status changed.
	 */
	void statusChanged(IRetrieval retrieval);

	/**
	 * Fired when the retrieval progresses.
	 * 
	 * @param retrieval
	 *            Retrieval that progressed.
	 */
	void progress(IRetrieval retrieval);

	/**
	 * Fired when data for the retrieval is available from a cache.
	 * <p/>
	 * {@link #complete(IRetrieval)} will still be fired once the retrieval is
	 * complete.
	 * <p/>
	 * This will be called even if the retrieval has been started with
	 * refresh=true; however, the cached version will be refreshed once the
	 * retrieval is complete.
	 * 
	 * @param retrieval
	 *            Retrieval that has cached data.
	 * @see IRetrieval#getCachedData()
	 */
	void cached(IRetrieval retrieval);

	/**
	 * Fired when the retrieval is complete (either successfully, with an error,
	 * or cancelled).
	 * <p/>
	 * Not called if the retrieval was paused (until it is resumed and
	 * completed).
	 * 
	 * @param retrieval
	 *            Retrieval that completed.
	 */
	void complete(IRetrieval retrieval);

	/**
	 * Fired when the retrieval is paused.
	 * 
	 * @param retrieval
	 *            Retrieval that paused.
	 */
	void paused(IRetrieval retrieval);
}
