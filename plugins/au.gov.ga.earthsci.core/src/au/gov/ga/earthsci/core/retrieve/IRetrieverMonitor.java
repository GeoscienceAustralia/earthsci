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

import java.io.Closeable;

/**
 * Monitors the resource retrieval. The {@link IRetriever} is expected to call
 * methods on this object during retrieval.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrieverMonitor
{
	/**
	 * Notify the monitor that the retrieval status has changed.
	 * 
	 * @param status
	 *            New status
	 */
	void updateStatus(RetrievalStatus status);

	/**
	 * Notify the monitor that the resource retrieval position has progressed
	 * (ie some data has been retrieved).
	 * <p/>
	 * Alternatively the {@link #setPosition(int)} method can be called for
	 * absolute amounts.
	 * 
	 * @param amount
	 *            Relative amount of progression of resource retrieval
	 */
	void progress(long amount);

	/**
	 * Notify the monitor that the resource retrieval position has been updated
	 * (ie some data has been retrieved).
	 * <p/>
	 * Alternatively the {@link #progress(int)} method can be called for
	 * relative amounts.
	 * 
	 * @param position
	 *            Absolute position of the resource retrieval
	 */
	void setPosition(long position);

	/**
	 * Notify the monitor of the length of the resource being retrieved. Don't
	 * need to call this method for resources of unknown length.
	 * 
	 * @param length
	 *            Length/size of the resource being retrieved
	 */
	void setLength(long length);

	/**
	 * Has this retrieval been canceled? The {@link IRetriever} should check
	 * this property during retrieval and cancel if true.
	 * 
	 * @return True if this retrieval has been canceled.
	 */
	boolean isCanceled();

	/**
	 * Has this retrieval been paused? The {@link IRetriever} should check this
	 * property during retrieval and pause if true.
	 * 
	 * @return True if this retrieval has been paused.
	 */
	boolean isPaused();

	/**
	 * Set the object that can be closed if the job performing the retrieval is
	 * canceled. This should interrupt the retrieval immediately.
	 * {@link IRetriever} implementations should call this from the retrieve
	 * method as soon as possible.
	 * 
	 * @param closeable
	 *            Object to close if/when the retrieval job is canceled
	 */
	void setCloseable(Closeable closeable);
}
