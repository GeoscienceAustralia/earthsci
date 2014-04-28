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
package au.gov.ga.earthsci.worldwind.common.view.rotate;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport.OrbitViewState;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;
import au.gov.ga.earthsci.worldwind.common.view.target.TargetOrbitViewInputHandler;

/**
 * {@link OrbitViewInputHandler} subclass that adds support for free rotation of
 * the globe (without it being fixed around the north-south axis).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FreeRotateOrbitViewInputHandler extends TargetOrbitViewInputHandler
{
	/**
	 * Rotate the globe by an amount in a given direction.
	 * 
	 * @param direction
	 *            Direction to rotate
	 * @param amount
	 *            Amount to rotate
	 * @param deviceAttributes
	 * @param actionAttributes
	 */
	public void onRotateFree(Angle direction, Angle amount, DeviceAttributes deviceAttributes,
			ActionAttributes actionAttributes)
	{
		if (!(getView() instanceof OrbitView))
		{
			String message = "View must be an instance of OrbitView";
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		amount = Angle.fromDegrees(amount.degrees * getScaleValueHorizTransRel(deviceAttributes, actionAttributes));

		OrbitView view = (OrbitView) getView();

		//if this view hasn't been applied yet, its globe will be null
		if (view.getGlobe() == null)
		{
			return;
		}

		//backup the pitch (set it later so it isn't changed by this method)
		Angle pitch = view.getPitch();

		//get the eye point and normalize it
		Vec4 centerPoint = view.getCenterPoint();
		Vec4 centerPointNormalized = centerPoint.normalize3();

		//find the current left vector
		Matrix modelview = view.getModelviewMatrix();
		Matrix modelviewInv = modelview.getInverse();
		Vec4 left = Vec4.UNIT_X.transformBy4(modelviewInv);

		//rotate the left vector around the forward vector
		Matrix translationRotation = Matrix.fromAxisAngle(direction, centerPointNormalized);
		Vec4 leftRotated = left.transformBy4(translationRotation);

		//calculate the new eye point by rotating it around the rotated left vector
		Matrix rotation = Matrix.fromAxisAngle(amount, leftRotated);
		Vec4 newCenterPoint = centerPoint.transformBy4(rotation);

		//calculate the new eye position
		Position newCenterPosition = view.getGlobe().computePositionFromPoint(newCenterPoint);
		view.setCenterPosition(newCenterPosition);

		//compute the new heading
		if (view instanceof IDelegateView)
		{
			modelview = ((IDelegateView) view).getPretransformedModelView();
		}
		Matrix newModelview = modelview.multiply(rotation);
		Angle newHeading = calculateHeading(view, newModelview);
		view.setHeading(newHeading);
		view.setPitch(pitch);

		if (view instanceof BasicOrbitView)
		{
			((BasicOrbitView) view).computeAndSetViewCenter();
		}
		view.firePropertyChange(AVKey.VIEW, null, view);
	}

	protected Angle calculateHeading(OrbitView view, Matrix modelview)
	{
		Globe globe = view.getGlobe();
		Position center = view.getCenterPosition();
		Vec4 centerPoint = globe.computePointFromPosition(center);
		Vec4 normal = globe.computeSurfaceNormalAtLocation(center.getLatitude(), center.getLongitude());
		Vec4 lookAtPoint = centerPoint.subtract3(normal);
		Vec4 north = globe.computeNorthPointingTangentAtLocation(center.getLatitude(), center.getLongitude());
		Matrix centerTransform = Matrix.fromViewLookAt(centerPoint, lookAtPoint, north);
		Matrix centerTransformInv = centerTransform.getInverse();
		if (centerTransformInv == null)
		{
			String message = Logging.getMessage("generic.NoninvertibleMatrix");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		Matrix hpzTransform = modelview.multiply(centerTransformInv);
		return ViewUtil.computeHeading(hpzTransform);
	}

	/**
	 * Change the altitude of the eye point, without changing the center (look
	 * at) point or the heading. Only the zoom and pitch change.
	 * 
	 * @param amount
	 * @param deviceAttributes
	 * @param actionAttributes
	 */
	public void onAltitudeFree(double amount, DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
	{
		if (!(getView() instanceof OrbitView))
		{
			String message = "View must be an instance of OrbitView";
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		amount *= getScaleValueHorizTransRel(deviceAttributes, actionAttributes);

		OrbitView view = (OrbitView) getView();

		//if this view hasn't been applied yet, its globe will be null
		if (view.getGlobe() == null)
		{
			return;
		}

		Globe globe = view.getGlobe();

		//get the eye point and normalize it
		Position centerPosition = view.getCenterPosition();
		Vec4 centerPoint = view.getCenterPoint();

		Position eyePosition = view.getEyePosition();
		Position newEyePosition = new Position(eyePosition, eyePosition.elevation + amount);
		Vec4 newEyePoint = globe.computePointFromPosition(newEyePosition);

		Vec4 north =
				globe.computeNorthPointingTangentAtLocation(centerPosition.getLatitude(), centerPosition.getLongitude());

		Angle heading = view.getHeading();
		Matrix headingMatrix = Matrix.fromRotationZ(heading);
		Vec4 up = north.transformBy4(headingMatrix);
		OrbitViewState state = OrbitViewInputSupport.computeOrbitViewState(globe, newEyePoint, centerPoint, up);

		double pitchDegrees = Math.min(90.0, Math.abs(state.getPitch().degrees));
		Angle pitch = Angle.fromDegrees(pitchDegrees);

		view.setZoom(state.getZoom());
		view.setPitch(pitch);
	}
}
