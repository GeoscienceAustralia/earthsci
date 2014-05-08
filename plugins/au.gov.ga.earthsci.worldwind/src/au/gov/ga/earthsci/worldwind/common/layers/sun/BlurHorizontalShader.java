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
package au.gov.ga.earthsci.worldwind.common.layers.sun;

import java.io.InputStream;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.Shader;

/**
 * Horizontal blur shader.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public class BlurHorizontalShader extends Shader
{
	private int width = -1;
	private int od = -1;

	public void use(GL2 gl, int width, float od)
	{
		super.use(gl);
		gl.glUniform1i(this.width, width);
		gl.glUniform1f(this.od, od);
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "Texture"), 0);
		width = gl.glGetUniformLocation(shaderProgram, "Width");
		od = gl.glGetUniformLocation(shaderProgram, "od");
	}

	@Override
	protected InputStream getVertexSource()
	{
		return getClass().getResourceAsStream("SunVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return getClass().getResourceAsStream("BlurHorizontalFragmentShader.glsl");
	}
}
