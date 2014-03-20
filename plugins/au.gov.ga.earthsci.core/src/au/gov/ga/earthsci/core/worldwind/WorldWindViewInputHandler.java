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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.awt.BasicViewInputHandler;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class WorldWindViewInputHandler extends BasicViewInputHandler
{
	@Override
	public void stopAnimators()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAnimating()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addAnimator(Animator animator)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRotateView(Angle headingChange, Angle pitchChange, ActionAttributes actionAttribs)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRotateView(double headingInput, double pitchInput, double totalHeadingInput,
			double totalPitchInput, DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void onVerticalTranslate(double translateChange, double totalTranslateChange,
			DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void onHorizontalTranslateRel(Angle forwardChange, Angle sideChange, ActionAttributes actionAttribs)
	{
		// TODO Auto-generated method stub

	}
}
