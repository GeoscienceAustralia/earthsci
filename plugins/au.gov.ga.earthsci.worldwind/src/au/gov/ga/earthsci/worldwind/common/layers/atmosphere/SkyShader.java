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

import java.io.InputStream;

import javax.media.opengl.GL2;

/**
 * Shader for performing atmospheric scattering effects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public class SkyShader extends AbstractAtmosphereShader
{
	private int g = -1;
	private int g2 = -1;

	public SkyShader(String[] defines)
	{
		super(defines);
	}

	public void use(GL2 gl, Vec4 cameraPos, Vec4 lightDir, float[] invWavelength4, float cameraHeight,
			float innerRadius, float outerRadius, float rayleighScattering, float mieScattering, float sunBrightness,
			float scaleDepth, float miePhaseAsymmetry, float exposure)
	{
		super.use(gl, cameraPos, lightDir, invWavelength4, cameraHeight, innerRadius, outerRadius, rayleighScattering,
				mieScattering, sunBrightness, scaleDepth, exposure);
		gl.glUniform1f(this.g, miePhaseAsymmetry);
		gl.glUniform1f(this.g2, miePhaseAsymmetry * miePhaseAsymmetry);
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		super.getUniformLocations(gl);
		this.g = gl.glGetUniformLocation(shaderProgram, "g");
		this.g2 = gl.glGetUniformLocation(shaderProgram, "g2");
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "texture"), 0);
	}

	@Override
	protected InputStream getVertexSource()
	{
		return this.getClass().getResourceAsStream("SkyVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return this.getClass().getResourceAsStream("SkyFragmentShader.glsl");
	}
}
