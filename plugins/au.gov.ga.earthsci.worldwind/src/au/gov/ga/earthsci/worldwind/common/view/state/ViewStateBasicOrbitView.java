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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.view.subsurface.SubSurfaceOrbitView;
import au.gov.ga.earthsci.worldwind.common.view.transform.TransformBasicOrbitView;

/**
 * Orbit view with better support for copying view state from other views.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ViewStateBasicOrbitView extends TransformBasicOrbitView
{
	protected double minimumFarDistance = MINIMUM_FAR_DISTANCE;

	@Override
	public void beforeComputeMatrices()
	{
		super.beforeComputeMatrices();
		minimumFarDistance = globe.getDiameter() * 1.5d;
	}

	@Override
	protected double computeFarClipDistance()
	{
		double far = super.computeFarClipDistance();

		double altitude = SubSurfaceOrbitView.computeEyeAltitude(getDC(), getGlobe(), getCurrentEyePoint());
		if (altitude < 0)
		{
			//if subsurface, use a larger far clip distance, so we don't fall into the black hole
			return Math.max(far, minimumFarDistance);
		}

		return far;
	}

	@Override
	public void copyViewState(View view)
	{
		this.globe = view.getGlobe();

		Vec4 eyePoint = view.getCurrentEyePoint();
		Position eyePosition = globe.computePositionFromPoint(eyePoint);

		double eyePositionElevation = globe.getElevation(eyePosition.latitude, eyePosition.longitude);
		boolean belowSurface = eyePosition.elevation < eyePositionElevation;

		Vec4 centerPoint = view.getCenterPoint();
		//if the view is not looking at the globe, create a centerPoint just in front of the eye
		if (centerPoint == null || belowSurface)
		{
			Vec4 forward = view.getForwardVector();
			centerPoint = eyePoint.add3(forward);
		}
		Position centerPosition = globe.computePositionFromPoint(centerPoint);

		if (trySetOrientation(eyePosition, centerPosition))
		{
			return;
		}

		//if a center position just in front of the eye doesn't work, then try the closest position
		centerPosition = Util.computeViewClosestCenterPosition(view, eyePoint);

		if (trySetOrientation(eyePosition, centerPosition))
		{
			return;
		}

		//if everything failed, just set the view using the heading/pitch
		setEyePosition(eyePosition);
		setHeading(view.getHeading());
		setPitch(view.getPitch());
	}
}
