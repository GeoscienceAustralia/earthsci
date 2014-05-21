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

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.image.BufferedImage;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.FrameBuffer;

/**
 * Atmosphere layer that colors the ground.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AtmosphereGroundLayer extends AbstractAtmosphereLayer
{
	private final GroundShader groundFromAtmosphereShader = new GroundShader(new String[] { "ATMOS" }); //$NON-NLS-1$
	private final GroundShader groundFromSpaceShader = new GroundShader(new String[] { "SPACE" }); //$NON-NLS-1$

	private BlendSurfaceImage terrain;

	private final FrameBuffer frameBuffer = new FrameBuffer();

	@Override
	protected void init(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		groundFromAtmosphereShader.create(gl);
		groundFromSpaceShader.create(gl);

		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, 0xff << 24);
		terrain = new BlendSurfaceImage(image, Sector.FULL_SPHERE);
		terrain.setBlendFunc(GL2.GL_ONE, GL2.GL_ZERO);
	}

	@Override
	protected void renderAtmosphere(DrawContext dc, Vec4 lightDirection, Vec4 eyePoint, float eyeMagnitude,
			float innerRadius, float outerRadius)
	{
		GL2 gl = dc.getGL().getGL2();

		frameBuffer.resize(gl, dc.getView().getViewport().getSize());
		frameBuffer.bind(gl);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		GroundShader groundShader = eyeMagnitude < outerRadius ? groundFromAtmosphereShader : groundFromSpaceShader;
		groundShader.use(gl, eyePoint, lightDirection, Atmosphere.INVWAVELENGTH4, eyeMagnitude, innerRadius,
				outerRadius, Atmosphere.RAYLEIGH_SCATTERING, Atmosphere.MIE_SCATTERING,
				Atmosphere.SUN_BRIGHTNESS, Atmosphere.SCALE_DEPTH, Atmosphere.EXPOSURE * 0.5f,
				dc.getView().getModelviewMatrix().getInverse());
		{
			terrain.preRender(dc);
			terrain.render(dc);
		}
		groundShader.unuse(gl);
		frameBuffer.unbind(gl);

		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);
		FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTexture().getId());
	}
}
