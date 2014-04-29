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

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.ViewPropertyLimits;
import gov.nasa.worldwind.view.orbit.BasicOrbitViewLimits;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;

import javax.media.opengl.GL;

/**
 * Better {@link OrbitView} implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BaseOrbitView extends AbstractView implements OrbitView
{
	protected final static double DEFAULT_MIN_ELEVATION = 0;
	protected final static double DEFAULT_MAX_ELEVATION = 4000000;
	protected final static Angle DEFAULT_MIN_PITCH = Angle.fromDegrees(0);
	protected final static Angle DEFAULT_MAX_PITCH = Angle.fromDegrees(120);

	protected final IViewState state;
	protected OrbitViewLimits viewLimits;

	protected boolean outOfFocus = false;
	protected final BaseOrbitViewCollisionSupport collisionSupport = new BaseOrbitViewCollisionSupport();
	protected boolean resolvingCollisions;

	protected Position appliedEyePosition;
	protected Vec4 appliedEyePoint;
	protected Position unsetEyePosition;

	public BaseOrbitView()
	{
		this.state = createViewState();
		this.viewInputHandler = createViewInputHandler();
		this.viewLimits = createOrbitViewLimits();

		this.collisionSupport.setCollisionThreshold(COLLISION_THRESHOLD);
		this.collisionSupport.setNumIterations(COLLISION_NUM_ITERATIONS);

		loadConfigurationValues();
	}

	protected IViewState createViewState()
	{
		return new ViewState();
	}

	protected ViewInputHandler createViewInputHandler()
	{
		return (ViewInputHandler) WorldWind.createConfigurationComponent(AVKey.VIEW_INPUT_HANDLER_CLASS_NAME);
	}

	protected OrbitViewLimits createOrbitViewLimits()
	{
		OrbitViewLimits viewLimits = new BasicOrbitViewLimits();
		viewLimits.setPitchLimits(DEFAULT_MIN_PITCH, DEFAULT_MAX_PITCH);
		viewLimits.setEyeElevationLimits(DEFAULT_MIN_ELEVATION, DEFAULT_MAX_ELEVATION);
		return viewLimits;
	}

	protected void loadConfigurationValues()
	{
		Position center = getCenterPosition();
		Double initLat = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE);
		Double initLon = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE);
		double initElev = center.getElevation();
		// Set center latitude and longitude. Do not change center elevation.
		if (initLat != null && initLon != null)
		{
			setCenterPosition(Position.fromDegrees(initLat, initLon, initElev));
		}
		else if (initLat != null)
		{
			setCenterPosition(Position.fromDegrees(initLat, center.getLongitude().degrees, initElev));
		}
		else if (initLon != null)
		{
			setCenterPosition(Position.fromDegrees(center.getLatitude().degrees, initLon, initElev));
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

		Double initAltitude = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE);
		if (initAltitude != null)
		{
			setZoom(initAltitude);
		}

		Double initFov = Configuration.getDoubleValue(AVKey.FOV);
		if (initFov != null)
		{
			setFieldOfView(Angle.fromDegrees(initFov));
		}
	}

	public IViewState getState()
	{
		return state;
	}

	@Override
	public Position getEyePosition()
	{
		if (appliedEyePosition != null)
		{
			return appliedEyePosition;
		}
		return getCurrentEyePosition();
	}

	@Override
	public Vec4 getEyePoint()
	{
		if (appliedEyePoint != null)
		{
			return appliedEyePoint;
		}
		return getCurrentEyePoint();
	}

	@Override
	public Vec4 getUpVector()
	{
		if (globe == null)
		{
			return Vec4.ZERO;
		}
		return state.getUp(globe);
	}

	@Override
	public Vec4 getForwardVector()
	{
		if (globe == null)
		{
			return Vec4.ZERO;
		}
		return state.getForward(globe);
	}

	@Override
	public Vec4 getCenterPoint()
	{
		if (globe == null)
		{
			return Vec4.ZERO;
		}
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
		resolveCollisionsWithPitch();
		markOutOfFocus();
	}

	@Override
	public Vec4 getCurrentEyePoint()
	{
		if (globe == null)
		{
			return Vec4.ZERO;
		}
		return state.getEyePoint(globe);
	}

	@Override
	public Position getCurrentEyePosition()
	{
		if (globe == null)
		{
			if (unsetEyePosition != null)
			{
				return unsetEyePosition;
			}
			return Position.ZERO;
		}
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
		if (globe == null)
		{
			unsetEyePosition = eyePosition;
			return;
		}
		unsetEyePosition = null;
		state.setEye(eyePosition, globe);
		resolveCollisionsWithPitch();
		markOutOfFocus();
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
		resolveCollisionsWithPitch();
		markOutOfFocus();
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
		resolveCollisionsWithPitch();
		focusOnViewportCenter();
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
		resolveCollisionsWithPitch();
		focusOnViewportCenter();
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
		focusOnViewportCenter();
	}

	protected void resolveCollisionsWithPitch()
	{
		if (this.dc == null)
		{
			return;
		}

		if (!isDetectCollisions() || resolvingCollisions)
		{
			return;
		}

		resolvingCollisions = true;
		// Compute the near distance corresponding to the current set of values.
		// If there is no collision, 'newPitch' will be null. Otherwise it will contain a value
		// that will resolve the collision.
		double nearDistance = this.computeNearDistance(this.getCurrentEyePosition());
		Angle newPitch = this.collisionSupport.computePitchToResolveCollision(this, nearDistance, this.dc);
		if (newPitch != null)
		{
			setPitch(newPitch);
			flagHadCollisions();
		}
		resolvingCollisions = false;
	}

	protected void flagHadCollisions()
	{
		this.hadCollisions = true;
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

	protected void markOutOfFocus()
	{
		outOfFocus = true;
	}

	@Override
	public boolean canFocusOnViewportCenter()
	{
		if (this.dc == null || this.globe == null)
		{
			//cannot focus on viewport center until the view has been applied at least once
			return false;
		}
		if (this.isAnimating())
		{
			//don't change the viewport center (rotation point) while the user is in the middle of changing the view
			return false;
		}
		if (Math.abs(this.getPitch().degrees) >= 90)
		{
			//don't try and focus on the viewport center if the user is pitched below the surface
			return false;
		}
		if (this.dc.getViewportCenterPosition() == null)
		{
			//cannot focus on a null point!
			return false;
		}
		return true;
	}

	@Override
	public void focusOnViewportCenter()
	{
		if (!canFocusOnViewportCenter())
		{
			return;
		}
		if (!outOfFocus)
		{
			return;
		}
		outOfFocus = false;

		//calculate the center point in cartesian space
		Position viewportCenter = this.dc.getViewportCenterPosition();
		double elevation = this.globe.getElevation(viewportCenter.latitude, viewportCenter.longitude);
		Position viewportExaggerated = new Position(viewportCenter, elevation * dc.getVerticalExaggeration());
		Vec4 viewportCenterPoint = this.globe.computePointFromPosition(viewportExaggerated);

		//find a point along the forward vector so the view doesn't appear to change, only the distance from the center point
		Vec4 eyePoint = getCurrentEyePoint();
		Vec4 forward = getForwardVector();
		double distance = eyePoint.distanceTo3(viewportCenterPoint);
		Vec4 newCenterPoint = Vec4.fromLine3(eyePoint, distance, forward);
		state.setCenter(globe.computePositionFromPoint(newCenterPoint));
		state.setZoom(distance);
	}

	@Override
	public void stopMovementOnCenter()
	{
		firePropertyChange(CENTER_STOPPED, null, null);
	}

	@Override
	protected double computeFarClipDistance()
	{
		return Math.max(globe.getDiameter(), super.computeFarClipDistance());
	}

	@Override
	protected double computeNearClipDistance()
	{
		return Math.min(getZoom(), super.computeNearClipDistance());
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

		if (unsetEyePosition != null)
		{
			setEyePosition(unsetEyePosition);
		}

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
		this.projection = computeProjection(this.nearClipDistance, this.farClipDistance);

		// Compute the current frustum.
		this.frustum = computeFrustum(this.nearClipDistance, this.farClipDistance);

		//========== load GL matrix state ==========//
		loadGLViewState(dc, this.modelview, this.projection);

		afterDoApply();
	}

	protected void afterDoApply()
	{
		// Establish frame-specific values.
		this.horizonDistance = this.computeHorizonDistance();
		this.appliedEyePosition = getCurrentEyePosition();
		this.appliedEyePoint = getCurrentEyePoint();

		// Clear cached computations.
		this.lastFrustumInModelCoords = null;
	}

	protected void beforeComputeMatrices()
	{
	}

	protected java.awt.Rectangle computeViewport(DrawContext dc)
	{
		int[] viewportArray = new int[4];
		this.dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
		return new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);
	}

	protected Matrix computeModelView()
	{
		if (globe == null)
		{
			return Matrix.IDENTITY;
		}
		return state.getTransform(globe);
	}

	protected Matrix computeProjection(double nearDistance, double farDistance)
	{
		double viewportWidth = this.viewport.width <= 0.0 ? 1.0 : this.viewport.width;
		double viewportHeight = this.viewport.height <= 0.0 ? 1.0 : this.viewport.height;
		return Matrix.fromPerspective(this.fieldOfView, viewportWidth, viewportHeight, nearDistance, farDistance);
	}

	protected Frustum computeFrustum(double nearDistance, double farDistance)
	{
		int viewportWidth = this.viewport.width <= 0.0 ? 1 : (int) this.viewport.width;
		int viewportHeight = this.viewport.height <= 0.0 ? 1 : (int) this.viewport.height;
		return Frustum.fromPerspective(this.fieldOfView, viewportWidth, viewportHeight, nearDistance, farDistance);
	}
}
