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
package au.gov.ga.earthsci.core.worldwind;

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
import gov.nasa.worldwind.view.BasicViewPropertyLimits;
import gov.nasa.worldwind.view.ViewUtil;

import javax.media.opengl.GL;

/**
 * {@link View} used by the application.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WorldWindView extends AbstractView
{
	protected final WorldWindViewState state = new WorldWindViewState();

	protected Angle lastHeading;
	protected Angle lastPitch;
	protected Angle lastRoll;

	protected final static double DEFAULT_MIN_ELEVATION = 0;
	protected final static double DEFAULT_MAX_ELEVATION = 4000000;
	protected final static Angle DEFAULT_MIN_PITCH = Angle.ZERO;
	protected final static Angle DEFAULT_MAX_PITCH = Angle.fromDegrees(180);
	protected boolean configurationValuesLoaded = false;

	public WorldWindView()
	{
		this.viewInputHandler = new WorldWindViewInputHandler();

		this.viewLimits = new BasicViewPropertyLimits();
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

	@Override
	public Vec4 getCurrentEyePoint()
	{
		return state.getEye();
	}

	@Override
	public Position getCurrentEyePosition()
	{
		return globe.computePositionFromPoint(getCurrentEyePoint());
	}

	@Override
	public void setOrientation(Position eyePosition, Position centerPosition)
	{
		Vec4 eyePoint = globe.computePointFromPosition(eyePosition);
		Vec4 lookAtPoint = globe.computePointFromPosition(centerPosition);
		state.setCenter(lookAtPoint);
		state.setEye(eyePoint);
	}

	@Override
	public void setEyePosition(Position eyePosition)
	{
		state.setEye(globe.computePointFromPosition(eyePosition));
	}

	@Override
	public Angle getHeading()
	{
		if (lastHeading == null)
		{
			lastHeading = ViewUtil.computeHeading(modelview);
		}
		return lastHeading;
	}

	@Override
	public void setHeading(Angle heading)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Angle getPitch()
	{
		if (lastPitch == null)
		{
			lastPitch = ViewUtil.computePitch(modelview);
		}
		return lastPitch;
	}

	@Override
	public void setPitch(Angle pitch)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Angle getRoll()
	{
		if (lastRoll == null)
		{
			lastRoll = ViewUtil.computeRoll(modelview);
		}
		return lastRoll;
	}

	@Override
	public void setRoll(Angle roll)
	{
		// TODO Auto-generated method stub
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

		if (!configurationValuesLoaded)
		{
			loadConfigurationValues();
			configurationValuesLoaded = true;
		}

		//========== modelview matrix state ==========//
		// Compute the current modelview matrix.
		//this.modelview = gluLookAt(eyePoint, lookAtPoint, Vec4.UNIT_Y);
		this.modelview = state.getTransform();
		//this.modelview = ViewUtil.computeTransformMatrix(this.globe, this.eyePosition, this.heading, this.pitch, this.roll);

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



		//TEMP
		Position oldEyePosition = getEyePosition();
		oldEyePosition = Position.fromDegrees(-20, oldEyePosition.longitude.degrees, oldEyePosition.elevation);
		Position newEyePosition = oldEyePosition.add(Position.fromDegrees(0, 1.0));
		setEyePosition(newEyePosition);
		//state.setRoll(newEyePosition.longitude);
		updateModelViewStateID();


		//state.setWorldRoll(Angle.fromDegrees(30));
		//System.out.println(state.getWorldRoll());
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
		this.lastHeading = null;
		this.lastPitch = null;
		this.lastRoll = null;
	}

	protected static final double EPSILON = 1.0e-6;

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
	}
}
