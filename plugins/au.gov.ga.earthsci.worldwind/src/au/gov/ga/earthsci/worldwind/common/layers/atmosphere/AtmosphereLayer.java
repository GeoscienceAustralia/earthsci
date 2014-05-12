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

import java.awt.image.BufferedImage;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

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
	public final static double ATMOSPHERE_SCALE = 1.025;
	public final static double RAYLEIGH_SCATTERING = 0.0025;
	public final static double MIE_SCATTERING = 0.0015;
	public final static double SUN_BRIGHTNESS = 18.0;
	public final static double MIE_PHASE_ASYMMETRY = -0.990;
	public final static double WAVELENGTH[] = new double[] {
			0.731,
			0.612,
			0.455
	};
	public final static double SCALE_DEPTH = 0.25;
	public final static double EXPOSURE = 2.0;

	private boolean inited = false;

	protected Vec4 lightDirection = new Vec4(0, 0, 1000).normalize3();
	protected final float wavelength4[] = new float[] {
			(float) Math.pow(WAVELENGTH[0], 4),
			(float) Math.pow(WAVELENGTH[1], 4),
			(float) Math.pow(WAVELENGTH[2], 4)
	};

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
		double innerRadius = dc.getGlobe().getRadius();
		double outerRadius = innerRadius * ATMOSPHERE_SCALE;
		View view = dc.getView();
		Vec4 eyePoint = view.getEyePoint();
		double eyeMagnitude = eyePoint.getLength3();

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

	protected void renderSky(DrawContext dc, GL2 gl, double eyeMagnitude, Vec4 eyePoint, double innerRadius,
			double outerRadius)
	{
		AtmosphereShader skyShader = eyeMagnitude < outerRadius ? skyFromAtmosphereShader : skyFromSpaceShader;
		skyShader.use(gl, eyePoint, lightDirection, wavelength4, (float) eyeMagnitude, (float) innerRadius,
				(float) outerRadius, (float) RAYLEIGH_SCATTERING, (float) MIE_SCATTERING, (float) SUN_BRIGHTNESS,
				(float) SCALE_DEPTH, (float) MIE_PHASE_ASYMMETRY, (float) EXPOSURE);

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
}
