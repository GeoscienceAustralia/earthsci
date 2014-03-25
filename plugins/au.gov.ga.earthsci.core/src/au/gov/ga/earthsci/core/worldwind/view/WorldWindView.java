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
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.ViewPropertyLimits;
import gov.nasa.worldwind.view.orbit.BasicOrbitViewLimits;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;

import java.util.Random;

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
		return globe.computePositionFromPoint(getEyePoint());
	}

	@Override
	public Vec4 getEyePoint()
	{
		return state.getEye();
	}

	@Override
	public Vec4 getUpVector()
	{
		return state.getUp();
	}

	@Override
	public Vec4 getForwardVector()
	{
		return state.getForward();
	}

	@Override
	public Vec4 getCenterPoint()
	{
		return state.getCenter();
	}

	@Override
	public Position getCenterPosition()
	{
		return globe.computePositionFromPoint(getCenterPoint());
	}

	@Override
	public void setCenterPosition(Position center)
	{
		state.setCenter(globe.computePointFromPosition(center));
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
		setCenterPosition(centerPosition);
		setEyePosition(eyePosition);
	}

	@Override
	public void setEyePosition(Position eyePosition)
	{
		state.setEye(globe.computePointFromPosition(eyePosition));
	}

	@Override
	public Angle getHeading()
	{
		//TODO find a more sensible way of performing this calculation
		//TODO cache the value
		Vec4 center = state.getCenter();
		Vec4 up = state.getUp();
		Position centerPosition = globe.computePositionFromPoint(center);
		Position centerUpPosition = globe.computePositionFromPoint(center.add3(up));
		return LatLon.greatCircleAzimuth(centerPosition, centerUpPosition);
	}

	@Override
	public void setHeading(Angle heading)
	{
		Angle current = getHeading();
		Angle delta = heading.subtract(current).normalizedLongitude();
		rotateAroundCenter(delta);
	}

	public void rotateAroundCenter(Angle delta)
	{
		Vec4 center = state.getCenter();
		if (center.getLengthSquared3() == 0)
		{
			Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, delta);
			state.setRotation(state.getRotation().multiply(q));
		}
		else
		{
			//use the center vector as the axis of rotation
			Quaternion q = Quaternion.fromAxisAngle(delta.multiply(-1), center.normalize3());
			state.setRotation(q.multiply(state.getRotation()));
		}
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
	}

	@Override
	public double getZoom()
	{
		return state.getDistance();
	}

	@Override
	public void setZoom(double zoom)
	{
		state.setDistance(zoom);
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

	@Override
	public boolean canFocusOnViewportCenter()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void focusOnViewportCenter()
	{
		// TODO Auto-generated method stub

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



			Position eyePosition = getEyePosition();
			eyePosition = new Position(LatLon.fromDegrees(40, 0), eyePosition.elevation);
			setOrientation(eyePosition, new Position(eyePosition.add(LatLon.fromDegrees(20, 0)), 0));
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
		oldEyePosition = Position.fromDegrees(40, oldEyePosition.longitude.degrees, oldEyePosition.elevation);
		Position newEyePosition = oldEyePosition.add(Position.fromDegrees(0, 1.0));
		//newEyePosition = oldEyePosition.add(Position.fromDegrees(0, 0.0));
		Position newCenterPosition = new Position(newEyePosition.add(Position.fromDegrees(20, 20)), 0);
		//setOrientation(newEyePosition, newCenterPosition);
		//setEyePosition(newEyePosition);
		//setCenterPosition(Position.fromDegrees(-20, 0, 0));
		//state.setRoll(newEyePosition.longitude);
		//updateModelViewStateID();

		//state.setHeading(state.getHeading().addDegrees(1));
		//state.setPitch(state.getPitch().addDegrees(1));
		//System.out.println("heading = " + state.getHeading() + ", pitch = " + state.getPitch());

		//state.setDistance(state.getDistance() - 10000);
	}

	@Deprecated
	Random random = new Random();

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
