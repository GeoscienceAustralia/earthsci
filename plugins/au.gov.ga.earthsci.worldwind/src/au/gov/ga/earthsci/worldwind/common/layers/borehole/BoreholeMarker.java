/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.markers.Marker;

import java.awt.Color;

/**
 * Represents a Borehole marker.
 *
 * @author Michael de Hoog
 */
public interface BoreholeMarker extends Marker
{
	/**
	 * @return {@link Borehole} that this marker is associated with
	 */
	Borehole getBorehole();

	/**
	 * @return Measured depth of this marker (in positive meters)
	 */
	double getDepth();

	/**
	 * @return Marker's azimuth
	 */
	Angle getAzimuth();

	/**
	 * @return Marker's dip
	 */
	Angle getDip();

	/**
	 * @return Color used to display this marker
	 */
	Color getColor();

	/**
	 * @return The display text associated with this marker; eg to show as a
	 *         tooltip
	 */
	String getText();

	/**
	 * @return A URL string to a website that describes this marker (null if
	 *         none)
	 */
	String getLink();
}
