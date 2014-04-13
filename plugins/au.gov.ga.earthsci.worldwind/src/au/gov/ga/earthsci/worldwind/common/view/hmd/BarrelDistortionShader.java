/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.view.hmd;

import gov.nasa.worldwind.render.DrawContext;

import java.io.InputStream;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.Shader;

/**
 * Shader that does barrel distortion correction for HMDs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BarrelDistortionShader extends Shader
{
	private int lensCenterUniform = -1;
	private int screenCenterUniform = -1;
	private int scaleUniform = -1;
	private int scaleInUniform = -1;
	private int hmdWarpParamUniform = -1;
	private int chromAbParamUniform = -1;
	private int texmUniform = -1;
	private int texScaleUniform = -1;
	private int texOffsetUniform = -1;
	private float[] texm = new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public void use(DrawContext dc, HMDDistortion distortion, int viewportX, int viewportY, int viewportWidth,
			int viewportHeight, boolean left)
	{
		GL2 gl = dc.getGL().getGL2();
		super.use(gl);

		float w = (float) viewportWidth / (float) distortion.getWindowWidth();
		float h = (float) viewportHeight / (float) distortion.getWindowHeight();
		float x = (float) viewportX / (float) distortion.getWindowWidth();
		float y = (float) viewportY / (float) distortion.getWindowHeight();
		float as = (float) viewportWidth / (float) viewportHeight;

		float xcenteroffset = distortion.getDistortionXCenterOffset();
		if (!left)
			xcenteroffset *= -1;

		// We are using 1/4 of DistortionCenter offset value here, since it is
		// relative to [-1,1] range that gets mapped to [0, 0.5].
		gl.glUniform2f(lensCenterUniform, x + (w + xcenteroffset * 0.5f) * 0.5f, y + h * 0.5f);
		gl.glUniform2f(screenCenterUniform, x + w * 0.5f, y + h * 0.5f);

		// MA: This is more correct but we would need higher-res texture vertically; we should adopt this
		// once we have asymmetric input texture scale.
		float scaleFactor = 1f / distortion.getDistortionScale();

		gl.glUniform2f(scaleUniform, (w / 2f) * scaleFactor, (h / 2f) * scaleFactor * as);
		gl.glUniform2f(scaleInUniform, 2f / w, (2f / h) / as);
		float[] K = distortion.getParameters().getDistortionCoefficients();
		gl.glUniform4f(hmdWarpParamUniform, K[0], K[1], K[2], K[3]);

		float[] C = distortion.getParameters().getChromaticAberrationCorrectionCoefficients();
		gl.glUniform4f(chromAbParamUniform, C[0], C[1], C[2], C[3]);

		texm[0] = w;
		texm[3] = x;
		texm[5] = h;
		texm[7] = y;
		texm[15] = 1;
		gl.glUniformMatrix4fv(texmUniform, 1, false, texm, 0);

		gl.glUniform2f(texScaleUniform, 2f, 1f);
		gl.glUniform2f(texOffsetUniform, left ? 0f : -1f, 0f);
	}

	@Override
	protected InputStream getVertexSource()
	{
		return getClass().getResourceAsStream("barrel.vert");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return getClass().getResourceAsStream("barrel.frag");
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex"), 0);
		lensCenterUniform = gl.glGetUniformLocation(shaderProgram, "LensCenter");
		screenCenterUniform = gl.glGetUniformLocation(shaderProgram, "ScreenCenter");
		scaleUniform = gl.glGetUniformLocation(shaderProgram, "Scale");
		scaleInUniform = gl.glGetUniformLocation(shaderProgram, "ScaleIn");
		hmdWarpParamUniform = gl.glGetUniformLocation(shaderProgram, "HmdWarpParam");
		chromAbParamUniform = gl.glGetUniformLocation(shaderProgram, "ChromAbParam");
		texmUniform = gl.glGetUniformLocation(shaderProgram, "Texm");
		texScaleUniform = gl.glGetUniformLocation(shaderProgram, "TexScale");
		texOffsetUniform = gl.glGetUniformLocation(shaderProgram, "TexOffset");
	}
}
