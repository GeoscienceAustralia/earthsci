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

import au.gov.ga.earthsci.core.util.IPropertyChangeBean;

/**
 * Represents the retrieval of a resource.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrieval extends IPropertyChangeBean
{
	/** Represents an unknown resource length. */
	public final static long UNKNOWN_LENGTH = -1;

	/**
	 * @return The URL being retrieved.
	 */
	URL getURL();

	/**
	 * @return The objects that have requested this resource be retrieved.
	 */
	Object[] getCallers();

	/**
	 * @return The current status of the retrieval.
	 */
	RetrievalStatus getStatus();

	/**
	 * @return The current position of the resource retrieval.
	 */
	long getPosition();

	/**
	 * @return The total length of the resource being retrieved. Returns
	 *         {@link #UNKNOWN_LENGTH} if unknown.
	 */
	long getLength();

	/**
	 * The percentage progress of the resource retrieval, between 0 and 1
	 * inclusive. Calculated by {@link #getPosition()} / {@link #getLength()}.
	 * If the length is unknown, returns -1.
	 * 
	 * @return The progress of the resource retrieval.
	 */
	float getPercentage();

	/**
	 * Add an object to listen for changes to this retrieval.
	 * 
	 * @param listener
	 *            Listener to add
	 */
	void addListener(IRetrievalListener listener);

	/**
	 * Remove an object that was listening for changes to this retrieval.
	 * 
	 * @param listener
	 *            Listener to remove
	 */
	void removeListener(IRetrievalListener listener);

	/**
	 * Start this retrieval, or resume from a paused state. No effect if this
	 * retrieval is already running.
	 */
	void start();

	/**
	 * Pause this retrieval. The {@link IRetriever} performing the retrieval
	 * must support pausing for this to have any effect.
	 */
	void pause();

	/**
	 * Cancel this retrieval. The {@link IRetriever} performing the retrieval
	 * must support cancellation for this to have any effect.
	 */
	void cancel();

	/**
	 * Get the cached retrieval data from a cache if available.
	 * 
	 * @return Result from a cache, or null if no cached version is available or
	 *         the cache hasn't yet been checked.
	 * @see IRetrievalListener#cached(IRetrieval)
	 */
	IRetrievalData getCachedData();

	/**
	 * Get the result after the retrieval has completed.
	 * 
	 * @return Retrieval result, or null if the retrieval isn't yet complete or
	 *         was canceled before completion.
	 */
	IRetrievalResult getResult();

	/**
	 * Get the result after the retrieval has completed. If the retrieval is not
	 * yet complete, this method will wait. Can return null if the retrieval is
	 * canceled or hasn't yet been started.
	 * <p/>
	 * If the retrieval is paused, this method will not return until the
	 * retrieval is resumed and completed.
	 * 
	 * @return Retrieval result.
	 * @throws InterruptedException
	 *             If the thread was interrupted while waiting for the retrieval
	 *             job to complete.
	 */
	IRetrievalResult waitAndGetResult() throws InterruptedException;
}
