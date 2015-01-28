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

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/**
 * Atmosphere layer that renders the sky.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AtmosphereSkyLayer extends AbstractAtmosphereLayer
{
	private final SkyShader skyFromAtmosphereShader = new SkyShader(new String[] { "ATMOS" }); //$NON-NLS-1$
	private final SkyShader skyFromSpaceShader = new SkyShader(new String[] { "SPACE" }); //$NON-NLS-1$

	private Globe lastGlobe;
	private int sphereList = -1;

	@Override
	protected void init(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		skyFromAtmosphereShader.create(gl);
		skyFromSpaceShader.create(gl);
	}

	@Override
	protected int attribBitsToPush()
	{
		return 0;
	}

	@Override
	protected void renderAtmosphere(DrawContext dc, Vec4 lightDirection, Vec4 eyePoint, float eyeMagnitude,
			float innerRadius, float outerRadius)
	{
		GL2 gl = dc.getGL().getGL2();

		SkyShader skyShader = eyeMagnitude < outerRadius ? skyFromAtmosphereShader : skyFromSpaceShader;
		skyShader.use(gl, eyePoint, lightDirection, Atmosphere.INVWAVELENGTH4, eyeMagnitude, innerRadius,
				outerRadius, Atmosphere.RAYLEIGH_SCATTERING, Atmosphere.MIE_SCATTERING,
				Atmosphere.SUN_BRIGHTNESS, Atmosphere.SCALE_DEPTH,
				Atmosphere.MIE_PHASE_ASYMMETRY, Atmosphere.EXPOSURE);
		{
			gl.glFrontFace(GL2.GL_CW);
			gl.glEnable(GL2.GL_CULL_FACE);
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDepthMask(false);

			if (lastGlobe != dc.getGlobe())
			{
				lastGlobe = dc.getGlobe();
				sphereList = -1;
			}

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
		}
		skyShader.unuse(gl);

		gl.glDepthMask(true);
	}

	@Override
	public String toString()
	{
		return "Atmosphere (sky)";
	}
}
