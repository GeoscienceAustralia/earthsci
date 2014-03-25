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
package au.gov.ga.earthsci.core.worldwind.view;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.ViewPropertyLimits;
import gov.nasa.worldwind.view.orbit.BasicOrbitViewLimits;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;

import javax.media.opengl.GL;

/**
 * {@link View} used by the application.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WorldWindView extends AbstractView
{
	protected final ViewState state = new ViewState();

	protected OrbitViewLimits viewLimits;

	protected final static double DEFAULT_MIN_ELEVATION = 0;
	protected final static double DEFAULT_MAX_ELEVATION = 4000000;
	protected final static Angle DEFAULT_MIN_PITCH = Angle.ZERO;
	protected final static Angle DEFAULT_MAX_PITCH = Angle.fromDegrees(180);
	protected boolean configurationValuesLoaded = false;

	public WorldWindView()
	{
		this.viewInputHandler = new WorldWindViewInputHandler();

		this.viewLimits = new BasicOrbitViewLimits();
		this.viewLimits.setPitchLimits(DEFAULT_MIN_PITCH, DEFAULT_MAX_PITCH);
		this.viewLimits.setEyeElevationLimits(DEFAULT_MIN_ELEVATION, DEFAULT_MAX_ELEVATION);
	}

	protected void loadConfigurationValues()
	{
		Double initLat = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE);
		Double initLon = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE);
		double initElev = 50000.0;

		// Set center latitude and longitude. Do not change center elevation.
		Double initAltitude = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE);
		Position eyePosition = getCurrentEyePosition();
		if (initAltitude != null)
		{
			initElev = initAltitude;
		}
		if (initLat != null && initLon != null)
		{
			setEyePosition(Position.fromDegrees(initLat, initLon, initElev));
		}

		// Set only center latitude. Do not change center longitude or center elevation.
		else if (initLat != null)
		{
			setEyePosition(Position.fromDegrees(initLat, eyePosition.getLongitude().degrees, initElev));
		}
		else if (initLon != null)
		{
			setEyePosition(Position.fromDegrees(eyePosition.getLatitude().degrees, initLon, initElev));
		}

		Double initHeading = Configuration.getDoubleValue(AVKey.INITIAL_HEADING);
		if (initHeading != null)
		{
			setHeading(Angle.fromDegrees(initHeading));
		}

		Double initPitch = Configuration.getDoubleValue(AVKey.INITIAL_PITCH);
		if (initPitch != null)
		{
			setPitch(Angle.fromDegrees(initPitch));
		}

		Double initFov = Configuration.getDoubleValue(AVKey.FOV);
		if (initFov != null)
		{
			setFieldOfView(Angle.fromDegrees(initFov));
		}
	}

	public ViewState getState()
	{
		return state;
	}

	@Override
	public Position getEyePosition()
	{
		return state.getEye(globe);
	}

	@Override
	public Vec4 getEyePoint()
	{
		return state.getEyePoint(globe);
	}

	@Override
	public Vec4 getUpVector()
	{
		return state.getUp(globe);
	}

	@Override
	public Vec4 getForwardVector()
	{
		return state.getForward(globe);
	}

	@Override
	public Vec4 getCenterPoint()
	{
		return state.getCenterPoint(globe);
	}

	@Override
	public Position getCenterPosition()
	{
		return state.getCenter();
	}

	@Override
	public void setCenterPosition(Position center)
	{
		state.setCenter(center);
		focusOnViewportCenterIfPossible();
	}

	@Override
	public Vec4 getCurrentEyePoint()
	{
		return state.getEyePoint(globe);
	}

	@Override
	public Position getCurrentEyePosition()
	{
		return state.getEye(globe);
	}

	@Override
	public void setOrientation(Position eyePosition, Position centerPosition)
	{
		setCenterPosition(centerPosition);
		setEyePosition(eyePosition);
	}

	@Override
	public void setEyePosition(Position eyePosition)
	{
		state.setEye(eyePosition, globe);
		focusOnViewportCenterIfPossible();
	}

	@Override
	public Angle getHeading()
	{
		return state.getHeading();
	}

	@Override
	public void setHeading(Angle heading)
	{
		state.setHeading(heading);
		focusOnViewportCenterIfPossible();
	}

	@Override
	public Angle getPitch()
	{
		return state.getPitch();
	}

	@Override
	public void setPitch(Angle pitch)
	{
		state.setPitch(pitch);
		focusOnViewportCenterIfPossible();
	}

	@Override
	public Angle getRoll()
	{
		return state.getRoll();
	}

	@Override
	public void setRoll(Angle roll)
	{
		state.setRoll(roll);
		focusOnViewportCenterIfPossible();
	}

	@Override
	public double getZoom()
	{
		return state.getZoom();
	}

	@Override
	public void setZoom(double zoom)
	{
		state.setZoom(zoom);
		focusOnViewportCenterIfPossible();
	}

	@Override
	public ViewPropertyLimits getViewPropertyLimits()
	{
		return viewLimits;
	}

	@Override
	public OrbitViewLimits getOrbitViewLimits()
	{
		return viewLimits;
	}

	@Override
	public void setOrbitViewLimits(OrbitViewLimits limits)
	{
		this.viewLimits = limits;
	}

	public void focusOnViewportCenterIfPossible()
	{
		if (canFocusOnViewportCenter())
		{
			focusOnViewportCenter();
		}
	}

	@Override
	public boolean canFocusOnViewportCenter()
	{
		return this.dc != null && this.globe != null
				&& this.dc.getViewportCenterPosition() != null;
	}

	@Override
	public void focusOnViewportCenter()
	{
		if (this.isAnimating())
		{
			return;
		}
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

		Position viewportCenterPos = this.dc.getViewportCenterPosition();
		if (viewportCenterPos == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextViewportCenterIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		// We want the actual "geometric point" here, which must be adjusted for vertical exaggeration.
		Vec4 viewportCenterPoint = this.globe.computePointFromPosition(
				viewportCenterPos.getLatitude(), viewportCenterPos.getLongitude(),
				this.globe.getElevation(viewportCenterPos.getLatitude(), viewportCenterPos.getLongitude())
						* dc.getVerticalExaggeration());

		if (viewportCenterPoint != null)
		{
			Vec4 eyePoint = getEyePoint();
			Vec4 forward = getForwardVector();
			double distance = eyePoint.distanceTo3(viewportCenterPoint);
			Vec4 newCenterPoint = Vec4.fromLine3(eyePoint, distance, forward);
			state.setCenter(globe.computePositionFromPoint(newCenterPoint));
			state.setZoom(distance);

			/*Matrix modelview = state.getTransform(globe);
			if (modelview != null)
			{
				Matrix modelviewInv = modelview.getInverse();
				if (modelviewInv != null)
				{
					// The change in focus must happen seamlessly; we can't move the eye or the forward vector
					// (only the center position and zoom should change). Therefore we pick a point along the
					// forward vector, and *near* the viewportCenterPoint, but not necessarily at the
					// viewportCenterPoint itself.
					Vec4 eyePoint = Vec4.UNIT_W.transformBy4(modelviewInv);
					Vec4 forward = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelviewInv);
					double distance = eyePoint.distanceTo3(viewportCenterPoint);
					Vec4 newCenterPoint = Vec4.fromLine3(eyePoint, distance, forward);

					OrbitViewInputSupport.OrbitViewState modelCoords = OrbitViewInputSupport.computeOrbitViewState(
							this.globe, modelview, newCenterPoint);
					if (validateModelCoordinates(modelCoords))
					{
						setModelCoordinates(modelCoords);
					}
				}
			}*/
		}
	}

	@Override
	public void stopMovementOnCenter()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void doApply(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull"); //$NON-NLS-1$
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (dc.getGL() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGLIsNull"); //$NON-NLS-1$
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (dc.getGlobe() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull"); //$NON-NLS-1$
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Update DrawContext and Globe references.
		this.dc = dc;
		this.globe = this.dc.getGlobe();

		if (!configurationValuesLoaded)
		{
			loadConfigurationValues();
			configurationValuesLoaded = true;
		}

		//========== modelview matrix state ==========//
		// Compute the current modelview matrix.
		this.modelview = state.getTransform(globe);

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
		int[] viewportArray = new int[4];
		this.dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
		this.viewport = new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);

		// Compute the current clip plane distances.
		this.nearClipDistance = this.computeNearClipDistance();
		this.farClipDistance = this.computeFarClipDistance();

		// Compute the current viewport dimensions.
		double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0 : this.viewport.getWidth();
		double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0 : this.viewport.getHeight();

		// Compute the current projection matrix.
		this.projection =
				Matrix.fromPerspective(this.fieldOfView, viewportWidth, viewportHeight, this.nearClipDistance,
						this.farClipDistance);

		// Compute the current frustum.
		this.frustum = Frustum.fromPerspective(this.fieldOfView, (int) viewportWidth, (int) viewportHeight,
				this.nearClipDistance, this.farClipDistance);

		//========== load GL matrix state ==========//
		loadGLViewState(dc, this.modelview, this.projection);

		afterDoApply();
	}

	protected void afterDoApply()
	{
		// Establish frame-specific values.
		this.horizonDistance = this.computeHorizonDistance();

		// Clear cached computations.
		this.lastFrustumInModelCoords = null;
	}

	/*protected static final double EPSILON = 1.0e-6;

	public static Matrix gluLookAt(Vec4 eye, Vec4 center, Vec4 up)
	{
		if (eye.distanceTo3(center) <= EPSILON)
		{
			String msg = Logging.getMessage("Geom.EyeAndCenterInvalid", eye, center);
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Vec4 forward = center.subtract3(eye);
		Vec4 f = forward.normalize3();

		Vec4 s = f.cross3(up);
		s = s.normalize3();

		if (s.getLength3() <= EPSILON)
		{
			String msg = Logging.getMessage("Geom.UpAndLineOfSightInvalid", up, forward);
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Vec4 u = s.cross3(f);
		u = u.normalize3();

		Matrix mAxes = new Matrix(
				s.x, s.y, s.z, 0.0,
				u.x, u.y, u.z, 0.0,
				-f.x, -f.y, -f.z, 0.0,
				0.0, 0.0, 0.0, 1.0);
		Matrix mEye = Matrix.fromTranslation(-eye.x, -eye.y, -eye.z);
		return mAxes.multiply(mEye);
	}*/
}
