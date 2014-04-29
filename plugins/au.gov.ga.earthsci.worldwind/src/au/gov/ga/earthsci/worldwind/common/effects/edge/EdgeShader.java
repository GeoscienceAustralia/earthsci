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
package au.gov.ga.earthsci.worldwind.common.effects.edge;

import java.io.InputStream;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.Shader;

/**
 * Shader used by the {@link EdgeDetectionEffect}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EdgeShader extends Shader
{
	private int textureWidthUniform;
	private int textureHeightUniform;

	public void use(GL2 gl, int textureWidth, int textureHeight)
	{
		super.use(gl);
		gl.glUniform1f(textureWidthUniform, textureWidth);
		gl.glUniform1f(textureHeightUniform, textureHeight);
	}

	@Override
	protected InputStream getVertexSource()
	{
		return this.getClass().getResourceAsStream("EdgeDetectionVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return this.getClass().getResourceAsStream("EdgeDetectionFragmentShader.glsl");
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "colorTexture"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "depthTexture"), 1);
		textureWidthUniform = gl.glGetUniformLocation(shaderProgram, "textureWidth");
		textureHeightUniform = gl.glGetUniformLocation(shaderProgram, "textureHeight");
	}
}
