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
package au.gov.ga.earthsci.worldwind.common.input;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.orbit.OrbitView;
import au.gov.ga.earthsci.worldwind.common.view.rotate.FreeRotateOrbitViewInputHandler;

/**
 * Basic {@link IProviderOrbitViewInputHandler} implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ProviderOrbitViewInputHandler extends FreeRotateOrbitViewInputHandler implements
		IProviderOrbitViewInputHandler
{
	@Override
	public void apply()
	{
		super.apply();
		OrbitInputProviderManager.getInstance().apply(this);
	}

	@Override
	public void markViewChanged()
	{
		try
		{
			View view = getView();
			if (view != null)
			{
				view.firePropertyChange(AVKey.VIEW, null, view);
			}
		}
		catch (Exception e)
		{
			//don't allow view exception to bubble up to caller; print it instead
			e.printStackTrace();
		}
	}

	@Override
	public void onRotateView(Angle headingChange, Angle pitchChange, ActionAttributes actionAttribs)
	{
		super.onRotateView(headingChange, pitchChange, actionAttribs);
	}

	@Override
	public void onVerticalTranslate(double translateChange, double totalTranslateChange,
			DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		super.onVerticalTranslate(translateChange, totalTranslateChange, deviceAttributes, actionAttributes);
	}

	@Override
	public double getScaleValueRotate(ActionAttributes actionAttributes)
	{
		return super.getScaleValueRotate(actionAttributes);
	}

	@Override
	public void stopAnimations()
	{
		View view = getView();
		if (view != null)
		{
			view.stopAnimations();
		}
	}

	@Override
	public void onResetHeadingPitchRoll(ActionAttributes actionAttribs)
	{
		super.onResetHeadingPitchRoll(actionAttribs);
	}

	@Override
	public OrbitView getView()
	{
		return super.getView();
	}
}
