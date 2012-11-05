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
package au.gov.ga.earthsci.worldwind.common.view.free;

import gov.nasa.worldwind.awt.ViewInputHandler;

/**
 * Input handler for the {@link FreeView}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface FreeViewInputHandler extends ViewInputHandler
{
	/**
	 * Rotate the camera by the given deltas. Rotations are applied in this
	 * order: pitch, heading, roll.
	 * 
	 * @param deltaHeading
	 *            Rotation about the y-axis
	 * @param deltaPitch
	 *            Rotation about the x-axis
	 * @param deltaRoll
	 *            Rotation about the z-axis
	 */
	void look(double deltaHeading, double deltaPitch, double deltaRoll);

	/**
	 * Move the camera by the given deltas.
	 * 
	 * @param deltaX
	 *            Distance to move along the x-axis
	 * @param deltaY
	 *            Distance to move along the y-axis
	 * @param deltaZ
	 *            Distance to move along the z-axis
	 */
	void move(double deltaX, double deltaY, double deltaZ);
}
