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
package au.gov.ga.earthsci.worldwind.common.view.orbit;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Rectangle;

import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Helper class to create an {@link OrbitView} animator that flies to a given
 * sector.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FlyToSectorAnimator
{
	/**
	 * Create a {@link FlyToOrbitViewAnimator} that flies to a given position,
	 * ensuring that a delta lat/lon area is visible.
	 * 
	 * @param orbitView
	 *            Orbit view to create the animator for
	 * @param beginCenterPos
	 *            Begin position
	 * @param endCenterPos
	 *            End position
	 * @param beginHeading
	 *            Initial heading
	 * @param beginPitch
	 *            Initial pitch
	 * @param beginZoom
	 *            Initial zoom
	 * @param endVisibleDelta
	 *            End lat/lon delta. The end zoom is calculated to ensure that
	 *            this lat/lon delta is visible.
	 * @param timeToMove
	 *            Time in milliseconds for the animation
	 * @return Animator to fly to the given position and zoom
	 */
	public static FlyToOrbitViewAnimator createFlyToSectorAnimator(OrbitView orbitView, Position beginCenterPos,
			Position endCenterPos, Angle beginHeading, Angle beginPitch, double beginZoom, LatLon endVisibleDelta,
			long timeToMove)
	{
		double endZoom = calculateEndZoom(orbitView, endVisibleDelta);
		return FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView, beginCenterPos, endCenterPos,
				beginHeading, Angle.ZERO, beginPitch, Angle.ZERO, beginZoom, endZoom, timeToMove, WorldWind.ABSOLUTE);
	}

	public static FlyToOrbitViewAnimator createScaledFlyToSectorAnimator(OrbitView orbitView, Position beginCenterPos,
			Position endCenterPos, Angle beginHeading, Angle beginPitch, double beginZoom, LatLon endVisibleDelta,
			double timeScale)
	{
		double endZoom = calculateEndZoom(orbitView, endVisibleDelta);
		long timeToMove =
				(Util.getScaledLengthMillis(timeScale, beginCenterPos, endCenterPos) + Util.getScaledLengthMillis(
						timeScale, beginZoom, endZoom)) / 2;
		return FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView, beginCenterPos, endCenterPos,
				beginHeading, Angle.ZERO, beginPitch, Angle.ZERO, beginZoom, endZoom, timeToMove, WorldWind.ABSOLUTE);
	}

	public static double calculateEndZoom(OrbitView orbitView, LatLon endVisibleDelta)
	{
		Rectangle viewport = orbitView.getViewport();
		Angle fieldOfView = orbitView.getFieldOfView();

		double deltaLonDegrees = Math.min(endVisibleDelta.getLongitude().degrees, 90);
		double deltaLatDegrees = Math.min(endVisibleDelta.getLatitude().degrees, 90);

		double degreesPerPixelWidth = deltaLonDegrees / viewport.getWidth();
		double degreesPerPixelHeight = deltaLatDegrees / viewport.getHeight();
		double degreesPerPixel = Math.max(degreesPerPixelWidth, degreesPerPixelHeight);

		double metersPerPixel = 111111.11 * degreesPerPixel; //very! approximate degrees to meters conversion
		metersPerPixel *= 1.1; //zoom out just a little more, to add a slight border

		double viewportWidth = viewport.getWidth();
		double pixelSizeScale = 2 * fieldOfView.tanHalfAngle() / (viewportWidth <= 0 ? 1d : viewportWidth);

		return metersPerPixel / pixelSizeScale;
	}
}
