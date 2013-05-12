/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.discovery;

import org.eclipse.jface.viewers.ILabelProvider;

/**
 * Represents a single data discovery (search).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDiscovery
{
	/**
	 * Represents an unknown value.
	 */
	static final int UNKNOWN = -1;

	/**
	 * Default page size for discovery implementations.
	 */
	static final int DEFAULT_PAGE_SIZE = 10;

	/**
	 * @return The discovery service that originally created this discovery (the
	 *         origin of the results).
	 */
	IDiscoveryService getService();

	/**
	 * @return The search parameters used to create this discovery.
	 */
	IDiscoveryParameters getParameters();

	/**
	 * @return A new label provider that configures the labels for the viewer
	 *         that displays this discovery's results.
	 */
	ILabelProvider createLabelProvider();

	/**
	 * Add a listener to this discovery.
	 * 
	 * @param listener
	 *            Listener to add
	 */
	void addListener(IDiscoveryListener listener);

	/**
	 * Remove a listener from this discovery.
	 * 
	 * @param listener
	 *            Listener to remove
	 */
	void removeListener(IDiscoveryListener listener);

	/**
	 * Start this discovery.
	 */
	void start();

	/**
	 * @return Is this discovery loading results?
	 */
	boolean isLoading();

	/**
	 * @return The error that occurred when performing this discovery (null if
	 *         no error)
	 */
	Exception getError();

	/**
	 * Number of results found for this discovery.
	 * <p/>
	 * Returns 0 if no results found (check {@link #isLoading()} to see if the
	 * discovery is still in progress). Returns {@link #UNKNOWN} if there is an
	 * unknown number of results (at least 1).
	 * 
	 * @return Number of results
	 */
	int getResultCount();

	/**
	 * Get the result at the given index. Returns null if this discovery is
	 * still loading the result.
	 * <p/>
	 * If the result at the given index hasn't yet been loaded, this will begin
	 * the loading process for the requested result.
	 * 
	 * @param index
	 *            Result's index
	 * @return Result at index, or null if still loading
	 * @throws DiscoveryResultNotFoundException
	 *             If there is no result at the given index.
	 * @throws DiscoveryIndexOutOfBoundsException
	 *             If the requested index is outside of the bounds of the
	 *             discovery's results (and there will never be a result at the
	 *             index).
	 */
	IDiscoveryResult getResult(int index) throws DiscoveryResultNotFoundException, DiscoveryIndexOutOfBoundsException;

	/**
	 * The page size used for this discovery.
	 * <ul>
	 * <li>Returns 0 if this discovery cannot be paged.</li>
	 * <li>If this discovery has a fixed page size, the fixed page size is
	 * returned.</li>
	 * <li>Otherwise returns {@link #getCustomPageSize()}.</li>
	 * </ul>
	 * 
	 * @return Page size
	 */
	int getPageSize();

	/**
	 * Does this discovery support custom page sizes. True if this discovery
	 * supports paging, and doesn't have a fixed page size.
	 * 
	 * @return True if this discovery supports custom page sizes.
	 */
	boolean supportsCustomPageSize();

	/**
	 * @return The custom page size to use if this discovery doesn't have a
	 *         fixed page size. Defaults to {@value #DEFAULT_PAGE_SIZE}.
	 */
	int getCustomPageSize();

	/**
	 * Set the page size to use for this discovery. Ignored if this discovery
	 * has a fixed page size. Defaults to {@value #DEFAULT_PAGE_SIZE}.
	 * 
	 * @param customPageSize
	 * 
	 * @see #supportsCustomPageSize()
	 */
	void setCustomPageSize(int customPageSize);
}
