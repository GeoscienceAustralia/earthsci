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
package au.gov.ga.earthsci.worldwind.common.layers.geometry.types.airspace;

import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * An interface for airspaces that support the drawing of the generating shape
 * outlines.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ShapeOutlineAirspace extends Airspace
{
	/**
	 * Set whether to draw the shape outline at the top of the airspace.
	 */
	public void setDrawUpperShapeOutline(boolean drawUpperShapeOutline);

	/**
	 * Set whether to draw the shape outline at the top of the airspace.
	 */
	public void setDrawLowerShapeOutline(boolean drawLowerShapeOutline);

}
