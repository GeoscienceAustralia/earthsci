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
package au.gov.ga.earthsci.worldwind.common.layers.atmosphere;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.sun.SunPositionService;
import au.gov.ga.earthsci.worldwind.common.util.GeometryUtil;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;

/**
 * Layer that renders atmospheric scattering effects.
 * <p/>
 * Based on Sean O'Neil's algorithm described in GPU Gems 2 <a
 * href="http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter16.html"
 * >chapter 16</a>.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractAtmosphereLayer extends AbstractLayer
{
	private boolean inited = false;
	private double[] clippingPlane = new double[4];

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!inited)
		{
			init(dc);
			inited = true;
		}
		renderAtmosphere(dc);
	}

	protected abstract void init(DrawContext dc);

	protected void renderAtmosphere(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		//setup variables
		float innerRadius = (float) dc.getGlobe().getRadius();
		float outerRadius = innerRadius * Atmosphere.ATMOSPHERE_SCALE;
		View view = dc.getView();
		Vec4 eyePoint = view.getEyePoint();
		float eyeMagnitude = (float) eyePoint.getLength3();
		Vec4 lightDirection = SunPositionService.INSTANCE.getDirection(dc.getView());

		OGLStackHandler ogsh = new OGLStackHandler();
		try
		{
			ogsh.pushProjection(gl);
			loadProjection(dc, outerRadius);
			ogsh.pushAttrib(gl, GL2.GL_TEXTURE_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT
					| GL2.GL_POLYGON_BIT | attribBitsToPush());
			gl.glColor4f(1f, 1f, 1f, 1f);
			setupClippingPlane(dc);
			renderAtmosphere(dc, lightDirection, eyePoint, eyeMagnitude, innerRadius, outerRadius);
		}
		finally
		{
			ogsh.pop(gl);
		}
	}

	protected void loadProjection(DrawContext dc, float outerRadius)
	{
		IDelegateView view = (IDelegateView) dc.getView();
		double far = view.getEyePoint().getLength3() + view.getGlobe().getRadius();
		double near = 1000;
		Matrix projection = view.computeProjection(near, far);

		if (projection != null)
		{
			double[] matrixArray = new double[16];
			GL2 gl = dc.getGL().getGL2();
			projection.toArray(matrixArray, 0, false);
			gl.glLoadMatrixd(matrixArray, 0);
		}
	}

	protected void setupClippingPlane(DrawContext dc)
	{
		View view = dc.getView();
		Vec4 forward = view.getForwardVector();
		Vec4 eyeNormalized = view.getEyePoint().normalize3();
		Angle pitch = eyeNormalized.angleBetween3(forward);
		if (pitch.degrees < 90)
		{
			//don't clip the sky when looking up at it
			return;
		}

		Vec4 up = view.getUpVector();
		Vec4 side = forward.cross3(up);
		Vec4 origin = Vec4.ZERO;
		double depth = view.getGlobe().getRadius() * 0.5;
		Vec4 add = forward.multiply3(depth);

		Vec4 v1 = origin.add3(add);
		Vec4 v2 = up.add3(add);
		Vec4 v3 = side.add3(add);

		Line l1 = Line.fromSegment(v1, v3);
		Line l2 = Line.fromSegment(v1, v2);
		Plane plane = GeometryUtil.createPlaneContainingLines(l1, l2);
		Vec4 v = plane.getVector();
		clippingPlane[0] = v.x;
		clippingPlane[1] = v.y;
		clippingPlane[2] = v.z;
		clippingPlane[3] = v.w;

		GL2 gl = dc.getGL().getGL2();
		gl.glEnable(GL2.GL_CLIP_PLANE5);
		gl.glClipPlane(GL2.GL_CLIP_PLANE5, clippingPlane, 0);
	}

	protected abstract int attribBitsToPush();

	protected abstract void renderAtmosphere(DrawContext dc, Vec4 lightDirection, Vec4 eyePoint, float eyeMagnitude,
			float innerRadius, float outerRadius);
}
