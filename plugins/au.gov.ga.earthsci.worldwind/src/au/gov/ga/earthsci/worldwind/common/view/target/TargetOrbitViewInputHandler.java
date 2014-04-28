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

import gov.nasa.worldwind.awt.AbstractViewInputHandler;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Point;

import au.gov.ga.earthsci.worldwind.common.view.orbit.BaseOrbitViewInputHandler;

/**
 * Extension of the standard orbit view input handler that adds support for the
 * {@link TargetOrbitView}, which allows manually changing the center point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TargetOrbitViewInputHandler extends BaseOrbitViewInputHandler
{
	public TargetOrbitViewInputHandler()
	{
		super();

		ViewInputAttributes.ActionAttributes actionAttrs;
		actionAttrs = this.getAttributes().getActionMap(ViewInputAttributes.DEVICE_MOUSE).getActionAttributes(
				ViewInputAttributes.VIEW_MOVE_TO);
		actionAttrs.setMouseActionListener(new TargetMoveToMouseActionListener());
	}

	protected boolean isTargetMode()
	{
		OrbitView view = getView();
		return view instanceof TargetOrbitView && ((ITargetView) view).isTargetMode();
	}

	@Override
	protected void onHorizontalTranslateRel(double forwardInput, double sideInput, double totalForwardInput,
			double totalSideInput, DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		if (isTargetMode())
		{
			//if the view is in target mode, then don't perform the absolute positioning, so
			//only call the relative positioning function

			this.stopGoToAnimators();
			this.stopUserInputAnimators(VIEW_ANIM_HEADING, VIEW_ANIM_PITCH, VIEW_ANIM_ZOOM);

			Angle forwardChange = Angle.fromDegrees(
					forwardInput * getScaleValueHorizTransRel(deviceAttributes, actionAttributes));
			Angle sideChange = Angle.fromDegrees(
					sideInput * getScaleValueHorizTransRel(deviceAttributes, actionAttributes));
			onHorizontalTranslateRel(forwardChange, sideChange, actionAttributes);

			return;
		}

		super.onHorizontalTranslateRel(forwardInput, sideInput, totalForwardInput, totalSideInput, deviceAttributes,
				actionAttributes);
	}

	@Override
	protected void onHorizontalTranslateRel(Angle forwardChange, Angle sideChange, ActionAttributes actionAttribs)
	{
		if (isTargetMode())
		{
			OrbitView view = getView();
			if (view == null)
			{
				return;
			}
			if (forwardChange.equals(Angle.ZERO) && sideChange.equals(Angle.ZERO))
			{
				return;
			}

			double elevationChangePerDegree = 0;
			LatLon centerLocation = view.getCenterPosition();
			if (centerLocation != null && view.getGlobe() != null)
			{
				Angle nearByLatitude = centerLocation.latitude.addDegrees(centerLocation.latitude.degrees > 0 ? -1 : 1);
				LatLon nearByLocation = new LatLon(nearByLatitude, centerLocation.longitude);
				Vec4 v1 = view.getGlobe().computePointFromLocation(centerLocation);
				Vec4 v2 = view.getGlobe().computePointFromLocation(nearByLocation);
				elevationChangePerDegree = v1.distanceTo3(v2); //for the earth, will be approx 111km
			}

			//move the center position, including elevation, according to the current view heading/pitch
			double sinHeading = view.getHeading().sin();
			double cosHeading = view.getHeading().cos();
			double sinPitch = view.getPitch().sin();
			double cosPitch = view.getPitch().cos();
			double latChange = cosPitch * cosHeading * forwardChange.degrees - sinHeading * sideChange.degrees;
			double lonChange = cosPitch * sinHeading * forwardChange.degrees + cosHeading * sideChange.degrees;
			double elevChange = sinPitch * forwardChange.degrees * elevationChangePerDegree;
			Position newPosition = view.getCenterPosition().add(Position.fromDegrees(latChange, lonChange, elevChange));

			this.setCenterPosition(view, this.uiAnimControl, newPosition, actionAttribs);

			return;
		}

		super.onHorizontalTranslateRel(forwardChange, sideChange, actionAttribs);
	}

	@Override
	public Point getMousePoint()
	{
		return super.getMousePoint();
	}

	public class TargetMoveToMouseActionListener extends MoveToMouseActionListener
	{
		@Override
		public boolean inputActionPerformed(AbstractViewInputHandler inputHandler,
				java.awt.event.MouseEvent mouseEvent, ViewInputAttributes.ActionAttributes viewAction)
		{
			boolean handleThisEvent = false;
			java.util.List<?> buttonList = viewAction.getMouseActions();
			for (Object b : buttonList)
			{
				ViewInputAttributes.ActionAttributes.MouseAction buttonAction =
						(ViewInputAttributes.ActionAttributes.MouseAction) b;
				if ((mouseEvent.getButton() == buttonAction.mouseButton))
				{
					handleThisEvent = true;
				}
			}
			if (!handleThisEvent)
			{
				return false;
			}

			if (isTargetMode())
			{
				Position mousePosition = ((ITargetView) getView()).getMousePosition();
				if (mousePosition != null)
				{
					onMoveTo(mousePosition, getAttributes().getDeviceAttributes(ViewInputAttributes.DEVICE_MOUSE),
							viewAction);
					return true;
				}
			}

			return super.inputActionPerformed(inputHandler, mouseEvent, viewAction);
		}
	}
}
