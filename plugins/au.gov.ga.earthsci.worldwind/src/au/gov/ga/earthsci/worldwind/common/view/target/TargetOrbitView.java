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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.FloatBuffer;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.view.orbit.BaseOrbitView;

/**
 * {@link OrbitView} implementation of {@link ITargetView}.
 * <p/>
 * Also draws an optional axis marker whenever the view changes to indicate the
 * current center of rotation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TargetOrbitView extends BaseOrbitView implements ITargetView
{
	protected boolean targetMode = false;
	protected boolean prioritizeFarClipping = true;

	protected static final double MINIMUM_NEAR_DISTANCE = 1;
	protected static final double MAXIMUM_FAR_NEAR_RATIO = 10000;
	protected static final double MAXIMUM_NEAR_FAR_RATIO = 100000;

	protected boolean nonTargetModeDetectCollisions = true;
	protected boolean targetModeDetectCollisions = false;
	protected Angle nonTargetMaxPitch = DEFAULT_MAX_PITCH;
	protected Angle targetMaxPitch = Angle.fromDegrees(170);

	protected boolean drawAxisMarker = true;
	protected Vec4 lastEye = Vec4.ZERO;
	protected final AxisRenderable axisMarker = new AxisRenderable();
	protected final EmptyScreenCredit viewScreenCredit = new EmptyScreenCredit()
	{
		@Override
		public void render(DrawContext dc)
		{
			TargetOrbitView.this.render(dc);
		}
	};

	protected final FloatBuffer depthPixel = FloatBuffer.allocate(1);
	protected Position mousePosition;

	@Override
	public boolean isTargetMode()
	{
		return targetMode;
	}

	@Override
	public void setTargetMode(boolean targetMode)
	{
		if (this.targetMode == targetMode)
		{
			return;
		}

		this.targetMode = targetMode;

		Angle[] pitchLimits = this.viewLimits.getPitchLimits();
		if (targetMode)
		{
			this.nonTargetMaxPitch = pitchLimits[1];
			pitchLimits[1] = this.targetMaxPitch;
			this.nonTargetModeDetectCollisions = isDetectCollisions();
			setDetectCollisions(this.targetModeDetectCollisions);
		}
		else
		{
			this.targetMaxPitch = pitchLimits[1];
			pitchLimits[1] = this.nonTargetMaxPitch;
			this.targetModeDetectCollisions = isDetectCollisions();
			setDetectCollisions(this.nonTargetModeDetectCollisions);
		}
		this.viewLimits.setPitchLimits(pitchLimits[0], pitchLimits[1]);
	}

	@Override
	public boolean isDrawAxisMarker()
	{
		return drawAxisMarker;
	}

	@Override
	public void setDrawAxisMarker(boolean drawAxisMarker)
	{
		this.drawAxisMarker = drawAxisMarker;
	}

	@Override
	public boolean isPrioritizeFarClipping()
	{
		return prioritizeFarClipping;
	}

	@Override
	public void setPrioritizeFarClipping(boolean prioritizeFarClipping)
	{
		this.prioritizeFarClipping = prioritizeFarClipping;
		firePropertyChange(AVKey.VIEW, null, this);
	}

	@Override
	public AxisRenderable getAxisMarker()
	{
		return axisMarker;
	}

	@Override
	public Position getMousePosition()
	{
		return mousePosition;
	}

	@Override
	public void focusOnViewportCenter()
	{
		if (isTargetMode())
		{
			//if we are in target mode, the center point can be changed by the user, so don't change it automatically
			return;
		}

		super.focusOnViewportCenter();
	}

	@Override
	protected void doApply(DrawContext dc)
	{
		super.doApply(dc);

		//the screen credits are stored in a map, so adding this each frame is not a problem
		dc.addScreenCredit(viewScreenCredit);

		if (isDrawAxisMarker())
		{
			Vec4 eye = getEyePoint();
			if (lastEye.distanceToSquared3(eye) > 10)
			{
				//view has changed, so show the axis marker
				axisMarker.trigger();
			}
			lastEye = eye;
		}
	}

	/**
	 * Render method is called during the rendering of the scene controller's
	 * screen credits, but the {@link #viewScreenCredit}.
	 * 
	 * @param dc
	 */
	protected void render(DrawContext dc)
	{
		if (isDrawAxisMarker())
		{
			axisMarker.render(dc);
		}

		if (viewInputHandler instanceof TargetOrbitViewInputHandler)
		{
			//calculate mouse position in geographic coordinates

			Point mousePoint = ((TargetOrbitViewInputHandler) viewInputHandler).getMousePoint();
			if (mousePoint == null)
			{
				mousePosition = null;
				return;
			}

			GL2 gl = dc.getGL().getGL2();
			Rectangle viewport = getViewport();
			int winX = mousePoint.x;
			int winY = viewport.height - mousePoint.y - 1;
			gl.glReadPixels(winX, winY, 1, 1, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, depthPixel.rewind());
			float winZ = depthPixel.get(0);

			//see gluUnproject:
			Matrix mvpi = projection.multiply(modelview).getInverse();
			Vec4 screen = new Vec4(
					2.0 * (winX - viewport.x) / viewport.width - 1.0,
					2.0 * (winY - viewport.y) / viewport.height - 1.0,
					2.0 * winZ - 1.0, 1.0);
			Vec4 model = screen.transformBy4(mvpi);
			model = model.divide3(model.w);
			mousePosition = globe.computePositionFromPoint(model);
		}
	}

	@Override
	protected double computeNearClipDistance()
	{
		double near = Math.max(super.computeNearClipDistance(), MINIMUM_NEAR_DISTANCE);
		if (shouldPrioritizeFarClipping())
		{
			double far = computeFarClipDistance();
			return Math.max(near, far / MAXIMUM_FAR_NEAR_RATIO);
		}
		return near;
	}

	@Override
	protected double computeFarClipDistance()
	{
		double elevation = getCurrentEyePosition().elevation;
		double far = elevation + globe.getDiameter();
		if (!shouldPrioritizeFarClipping())
		{
			double near = computeNearClipDistance();
			return Math.min(far, near * MAXIMUM_NEAR_FAR_RATIO);
		}
		return far;
	}

	protected boolean shouldPrioritizeFarClipping()
	{
		//always prioritize far clipping when below the earth's surface
		return isPrioritizeFarClipping() || isBelowSurface();
	}

	protected boolean isBelowSurface()
	{
		if (dc == null)
		{
			return false;
		}
		Position eyePosition = getEyePosition();
		double altitude = ViewUtil.computeElevationAboveSurface(dc, eyePosition);
		return altitude < 0;
	}
}
