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

import gov.nasa.worldwind.geom.Position;

import java.util.SortedMap;

/**
 * Represents the path of a borehole (not necessarily vertical).
 *
 * @author Michael de Hoog
 */
public interface BoreholePath
{
	/**
	 * @return {@link Borehole} that this path is for
	 */
	Borehole getBorehole();

	/**
	 * @return Map of borehole measured depth to true position.
	 */
	SortedMap<Double, Position> getPositions();

	/**
	 * Add a position to the borehole path.
	 * 
	 * @param measuredDepth
	 * @param position
	 */
	void addPosition(double measuredDepth, Position position);

	/**
	 * Get the position for the given measured depth.
	 * 
	 * @param measuredDepth
	 *            Measured depth to get the position for
	 * @return Position for the given measured depth
	 */
	Position getPosition(double measuredDepth);
}
