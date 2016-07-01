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
package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import gov.nasa.worldwind.render.markers.Marker;

import java.util.List;

/**
 * Represents a single borehole in the {@link BoreholeLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Borehole extends Marker
{
	/**
	 * @return The path of this borehole
	 */
	BoreholePath getPath();

	/**
	 * @return List of samples associated with this borehole
	 */
	List<BoreholeSample> getSamples();

	/**
	 * @return List of markers associated with this borehole
	 */
	List<BoreholeMarker> getMarkers();

	/**
	 * @return The display text associated with this borehole; eg to show as a
	 *         tooltip
	 */
	String getText();

	/**
	 * @return A URL string to a website that describes this borehole (null if
	 *         none)
	 */
	String getLink();

	/**
	 * Called by the {@link BoreholeLayer} after all data for this borehole has
	 * been loaded.
	 */
	void loadComplete();
}
