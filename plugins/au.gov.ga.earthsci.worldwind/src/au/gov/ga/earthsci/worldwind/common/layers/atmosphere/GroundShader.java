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

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;

import java.io.InputStream;

import javax.media.opengl.GL2;

/**
 * Shader that calculates atmospheric scattering effects on the ground.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public class GroundShader extends AbstractAtmosphereShader
{
	private int oldModelViewInverse = -1;

	public GroundShader(String[] defines)
	{
		super(defines);
	}

	public void use(GL2 gl, Vec4 cameraPos, Vec4 lightDir, float[] invWavelength4, float cameraHeight,
			float innerRadius, float outerRadius, float rayleighScattering, float mieScattering, float sunBrightness,
			float scaleDepth, float exposure, Matrix mvInv)
	{
		super.use(gl, cameraPos, lightDir, invWavelength4, cameraHeight, innerRadius, outerRadius, rayleighScattering,
				mieScattering, sunBrightness, scaleDepth, exposure);
		float[] modelViewInvArray = new float[] {
				(float) mvInv.m11, (float) mvInv.m21, (float) mvInv.m31, (float) mvInv.m41,
				(float) mvInv.m12, (float) mvInv.m22, (float) mvInv.m32, (float) mvInv.m42,
				(float) mvInv.m13, (float) mvInv.m23, (float) mvInv.m33, (float) mvInv.m43,
				(float) mvInv.m14, (float) mvInv.m24, (float) mvInv.m34, (float) mvInv.m44 };
		gl.glUniformMatrix4fv(oldModelViewInverse, 1, false, modelViewInvArray, 0);
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		super.getUniformLocations(gl);
		oldModelViewInverse = gl.glGetUniformLocation(shaderProgram, "oldModelViewInverse");
	}

	@Override
	protected InputStream getVertexSource()
	{
		return this.getClass().getResourceAsStream("GroundVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return this.getClass().getResourceAsStream("GroundFragmentShader.glsl");
	}
}
