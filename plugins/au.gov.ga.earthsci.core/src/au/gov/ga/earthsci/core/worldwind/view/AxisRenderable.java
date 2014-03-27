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
package au.gov.ga.earthsci.core.worldwind.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Rectangle;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 * {@link Renderable} that displays a red/green/blue axis marker at the view's
 * center of rotation point. The red axis lies east-west, the green axis lies
 * north-south, and the blue axis points to the center of the globe. Heading
 * changes always rotate around the blue axis.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AxisRenderable implements Renderable
{
	protected final static double NANO = 1e-9;

	protected double fadeInTime = 0.2;
	protected double fadeOutTime = 0.2;
	protected double onTime = 0.2;

	protected double triggerTime = 0;
	protected double opacity = 0;

	/**
	 * @return Fade in time
	 */
	public double getFadeInTime()
	{
		return fadeInTime;
	}

	/**
	 * Set the length of time it takes for the axis marker to fade in.
	 * 
	 * @param fadeInTime
	 */
	public void setFadeInTime(double fadeInTime)
	{
		this.fadeInTime = Math.max(0, fadeInTime);
	}

	/**
	 * @return Fade out time
	 */
	public double getFadeOutTime()
	{
		return fadeOutTime;
	}

	/**
	 * Set the length of time it takes for the axis marker to fade out.
	 * 
	 * @param fadeOutTime
	 */
	public void setFadeOutTime(double fadeOutTime)
	{
		this.fadeOutTime = Math.max(0, fadeOutTime);
	}

	/**
	 * @return Length of time the axis marker stays on for before fading out
	 */
	public double getOnTime()
	{
		return onTime;
	}

	/**
	 * Set the length of time the axis marker stays on for before fading out.
	 * 
	 * @param onTime
	 */
	public void setOnTime(double onTime)
	{
		this.onTime = Math.max(0, onTime);
	}

	/**
	 * Trigger the axis marker visibility. It will stay visible for
	 * <code>fadeInTime + onTime + fadeOutTime</code>
	 */
	public void trigger()
	{
		triggerTime = System.nanoTime() * NANO - opacity * fadeInTime;
	}

	@Override
	public void render(DrawContext dc)
	{
		double currentTime = System.nanoTime() * NANO;
		double time = currentTime - triggerTime;
		if (time <= 0 || time >= fadeInTime + onTime + fadeOutTime)
		{
			opacity = 0;
		}
		else if (time < fadeInTime)
		{
			opacity = time / fadeInTime;
		}
		else if (time < fadeInTime + onTime)
		{
			opacity = 1;
		}
		else
		{
			opacity = 1 - (time - fadeInTime - onTime) / fadeOutTime;
		}

		if (opacity <= 0)
		{
			return;
		}

		renderAxis(dc, opacity);

		//make sure it renders again for fading:
		dc.getView().firePropertyChange(AVKey.VIEW, null, dc.getView());
	}

	/**
	 * Render the axis marker.
	 * 
	 * @param dc
	 */
	protected void renderAxis(DrawContext dc, double opacity)
	{
		View view = dc.getView();
		Vec4 centerPoint = view.getCenterPoint();
		Vec4 eyePoint = view.getEyePoint();
		if (centerPoint == null || eyePoint == null)
		{
			return;
		}

		GL2 gl = dc.getGL().getGL2();
		OGLStackHandler oglsh = new OGLStackHandler();
		try
		{
			oglsh.pushProjection(gl);
			oglsh.pushModelview(gl);
			oglsh.pushAttrib(gl, GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT | GL2.GL_LINE_BIT);

			double distance = eyePoint.distanceTo3(centerPoint);
			double length = distance / 20;

			Rectangle viewport = view.getViewport();
			Matrix projection = Matrix.fromPerspective(view.getFieldOfView(), viewport.width, viewport.height,
					distance - length, distance + length);

			Position centerPosition = dc.getGlobe().computePositionFromPoint(centerPoint);
			Matrix rotation = Matrix.fromRotationXYZ(Angle.ZERO, centerPosition.longitude, Angle.ZERO).multiply(
					Matrix.fromRotationXYZ(centerPosition.latitude.multiply(-1), Angle.ZERO, Angle.ZERO));
			Matrix modelview = Matrix.fromTranslation(centerPoint).multiply(rotation);

			double[] matrixArray = new double[16];
			gl.glMatrixMode(GL2.GL_PROJECTION);
			projection.toArray(matrixArray, 0, false);
			gl.glLoadMatrixd(matrixArray, 0);
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			modelview.toArray(matrixArray, 0, false);
			gl.glMultMatrixd(matrixArray, 0);

			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

			gl.glLineWidth(2f);
			gl.glBegin(GL.GL_LINES);
			{
				gl.glColor4d(1, 0, 0, opacity);
				gl.glVertex3d(0, 0, 0);
				gl.glVertex3d(length, 0, 0);
				gl.glColor4d(0, 1, 0, opacity);
				gl.glVertex3d(0, 0, 0);
				gl.glVertex3d(0, length, 0);
				gl.glColor4d(0, 0, 1, opacity);
				gl.glVertex3d(0, 0, 0);
				gl.glVertex3d(0, 0, length);
			}
			gl.glEnd();

			gl.glLineStipple(4, (short) 0xaaaa);
			gl.glEnable(GL2.GL_LINE_STIPPLE);
			gl.glBegin(GL.GL_LINES);
			{
				gl.glColor4d(1, 0, 0, opacity);
				gl.glVertex3d(0, 0, 0);
				gl.glVertex3d(-length, 0, 0);
				gl.glColor4d(0, 1, 0, opacity);
				gl.glVertex3d(0, 0, 0);
				gl.glVertex3d(0, -length, 0);
				gl.glColor4d(0, 0, 1, opacity);
				gl.glVertex3d(0, 0, 0);
				gl.glVertex3d(0, 0, -length);
			}
			gl.glEnd();
			gl.glDisable(GL2.GL_LINE_STIPPLE);
		}
		finally
		{
			oglsh.pop(gl);
		}
	}
}
