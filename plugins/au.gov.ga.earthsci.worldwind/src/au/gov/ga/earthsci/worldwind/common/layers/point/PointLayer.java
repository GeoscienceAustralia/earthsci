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
package au.gov.ga.earthsci.worldwind.common.layers.point;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayer;

/**
 * Interface for all Point layers. Point classes can extend the specific class
 * for the type of points to display (such as Markers or Icons), and simply need
 * to implement this interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface PointLayer extends DataLayer
{
	/**
	 * Add a point to this layer. Called by the {@link PointProvider}.
	 * 
	 * @param position
	 *            Point to add
	 * @param attributeValues
	 *            Attribute values for this point
	 */
	void addPoint(Position position, AVList attributeValues);

	/**
	 * Called by the {@link PointProvider} after all points have been loaded.
	 */
	void loadComplete();
}
