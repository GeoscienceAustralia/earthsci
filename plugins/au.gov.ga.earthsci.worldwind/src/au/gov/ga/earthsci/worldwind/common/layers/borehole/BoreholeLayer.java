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

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;

import java.awt.Color;

import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayer;

/**
 * Layer used to visualise borehole data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface BoreholeLayer extends DataLayer
{
	/**
	 * Add a borehole sample to this layer. Called by the
	 * {@link BoreholeProvider}.
	 * 
	 * @param position
	 *            Borehole sample position
	 * @param attributeValues
	 *            Attribute values for this point
	 */
	void addBoreholeSample(Position position, AVList attributeValues);

	/**
	 * Called by the {@link BoreholeProvider} after all boreholes have been
	 * loaded.
	 */
	void loadComplete();

	/**
	 * @return The minimum distance a Borehole must be from the camera to render
	 *         it. Any borehole closer to the camera than this minimum distance
	 *         should be rendered. If null, there's no minimum distance.
	 */
	Double getMinimumDistance();
	
	/**
	 * @return The default colour to use when rendering samples that have no colour
	 * 		   information available.
	 */
	Color getDefaultSampleColor();
}
