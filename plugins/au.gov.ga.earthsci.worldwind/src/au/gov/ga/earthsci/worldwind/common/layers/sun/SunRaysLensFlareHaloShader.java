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
 * Shader for computing sun rays, lens flare, halo, and dirty lens.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public class SunRaysLensFlareHaloShader extends Shader
{
	private int sunPosProj = -1;

	public void use(GL2 gl, float projectedSunPosX, float projectedSunPosY)
	{
		if (super.use(gl))
		{
			gl.glUniform2f(sunPosProj, projectedSunPosX, projectedSunPosY);
		}
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "LowBlurredSunTexture"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "HighBlurredSunTexture"), 1);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "DirtTexture"), 2);
		gl.glUniform1f(gl.glGetUniformLocation(shaderProgram, "Dispersal"), 0.1875f);
		gl.glUniform1f(gl.glGetUniformLocation(shaderProgram, "HaloWidth"), 0.45f);
		gl.glUniform1f(gl.glGetUniformLocation(shaderProgram, "Intensity"), 2.25f);
		gl.glUniform3f(gl.glGetUniformLocation(shaderProgram, "Distortion"), 0.94f, 0.97f, 1.00f);
		sunPosProj = gl.glGetUniformLocation(shaderProgram, "SunPosProj");
	}

	@Override
	protected InputStream getVertexSource()
	{
		return getClass().getResourceAsStream("SunVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return getClass().getResourceAsStream("SunRaysLensFlareHaloFragmentShader.glsl");
	}
}
