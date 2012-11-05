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
package au.gov.ga.earthsci.worldwind.common.view.free;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicView;
import gov.nasa.worldwind.view.ViewUtil;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.view.transform.TransformView;

/**
 * Special {@link View} that allows the user to freely move around the model,
 * without being constrained to the surface of the globe. Removes the concept of
 * 'up' being the normal to the globe's surface. User is able to move and rotate
 * the camera in another direction (6 degrees of freedom).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FreeView extends BasicView implements TransformView
{
	protected double minimumFarDistance = MINIMUM_FAR_DISTANCE;
	protected double minimumNearDistance = 0.1;

	public FreeView()
	{
		this.viewInputHandler = new BasicFreeViewInputHandler();
	}

	protected static Matrix computeRotationTransform(Angle heading, Angle pitch, Angle roll)
	{
		Matrix transform = Matrix.IDENTITY;
		transform = transform.multiply(Matrix.fromAxisAngle(pitch, -1, 0, 0));
		transform = transform.multiply(Matrix.fromAxisAngle(roll, 0, 1, 0));
		transform = transform.multiply(Matrix.fromAxisAngle(heading, 0, 0, 1));
		return transform;
	}

	protected static Matrix computePositionTransform(Globe globe, Position eyePosition)
	{
		Vec4 point = globe.computePointFromPosition(eyePosition);
		return Matrix.fromTranslation(point.getNegative3());
	}

	@Override
	public void beforeComputeMatrices()
	{
		minimumFarDistance = globe.getDiameter() * 1.5d;
	}
	
	@Override
	public java.awt.Rectangle computeViewport(DrawContext dc)
	{
		int[] viewportArray = new int[4];
		this.dc.getGL().glGetIntegerv(GL2.GL_VIEWPORT, viewportArray, 0);
		return new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);
	}

	@Override
	public Matrix computeModelView()
	{
		//default:
		//return ViewUtil.computeTransformMatrix(this.globe, this.eyePosition, this.heading, this.pitch, this.roll);

		Matrix transform = computeRotationTransform(heading, pitch, roll);
		transform = transform.multiply(computePositionTransform(globe, eyePosition));
		return transform;
	}

	@Override
	public Matrix computeProjection()
	{
		double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0 : this.viewport.getWidth();
		double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0 : this.viewport.getHeight();

		return Matrix.fromPerspective(this.fieldOfView, viewportWidth, viewportHeight, this.nearClipDistance,
				this.farClipDistance);
	}

	@Override
	protected void doApply(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (dc.getGL() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (dc.getGlobe() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Update DrawContext and Globe references.
		this.dc = dc;
		this.globe = this.dc.getGlobe();

		beforeComputeMatrices();

		//========== modelview matrix state ==========//
		// Compute the current modelview matrix.
		this.modelview = computeModelView();
		if (this.modelview == null)
		{
			this.modelview = Matrix.IDENTITY;
		}

		// Compute the current inverse-modelview matrix.
		this.modelviewInv = this.modelview.getInverse();
		if (this.modelviewInv == null)
		{
			this.modelviewInv = Matrix.IDENTITY;
		}

		//========== projection matrix state ==========//
		// Get the current OpenGL viewport state.
		this.viewport = computeViewport(dc);

		// Compute the current clip plane distances.
		this.nearClipDistance = this.computeNearClipDistance();
		this.farClipDistance = this.computeFarClipDistance();

		// Compute the current projection matrix.
		this.projection = computeProjection();
		// Compute the current frustum.
		this.frustum = Frustum.fromProjectionMatrix(this.projection);

		//========== load GL matrix state ==========//
		loadGLViewState(dc, this.modelview, this.projection);

		//========== after apply (GL matrix state) ==========//
		afterDoApply();
	}

	protected void afterDoApply()
	{
		// Establish frame-specific values.
		this.lastEyePosition = this.computeEyePositionFromModelview();
		this.horizonDistance = this.computeHorizonDistance();

		// Clear cached computations.
		this.lastEyePoint = null;
		this.lastUpVector = null;
		this.lastForwardVector = null;
		this.lastFrustumInModelCoords = null;
	}

	@Override
	protected double computeNearDistance(Position eyePosition)
	{
		double near = super.computeNearDistance(eyePosition);
		return near < minimumNearDistance ? minimumNearDistance : near;
	}

	@Override
	protected double computeFarDistance(Position eyePosition)
	{
		double far = super.computeFarDistance(eyePosition);
		if (eyePosition.getAltitude() < 0)
		{
			far = Math.max(minimumFarDistance, far);
		}
		return far;
	}

	@Override
	public void copyViewState(View view)
	{
		this.globe = view.getGlobe();

		Matrix transform = view.getModelviewMatrix();
		setHeading(ViewUtil.computeHeading(transform));
		setPitch(ViewUtil.computePitch(transform));
		setRoll(ViewUtil.computeRoll(transform));

		setEyePosition(view.getCurrentEyePosition());
	}
}
