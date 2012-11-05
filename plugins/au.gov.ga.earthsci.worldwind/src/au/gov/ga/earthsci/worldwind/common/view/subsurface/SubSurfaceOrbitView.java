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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.AccessibleOrbitViewInputSupport;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;
import au.gov.ga.earthsci.worldwind.common.view.state.ViewStateBasicOrbitView;

/**
 * {@link OrbitView} implementation that allows the user to move sub-surface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SubSurfaceOrbitView extends ViewStateBasicOrbitView
{
	public SubSurfaceOrbitView()
	{
		setViewInputHandler(new SubSurfaceOrbitViewInputHandler());
		setDetectCollisions(false);
		getOrbitViewLimits().setPitchLimits(Angle.fromDegrees(-180), Angle.fromDegrees(180));
	}

	@Override
	public void focusOnViewportCenter()
	{
		if (this.dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		if (this.globe == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Matrix modelview =
				OrbitViewInputSupport.computeTransformMatrix(this.globe, this.center, this.heading, this.pitch,
						this.roll, this.zoom);
		if (modelview != null)
		{
			Matrix modelviewInv = modelview.getInverse();
			if (modelviewInv != null)
			{
				Vec4 eyePoint = Vec4.UNIT_W.transformBy4(modelviewInv);
				Vec4 forward = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelviewInv);
				Vec4 newCenterPoint = null;
				double altitude = computeEyeAltitude(dc, this.globe, eyePoint);


				//try and put it on the surface, but if we are below the surface (or close), ignore
				if (altitude > 100)
				{
					Position viewportCenterPos = this.dc.getViewportCenterPosition();
					if (viewportCenterPos != null)
					{
						Vec4 viewportCenterPoint =
								this.globe.computePointFromPosition(
										viewportCenterPos.getLatitude(),
										viewportCenterPos.getLongitude(),
										this.globe.getElevation(viewportCenterPos.getLatitude(),
												viewportCenterPos.getLongitude())
												* dc.getVerticalExaggeration());

						double distance = eyePoint.distanceTo3(viewportCenterPoint);
						newCenterPoint = Vec4.fromLine3(eyePoint, distance, forward);
					}
				}


				//calculate the center point as 1 unit vector forward from eye point if it's not on the surface
				if (newCenterPoint == null)
				{
					newCenterPoint = eyePoint.add3(forward);
				}

				AccessibleOrbitViewInputSupport.AccessibleOrbitViewState modelCoords =
						AccessibleOrbitViewInputSupport.computeOrbitViewState(this.globe, modelview, newCenterPoint);
				if (validateModelCoordinates(modelCoords))
				{
					setModelCoordinates(modelCoords);
				}
			}
		}
	}

	public static double computeEyeAltitude(DrawContext dc, Globe globe, Vec4 eyePoint)
	{
		Position eyePosition = globe.computePositionFromPoint(eyePoint);
		Vec4 surfacePoint = null;
		if (dc.getSurfaceGeometry() != null)
		{
			surfacePoint = dc.getSurfaceGeometry().getSurfacePoint(eyePosition.latitude, eyePosition.longitude, 0);
		}
		if (surfacePoint == null)
		{
			double elevation =
					globe.getElevation(eyePosition.latitude, eyePosition.longitude) * dc.getVerticalExaggeration();
			Position surfacePosition = new Position(eyePosition, elevation);
			surfacePoint = globe.computePointFromPosition(surfacePosition);
		}
		return eyePoint.getLength3() - surfacePoint.getLength3();
	}

	@Override
	public void computeAndSetViewCenter()
	{
		super.computeAndSetViewCenter();
		//never let the view be in focus
		setViewOutOfFocus(true);
	}

	@Override
	public void setOrientation(Position eyePosition, Position centerPosition)
	{
		super.setOrientation(eyePosition, centerPosition);
		computeAndSetViewCenterIfNeeded();
	}

	@Override
	public void setZoom(double zoom)
	{
		this.zoom = zoom;
		computeAndSetViewCenterIfNeeded();
	}
}
