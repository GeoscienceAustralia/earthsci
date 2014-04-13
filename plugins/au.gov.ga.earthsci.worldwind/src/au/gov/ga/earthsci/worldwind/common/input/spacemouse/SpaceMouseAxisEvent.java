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
package au.gov.ga.earthsci.worldwind.common.input.spacemouse;

import java.util.Arrays;

/**
 * Event passed to the {@link ISpaceMouseListener}s when the user moves the
 * SpaceMouse on one of its axes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SpaceMouseAxisEvent
{
	/**
	 * Absolute value of the axes. There are 6 axes: X/Y/Z translation, and
	 * X/Y/Z rotation.
	 */
	public final float[] values = new float[6];

	/**
	 * Delta change of the axes since the last event. There are 6 axes: X/Y/Z
	 * translation, and X/Y/Z rotation.
	 */
	public final float[] deltas = new float[6];

	@Override
	public String toString()
	{
		return getClass().getName() + " (values = " + Arrays.toString(values) + ")";
	}
}
