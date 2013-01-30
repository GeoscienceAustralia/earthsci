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
package au.gov.ga.earthsci.worldwind.common.exaggeration;

/**
 * Service interface for accessing the current vertical exaggeration of the
 * system, and for checking/listening for changes to the exaggeration.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface VerticalExaggerationService
{
	public static final VerticalExaggerationService INSTANCE = new VerticalExaggerationServiceImpl();

	/**
	 * @return The vertical exaggeration.
	 */
	double get();

	/**
	 * Set the vertical exaggeration to the given value.
	 * 
	 * @param exaggeration
	 *            Vertical exaggeration value
	 */
	void set(double exaggeration);

	/**
	 * Add a listener that listens to changes to this service's vertical
	 * exaggertion.
	 * 
	 * @param listener
	 *            Listener to add
	 */
	void addListener(VerticalExaggerationListener listener);

	/**
	 * Remove a listener from this service.
	 * 
	 * @param listener
	 *            Listener to remove
	 */
	void removeListener(VerticalExaggerationListener listener);

	/**
	 * Mark the current vertical exaggeration settings against the provided
	 * object.
	 * <p/>
	 * Used to detect a change in the vertical exaggeration.
	 * <p/>
	 * Objects that wish to monitor changes should first call
	 * {@link #markVerticalExaggeration(Object)}, then call
	 * {@link #isVerticalExaggerationChanged(Object)}.
	 */
	void markVerticalExaggeration(Object key);

	/**
	 * Clear the marked vertical exaggeration settings for the provided object
	 */
	void clearMark(Object key);

	/**
	 * Returns whether the vertical exaggeration settings have changed since
	 * object's last call to {@link #markVerticalExaggeration(Object)}.
	 * <p/>
	 * Objects that wish to monitor changes should first call
	 * {@link #markVerticalExaggeration(Object)}, then call
	 * {@link #isVerticalExaggerationChanged(Object)}.
	 * 
	 * @return Whether the vertical exaggeration has changed since the last call
	 *         to {@link #markVerticalExaggeration(Object)}.
	 */
	boolean isVerticalExaggerationChanged(Object key);

	/**
	 * Equivalent to a call to {@link #isVerticalExaggerationChanged(Object)}
	 * followed by {@link #markVerticalExaggeration(Object)}.
	 * 
	 * @return Whether the vertical exaggeration has changed since the last call
	 *         to {@link #markVerticalExaggeration(Object)}.
	 */
	boolean checkAndMarkVerticalExaggeration(Object key);
}
