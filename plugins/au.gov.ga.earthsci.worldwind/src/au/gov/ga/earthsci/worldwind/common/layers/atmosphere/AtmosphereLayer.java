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
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Layer that renders atmospheric scattering effects.
 * <p/>
 * Based on Sean O'Neil's algorithm described in GPU Gems 2 <a
 * href="http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter16.html"
 * >chapter 16</a>.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AtmosphereLayer extends AbstractLayer
{
	//atmosphere constants
	public final static float ATMOSPHERE_SCALE = 1.025f;
	public final static float RAYLEIGH_SCATTERING = 0.0025f;
	public final static float MIE_SCATTERING = 0.0015f;
	public final static float SUN_BRIGHTNESS = 18.0f;
	public final static float MIE_PHASE_ASYMMETRY = -0.990f;
	public final static float WAVELENGTH[] = new float[] {
			0.731f,
			0.612f,
			0.455f
	};
	public final static float SCALE_DEPTH = 0.25f;
	public final static float EXPOSURE = 2.0f;

	//derived constants
	public final static float INVWAVELENGTH4[] = new float[] {
			1f / (float) Math.pow(WAVELENGTH[0], 4),
			1f / (float) Math.pow(WAVELENGTH[1], 4),
			1f / (float) Math.pow(WAVELENGTH[2], 4)
	};

	private boolean inited = false;

	protected Vec4 lightDirection = new Vec4(0, 0, 1000).normalize3();

	private final AtmosphereShader skyFromAtmosphereShader = new AtmosphereShader(new String[] { "ATMOS" }); //$NON-NLS-1$
	private final AtmosphereShader skyFromSpaceShader = new AtmosphereShader(new String[] { "SPACE" }); //$NON-NLS-1$

	private BlendSurfaceImage terrainMask;
	private int sphereList = -1;
	private Globe lastGlobe;

	@Override
	protected void doRender(DrawContext dc)
	{
		if (lastGlobe != dc.getGlobe())
		{
			lastGlobe = dc.getGlobe();
			sphereList = -1;
		}

		if (!inited)
		{
			initAtmosphere(dc);
			inited = true;
		}
		renderAtmosphere(dc);
	}

	protected void initAtmosphere(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		skyFromAtmosphereShader.create(gl);
		skyFromSpaceShader.create(gl);

		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, 0xff << 24);
		terrainMask = new BlendSurfaceImage(image, Sector.FULL_SPHERE);
		terrainMask.setBlendFunc(GL2.GL_ZERO, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}

	protected void renderAtmosphere(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		//setup variables
		float innerRadius = (float) dc.getGlobe().getRadius();
		float outerRadius = innerRadius * ATMOSPHERE_SCALE;
		View view = dc.getView();
		Vec4 eyePoint = view.getEyePoint();
		float eyeMagnitude = (float) eyePoint.getLength3();

		OGLStackHandler ogsh = new OGLStackHandler();

		try
		{
			ogsh.pushAttrib(gl, GL2.GL_TEXTURE_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT
					| GL2.GL_POLYGON_BIT);

			gl.glDepthMask(false);

			//render the atmosphere
			renderSky(dc, gl, eyeMagnitude, eyePoint, innerRadius, outerRadius);

			//mask out the middle of the sphere using a terrain-following 1x1 black image
			terrainMask.preRender(dc);
			terrainMask.render(dc);
		}
		finally
		{
			ogsh.pop(gl);
		}
	}

	protected void renderSky(DrawContext dc, GL2 gl, float eyeMagnitude, Vec4 eyePoint, float innerRadius,
			float outerRadius)
	{
		AtmosphereShader skyShader = eyeMagnitude < outerRadius ? skyFromAtmosphereShader : skyFromSpaceShader;
		skyShader.use(gl, eyePoint, lightDirection, INVWAVELENGTH4, eyeMagnitude, innerRadius,
				outerRadius, RAYLEIGH_SCATTERING, MIE_SCATTERING, SUN_BRIGHTNESS,
				SCALE_DEPTH, MIE_PHASE_ASYMMETRY, EXPOSURE);

		gl.glFrontFace(GL2.GL_CW);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

		if (sphereList < 0)
		{
			GLU glu = dc.getGLU();
			GLUquadric sphere = glu.gluNewQuadric();
			sphereList = gl.glGenLists(1);
			gl.glNewList(sphereList, GL2.GL_COMPILE);
			glu.gluSphere(sphere, outerRadius, 256, 128);
			gl.glEndList();
			glu.gluDeleteQuadric(sphere);
		}

		gl.glCallList(sphereList);

		gl.glFrontFace(GL2.GL_CCW);
		gl.glDisable(GL2.GL_CULL_FACE);
		skyShader.unuse(gl);
	}

	/**
	 * Get the color of an object at a point in space, using the atmospheric
	 * scattering parameters defined by this layer.
	 * 
	 * @param dc
	 *            Current draw context, from which to get the current globe
	 *            radius and eye point
	 * @param point
	 *            Point to determine the color of
	 * @return Color of the object at the given point
	 */
	public static Color getSpaceObjectColor(DrawContext dc, Vec4 point)
	{
		double innerRadius = dc.getGlobe().getRadius();
		double outerRadius = innerRadius * ATMOSPHERE_SCALE;
		Vec4 eyePoint = dc.getView().getEyePoint();

		double cameraHeight = eyePoint.getLength3();
		double cameraHeight2 = cameraHeight * cameraHeight;

		double outerRadius2 = outerRadius * outerRadius;
		Vec4 ray = point.subtract3(eyePoint);
		double far = ray.getLength3();
		ray = ray.divide3(far);

		double B = 2.0 * eyePoint.dot3(ray);
		double C = cameraHeight2 - outerRadius2;
		double det = Math.max(0.0, B * B - 4.0 * C);
		double near = 0.5 * (-B - Math.sqrt(det));
		Vec4 start = eyePoint;

		if (cameraHeight > outerRadius)
		{
			start = start.add3(ray.multiply3(near));
		}

		double scaleOverScaleDepth = (1f / (outerRadius - innerRadius)) / SCALE_DEPTH;
		double height = start.getLength3();
		double depth = Math.exp(scaleOverScaleDepth * (innerRadius - cameraHeight));
		double angle = ray.dot3(start) / height;
		double scatter = depth * scale(angle, SCALE_DEPTH);

		double fKr4PI = RAYLEIGH_SCATTERING * 4.0 * Math.PI;
		double fKm4PI = MIE_PHASE_ASYMMETRY * 4.0 * Math.PI;
		double r = Util.clamp(Math.exp(-scatter * (INVWAVELENGTH4[0] * fKr4PI + fKm4PI)), 0.0, 1.0);
		double g = Util.clamp(Math.exp(-scatter * (INVWAVELENGTH4[1] * fKr4PI + fKm4PI)), 0.0, 1.0);
		double b = Util.clamp(Math.exp(-scatter * (INVWAVELENGTH4[2] * fKr4PI + fKm4PI)), 0.0, 1.0);
		return new Color((float) r, (float) g, (float) b);
	}

	protected static double scale(double cos, double scaleDepth)
	{
		double x = 1.0 - cos;
		return scaleDepth * Math.exp(-0.00287 + x * (0.459 + x * (3.83 + x * (-6.80 + x * 5.25))));
	}
}
