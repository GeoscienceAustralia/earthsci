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
package au.gov.ga.earthsci.worldwind.common.effects.depthoffield;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;
import java.io.InputStream;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.Shader;

/**
 * Gaussian blur shader that blurs the input in a single direction (must be
 * applied twice, both horizontally and vertically, for a correct Gaussian
 * blur).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GaussianBlurShader extends Shader
{
	private int sigmaUniform;
	private int blurSizeUniform;
	private int horizontalUniform;

	/**
	 * Use this shader.
	 * 
	 * @param dc
	 *            Draw context
	 * @param dimensions
	 *            Dimensions of the input texture
	 * @param horizontal
	 *            Blur horizontally?
	 */
	public void use(DrawContext dc, Dimension dimensions, boolean horizontal)
	{
		GL2 gl = dc.getGL().getGL2();
		super.use(gl);

		gl.glUniform1f(sigmaUniform, 4.0f);
		gl.glUniform1i(horizontalUniform, horizontal ? 1 : 0);
		float blurSize = 1.0f / (horizontal ? dimensions.width : dimensions.height);
		gl.glUniform1f(blurSizeUniform, blurSize);
	}

	@Override
	protected InputStream getVertexSource()
	{
		return this.getClass().getResourceAsStream("GenericVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return this.getClass().getResourceAsStream("GaussianBlurFragmentShader.glsl");
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "blurSampler"), 0);
		sigmaUniform = gl.glGetUniformLocation(shaderProgram, "sigma");
		blurSizeUniform = gl.glGetUniformLocation(shaderProgram, "blurSize");
		horizontalUniform = gl.glGetUniformLocation(shaderProgram, "horizontal");
	}
}
