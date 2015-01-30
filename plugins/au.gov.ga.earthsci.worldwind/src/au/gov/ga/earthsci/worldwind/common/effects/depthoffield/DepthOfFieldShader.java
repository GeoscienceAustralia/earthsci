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

import java.awt.Dimension;
import java.io.InputStream;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.Shader;

/**
 * A {@link Shader} that takes a scene, depth and blurred scene texture, and
 * interpolates between the scene and blurred texture according to the depth,
 * creating a depth-of-field effect.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DepthOfFieldShader extends Shader
{
	private int cameraNearUniform = -1;
	private int cameraFarUniform = -1;
	private int focalLengthUniform = -1;
	private int pixelSizeUniform = -1;
	private int blurTextureScaleUniform = -1;

	/**
	 * Use this shader.
	 * 
	 * @param gl
	 *            GL context
	 * @param dimensions
	 *            Dimensions of the textures
	 * @param focus
	 *            Focus distance
	 * @param near
	 *            Near limit
	 * @param far
	 *            Far limit
	 * @param blurTextureScale
	 *            Scale of the blurred texture compared to the scene texture
	 */
	public void use(GL2 gl, Dimension dimensions, float focus, float near, float far, float blurTextureScale)
	{
		if (super.use(gl))
		{
			gl.glUniform1f(cameraNearUniform, near);
			gl.glUniform1f(cameraFarUniform, far);
			gl.glUniform1f(focalLengthUniform, focus);
			gl.glUniform2f(pixelSizeUniform, 1f / dimensions.width, 1f / dimensions.height);
			gl.glUniform1f(blurTextureScaleUniform, blurTextureScale);
		}
	}

	@Override
	protected InputStream getVertexSource()
	{
		return this.getClass().getResourceAsStream("GenericVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return this.getClass().getResourceAsStream("DepthOfFieldFragmentShader.glsl");
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "colorTexture"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "depthTexture"), 1);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "blurTexture"), 2);
		cameraNearUniform = gl.glGetUniformLocation(shaderProgram, "cameraNear");
		cameraFarUniform = gl.glGetUniformLocation(shaderProgram, "cameraFar");
		focalLengthUniform = gl.glGetUniformLocation(shaderProgram, "focalLength");
		pixelSizeUniform = gl.glGetUniformLocation(shaderProgram, "pixelSize");
		blurTextureScaleUniform = gl.glGetUniformLocation(shaderProgram, "blurTextureScale");
	}
}
