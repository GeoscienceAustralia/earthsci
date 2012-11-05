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
package au.gov.ga.earthsci.worldwind.common.view.state;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.ViewUtil;
import au.gov.ga.earthsci.worldwind.common.view.transform.TransformBasicFlyView;

/**
 * Fly view with better support for copying view state from other views.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ViewStateBasicFlyView extends TransformBasicFlyView
{
	@Override
	public void copyViewState(View view)
	{
		this.globe = view.getGlobe();
		Position eyePosition = view.getCurrentEyePosition();

		Matrix positionTransform = ViewUtil.computePositionTransform(globe, eyePosition);
		Matrix invPositionTransform = positionTransform.getInverse();

		if (invPositionTransform != null)
		{
			Matrix transform = view.getModelviewMatrix().multiply(invPositionTransform);
			setEyePosition(eyePosition);
			setHeading(ViewUtil.computeHeading(transform));
			setPitch(ViewUtil.computePitch(transform));
		}
		else
		{
			setEyePosition(eyePosition);
			setHeading(view.getHeading());
			setPitch(view.getPitch());
		}
	}
}
