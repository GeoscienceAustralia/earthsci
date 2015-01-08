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
package au.gov.ga.earthsci.worldwind.common.view.oculus;

import gov.nasa.worldwind.render.DrawContext;

import java.io.InputStream;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.Shader;

/**
 * Shader for the Oculus Rift lens distortion.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DistortionShader extends Shader
{
	private int eyeToSourceUVscaleUniform = -1;
	private int eyeToSourceUVoffsetUniform = -1;
	private int eyeRotationStartUniform = -1;
	private int eyeRotationEndUniform = -1;

	@Override
	protected InputStream getVertexSource()
	{
		return getClass().getResourceAsStream("Distortion.vert");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return getClass().getResourceAsStream("Distortion.frag");
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		eyeToSourceUVscaleUniform = gl.glGetUniformLocation(shaderProgram, "eyeToSourceUVscale");
		eyeToSourceUVoffsetUniform = gl.glGetUniformLocation(shaderProgram, "eyeToSourceUVoffset");
		eyeRotationStartUniform = gl.glGetUniformLocation(shaderProgram, "eyeRotationStart");
		eyeRotationEndUniform = gl.glGetUniformLocation(shaderProgram, "eyeRotationEnd");
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "texture0"), 0);
	}

	public void use(DrawContext dc, float eyeToSourceUscale, float eyeToSourceVscale, float eyeToSourceUoffset,
			float eyeToSourceVoffset, float[] eyeRotationStart, float[] eyeRotationEnd)
	{
		GL2 gl = dc.getGL().getGL2();
		super.use(gl);

		gl.glUniform2f(eyeToSourceUVscaleUniform, eyeToSourceUscale, eyeToSourceVscale);
		gl.glUniform2f(eyeToSourceUVoffsetUniform, eyeToSourceUoffset, eyeToSourceVoffset);
		gl.glUniformMatrix4fv(eyeRotationStartUniform, 1, true, eyeRotationStart, 0);
		gl.glUniformMatrix4fv(eyeRotationEndUniform, 1, true, eyeRotationEnd, 0);
	}
}
