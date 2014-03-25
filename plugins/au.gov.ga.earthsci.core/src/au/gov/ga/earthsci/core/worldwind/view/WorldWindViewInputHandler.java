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
package au.gov.ga.earthsci.core.worldwind.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.BasicViewInputHandler;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class WorldWindViewInputHandler extends BasicViewInputHandler
{
	@Override
	protected WorldWindView getView()
	{
		return (WorldWindView) super.getView();
	}

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
	}

	@Override
	protected void onRotateView(double headingInput, double pitchInput, double totalHeadingInput,
			double totalPitchInput, DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		WorldWindView view = getView();

		if (actionAttributes.getMouseActions() != null)
		{
			// Switch the direction of heading change depending on whether the cursor is above or below
			// the center of the screen.
			Rectangle viewport = view.getViewport();
			if (getMousePoint().y - viewport.y < viewport.height / 2)
			{
				headingInput = -headingInput;
			}
		}
		else
		{
			double length = Math.sqrt(headingInput * headingInput + pitchInput * pitchInput);
			if (length > 0.0)
			{
				headingInput /= length;
				pitchInput /= length;
			}
		}

		view.setHeading(view.getHeading().addDegrees(headingInput * 0.1));
		view.setPitch(view.getPitch().addDegrees(-pitchInput * 0.1));
		//view.setRoll(view.getRoll().addDegrees(-headingInput * 0.1));

		view.firePropertyChange(AVKey.VIEW, null, view);
	}

	@Override
	protected void onVerticalTranslate(double translateChange, double totalTranslateChange,
			DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		WorldWindView view = getView();
		ViewState state = view.getState();

		double currentDistance = state.getZoom();
		double logCurrentDistance = currentDistance != 0 ? Math.log(currentDistance) : 0;
		double newDistance = Math.exp(logCurrentDistance + translateChange * 0.1);
		state.setZoom(newDistance);

		view.firePropertyChange(AVKey.VIEW, null, view);
	}

	/*@Override
	protected void onHorizontalTranslateRel(double forwardInput, double sideInput, double totalForwardInput,
			double totalSideInput, DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		WorldWindView view = getView();
		Position centerPosition = view.getCenterPosition();
		Position eyePosition = view.getEyePosition();
		Position delta =
				Position.fromDegrees(forwardInput * getScaleValueHorizTransRel(deviceAttributes, actionAttributes),
						sideInput * getScaleValueHorizTransRel(deviceAttributes, actionAttributes));
		view.setCenterPosition(centerPosition.add(delta));
		view.setEyePosition(eyePosition.add(delta));

		view.firePropertyChange(AVKey.VIEW, null, view);
	}*/

	@Override
	protected void onHorizontalTranslateRel(double forwardInput, double sideInput,
			double totalForwardInput, double totalSideInput,
			ViewInputAttributes.DeviceAttributes deviceAttributes,
			ViewInputAttributes.ActionAttributes actionAttributes)
	{
		//this.stopGoToAnimators();
		//this.stopUserInputAnimators(VIEW_ANIM_HEADING, VIEW_ANIM_PITCH, VIEW_ANIM_ZOOM);

		if (actionAttributes.getMouseActions() != null)
		{
			// Normalize the forward and right magnitudes.
			double length = Math.sqrt(forwardInput * forwardInput + sideInput * sideInput);
			if (length > 0.0)
			{
				forwardInput /= length;
				sideInput /= length;
			}

			Point point = constrainToSourceBounds(getMousePoint(), getWorldWindow());
			Point lastPoint = constrainToSourceBounds(getLastMousePoint(), getWorldWindow());
			if (getSelectedPosition() == null)
			{
				// Compute the current selected position if none exists. This happens if the user starts dragging when
				// the cursor is off the globe, then drags the cursor onto the globe.
				setSelectedPosition(computeSelectedPosition());
			}
			else if (computeSelectedPosition() == null)
			{
				// User dragged the cursor off the globe. Clear the selected position to ensure a new one will be
				// computed if the user drags the cursor back to the globe.
				setSelectedPosition(null);
			}
			else if (computeSelectedPointAt(point) == null || computeSelectedPointAt(lastPoint) == null)
			{
				// User selected a position that is won't work for dragging. Probably the selected elevation is above the
				// eye elevation, in which case dragging becomes unpredictable. Clear the selected position to ensure
				// a new one will be computed if the user drags the cursor to a valid position.
				setSelectedPosition(null);
			}

			Vec4 vec = computeSelectedPointAt(point);
			Vec4 lastVec = computeSelectedPointAt(lastPoint);

			// Cursor is on the globe, pan between the two positions.
			if (vec != null && lastVec != null)
			{


				// Compute the change in view location given two screen points and corresponding world vectors.
				LatLon latlon = getChangeInLocation(lastPoint, point, lastVec, vec);
				onHorizontalTranslateAbs(latlon.getLatitude(), latlon.getLongitude(), actionAttributes);
				return;
			}

			Point movement = ViewUtil.subtract(point, lastPoint);
			forwardInput = movement.y;
			sideInput = -movement.x;
		}

		// Cursor is off the globe, we potentially want to simulate globe dragging.
		// or this is a keyboard event.
		Angle forwardChange = Angle.fromDegrees(
				forwardInput * getScaleValueHorizTransRel(deviceAttributes, actionAttributes));
		Angle sideChange = Angle.fromDegrees(
				sideInput * getScaleValueHorizTransRel(deviceAttributes, actionAttributes));
		onHorizontalTranslateRel(forwardChange, sideChange, actionAttributes);
	}

	@Override
	protected void onHorizontalTranslateRel(Angle forwardChange, Angle sideChange, ActionAttributes actionAttribs)
	{
		WorldWindView view = this.getView();
		if (view == null) // include this test to ensure any derived implementation performs it
		{
			return;
		}

		if (forwardChange.equals(Angle.ZERO) && sideChange.equals(Angle.ZERO))
		{
			return;
		}

		double sinHeading = view.getHeading().sin();
		double cosHeading = view.getHeading().cos();
		double latChange = cosHeading * forwardChange.getDegrees() - sinHeading * sideChange.getDegrees();
		double lonChange = sinHeading * forwardChange.getDegrees() + cosHeading * sideChange.getDegrees();
		Position delta = Position.fromDegrees(latChange, lonChange, 0.0);

		Position centerPosition = view.getCenterPosition();
		view.setCenterPosition(centerPosition.add(delta));

		view.firePropertyChange(AVKey.VIEW, null, view);
	}

	@Override
	protected void onHorizontalTranslateAbs(Angle latitudeChange, Angle longitudeChange, ActionAttributes actionAttribs)
	{
		//this.stopGoToAnimators();
		//this.stopUserInputAnimators(VIEW_ANIM_HEADING, VIEW_ANIM_PITCH, VIEW_ANIM_ZOOM);

		WorldWindView view = this.getView();
		if (view == null) // include this test to ensure any derived implementation performs it
		{
			return;
		}

		if (latitudeChange.equals(Angle.ZERO) && longitudeChange.equals(Angle.ZERO))
		{
			return;
		}

		Position delta = new Position(latitudeChange, longitudeChange, 0.0);

		Position centerPosition = view.getCenterPosition();
		view.setCenterPosition(centerPosition.add(delta));

		view.firePropertyChange(AVKey.VIEW, null, view);
	}

	protected double getScaleValueHorizTransRel(
			ViewInputAttributes.DeviceAttributes deviceAttributes, ViewInputAttributes.ActionAttributes actionAttributes)
	{

		View view = this.getView();
		if (view == null)
		{
			return 0.0;
		}
		if (view instanceof OrbitView)
		{
			double[] range = actionAttributes.getValues();
			// If this is an OrbitView, we use the zoom value to set the scale
			double radius = this.getWorldWindow().getModel().getGlobe().getRadius();
			double t = getScaleValue(range[0], range[1],
					((OrbitView) view).getZoom(), 3.0 * radius, true);
			return (t);
		}
		else
		{
			// Any other view, use the base class scaling method
			return (super.getScaleValueElevation(deviceAttributes, actionAttributes));
		}
	}
}
