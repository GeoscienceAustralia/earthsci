/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.view.target;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;

/**
 * {@link View} that allows the user to optionally modify the center of rotation
 * point, instead of keeping the center point fixed to the earth's surface,
 * which is the default.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ITargetView extends View
{
	/**
	 * @return Is target mode enabled?
	 */
	boolean isTargetMode();

	/**
	 * Enable/disable target mode. When enabled, the user can modify the center
	 * point, instead of fixing it to the earth's surface.
	 * <p/>
	 * If target mode is enabled, the minimum pitch limit will be set to -180
	 * degrees, and collision detection will be disabled.
	 * 
	 * @param targetMode
	 */
	void setTargetMode(boolean targetMode);

	/**
	 * @return Should the axis marker be drawn when the view changes?
	 */
	boolean isDrawAxisMarker();

	/**
	 * Enable/disable the axis marker that is drawn when the view changes.
	 * 
	 * @param drawAxisMarker
	 */
	void setDrawAxisMarker(boolean drawAxisMarker);

	/**
	 * @return Axis marker that is drawn when the view changes
	 */
	AxisRenderable getAxisMarker();

	/**
	 * @return Approximate mouse position in geographic coordinates
	 */
	Position getMousePosition();

	/**
	 * @return Should this view prioritize a large far clipping plane?
	 */
	boolean isPrioritizeFarClipping();

	/**
	 * Set if this view should prioritize a large far clipping plane. If true,
	 * the far clipping plane will be set to always display the entire globe,
	 * and the near clipping plane will be larger than ideal to keep an
	 * acceptable resolution accuracy in the depth buffer.
	 * 
	 * @param prioritizeFarClipping
	 */
	void setPrioritizeFarClipping(boolean prioritizeFarClipping);

}