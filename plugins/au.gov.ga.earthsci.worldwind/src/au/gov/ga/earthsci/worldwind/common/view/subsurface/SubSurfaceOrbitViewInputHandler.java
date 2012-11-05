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
package au.gov.ga.earthsci.worldwind.common.view.subsurface;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.animation.AnimationController;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.view.BasicView;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;

/**
 * {@link OrbitViewInputHandler} subclass that supports the
 * {@link SubSurfaceOrbitView} which allows the user to move sub-surface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SubSurfaceOrbitViewInputHandler extends OrbitViewInputHandler
{
	@Override
	protected void changeZoom(BasicOrbitView view, AnimationController animControl, double change,
			ActionAttributes attrib)
	{
		Vec4 eyePoint = view.getCurrentEyePoint();
		double altitude = SubSurfaceOrbitView.computeEyeAltitude(view.getDC(), view.getGlobe(), eyePoint);

		//use the super method if far away from surface, as it does smoothing
		if (altitude > 20000)
		{
			super.changeZoom(view, animControl, change, attrib);
			return;
		}

		view.computeAndSetViewCenterIfNeeded();

		if (animControl.get(VIEW_ANIM_ZOOM) != null)
			animControl.remove(VIEW_ANIM_ZOOM);

		view.setZoom(computeNewZoomFromAltitude(altitude, view.getZoom(), change, view.getOrbitViewLimits()));

		view.firePropertyChange(AVKey.VIEW, null, view);
	}

	protected static double computeNewZoomFromAltitude(double altitude, double curZoom, double change,
			OrbitViewLimits limits)
	{
		altitude = Math.max(10000, altitude);
		double newAltitude = computeNewZoom(altitude, change, limits);
		double delta = newAltitude - altitude;
		return curZoom + delta;
	}

	@Override
	protected double getScaleValueHorizTransRel(DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		View view = this.getView();
		if (view != null && view instanceof BasicView)
		{
			//change move speed according to eye altitude
			double[] range = actionAttributes.getValues();
			Vec4 eyePoint = view.getCurrentEyePoint();
			double altitude =
					SubSurfaceOrbitView.computeEyeAltitude(((BasicView) view).getDC(), view.getGlobe(), eyePoint);
			double radius = view.getGlobe().getRadius();

			//multiply minimum speed by 10, because default min speed is too slow for sub-surface movement
			return getScaleValue(range[0] * 10, range[1], Math.abs(altitude), 3.0 * radius, true);
		}
		return super.getScaleValueHorizTransRel(deviceAttributes, actionAttributes);
	}
}
