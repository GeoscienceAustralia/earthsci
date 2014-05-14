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

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.Shader;

/**
 * Base shader for atmospheric scattering effects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public abstract class AbstractAtmosphereShader extends Shader
{
	private final String[] defines;
	private int v3CameraPos = -1;
	private int v3LightPos = -1;
	private int v3InvWavelength = -1;
	private int fCameraHeight = -1;
	private int fCameraHeight2 = -1;
	private int fInnerRadius = -1;
	private int fInnerRadius2 = -1;
	private int fOuterRadius = -1;
	private int fOuterRadius2 = -1;
	private int fKrESun = -1;
	private int fKmESun = -1;
	private int fKr4PI = -1;
	private int fKm4PI = -1;
	private int fScale = -1;
	private int fScaleDepth = -1;
	private int fScaleOverScaleDepth = -1;
	private int fExposure = -1;

	protected AbstractAtmosphereShader(String[] defines)
	{
		this.defines = defines;
	}

	@Override
	protected String[] getDefines()
	{
		return defines;
	}

	public void use(GL2 gl, Vec4 cameraPos, Vec4 lightDir, float[] invWavelength4, float cameraHeight,
			float innerRadius, float outerRadius, float rayleighScattering, float mieScattering, float sunBrightness,
			float scaleDepth, float exposure)
	{
		super.use(gl);
		if (cameraHeight < innerRadius)
		{
			cameraHeight = innerRadius;
			cameraPos = cameraPos.normalize3().multiply3(innerRadius);
		}
		float fScale = 1f / (outerRadius - innerRadius);
		gl.glUniform3f(this.v3CameraPos, (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
		gl.glUniform3f(this.v3LightPos, (float) lightDir.x, (float) lightDir.y, (float) lightDir.z);
		gl.glUniform3f(this.v3InvWavelength, invWavelength4[0], invWavelength4[1], invWavelength4[2]);
		gl.glUniform1f(this.fCameraHeight, cameraHeight);
		gl.glUniform1f(this.fCameraHeight2, cameraHeight * cameraHeight);
		gl.glUniform1f(this.fInnerRadius, innerRadius);
		gl.glUniform1f(this.fInnerRadius2, innerRadius * innerRadius);
		gl.glUniform1f(this.fOuterRadius, outerRadius);
		gl.glUniform1f(this.fOuterRadius2, outerRadius * outerRadius);
		gl.glUniform1f(this.fKrESun, rayleighScattering * sunBrightness);
		gl.glUniform1f(this.fKmESun, mieScattering * sunBrightness);
		gl.glUniform1f(this.fKr4PI, rayleighScattering * 4f * (float) Math.PI);
		gl.glUniform1f(this.fKm4PI, mieScattering * 4f * (float) Math.PI);
		gl.glUniform1f(this.fScale, fScale);
		gl.glUniform1f(this.fScaleDepth, scaleDepth);
		gl.glUniform1f(this.fScaleOverScaleDepth, fScale / scaleDepth);
		gl.glUniform1f(this.fExposure, exposure);
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		this.v3CameraPos = gl.glGetUniformLocation(shaderProgram, "v3CameraPos");
		this.v3LightPos = gl.glGetUniformLocation(shaderProgram, "v3LightPos");
		this.v3InvWavelength = gl.glGetUniformLocation(shaderProgram, "v3InvWavelength");
		this.fCameraHeight = gl.glGetUniformLocation(shaderProgram, "fCameraHeight");
		this.fCameraHeight2 = gl.glGetUniformLocation(shaderProgram, "fCameraHeight2");
		this.fInnerRadius = gl.glGetUniformLocation(shaderProgram, "fInnerRadius");
		this.fInnerRadius2 = gl.glGetUniformLocation(shaderProgram, "fInnerRadius2");
		this.fOuterRadius = gl.glGetUniformLocation(shaderProgram, "fOuterRadius");
		this.fOuterRadius2 = gl.glGetUniformLocation(shaderProgram, "fOuterRadius2");
		this.fKrESun = gl.glGetUniformLocation(shaderProgram, "fKrESun");
		this.fKmESun = gl.glGetUniformLocation(shaderProgram, "fKmESun");
		this.fKr4PI = gl.glGetUniformLocation(shaderProgram, "fKr4PI");
		this.fKm4PI = gl.glGetUniformLocation(shaderProgram, "fKm4PI");
		this.fScale = gl.glGetUniformLocation(shaderProgram, "fScale");
		this.fScaleDepth = gl.glGetUniformLocation(shaderProgram, "fScaleDepth");
		this.fScaleOverScaleDepth = gl.glGetUniformLocation(shaderProgram, "fScaleOverScaleDepth");
		this.fExposure = gl.glGetUniformLocation(shaderProgram, "fExposure");
	}
}
