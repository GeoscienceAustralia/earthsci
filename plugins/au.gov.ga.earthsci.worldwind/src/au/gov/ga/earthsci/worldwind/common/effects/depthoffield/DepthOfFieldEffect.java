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

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.effects.Effect;
import au.gov.ga.earthsci.worldwind.common.effects.EffectBase;
import au.gov.ga.earthsci.worldwind.common.render.FrameBuffer;

/**
 * {@link Effect} implementation that provides a depth-of-field effect, with
 * animatable near, far, and focus length parameters. The parameter values
 * default to the near clipping plane, far clipping plane, and animation camera
 * look-at point respectively.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DepthOfFieldEffect extends EffectBase
{
	private final DepthOfFieldShader depthOfFieldShader = new DepthOfFieldShader();
	private final GaussianBlurShader gaussianBlurShader = new GaussianBlurShader();
	private final FrameBuffer blurFrameBuffer = new FrameBuffer();

	private double focus = 5;
	private double near = 0;
	private double far = 10;

	@Override
	protected void resizeExtraFrameBuffers(DrawContext dc, Dimension dimensions)
	{
		blurFrameBuffer.resize(dc.getGL().getGL2(), new Dimension(dimensions.width / 4, dimensions.height / 4)); //1/16 of the size
	}

	/**
	 * @return The near blur limit (everything closer than this distance is
	 *         fully blurred)
	 */
	public double getNear()
	{
		return near;
	}

	/**
	 * Set the near blur limit
	 * 
	 * @param near
	 */
	public void setNear(double near)
	{
		this.near = near;
	}

	/**
	 * @return The far blur limit (everything beyond this is fully blurred)
	 */
	public double getFar()
	{
		return far;
	}

	/**
	 * Set the far blur limit
	 * 
	 * @param far
	 */
	public void setFar(double far)
	{
		this.far = far;
	}

	/**
	 * @return The focus distance (everything at this depth is in focus)
	 */
	public double getFocus()
	{
		return focus;
	}

	/**
	 * Set the focus distance
	 * 
	 * @param focus
	 */
	public void setFocus(double focus)
	{
		this.focus = focus;
	}

	@Override
	protected void drawFrameBufferWithEffect(DrawContext dc, Dimension dimensions, FrameBuffer frameBuffer)
	{
		GL2 gl = dc.getGL().getGL2();

		if (!(depthOfFieldShader.createIfRequired(gl) && gaussianBlurShader.createIfRequired(gl)))
		{
			FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTexture().getId(), frameBuffer.getDepth().getId());
			return;
		}

		try
		{
			//disable depth testing for the blur frame buffer, and change the viewport to match the frame buffer's dimensions
			gl.glPushAttrib(GL2.GL_VIEWPORT_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			gl.glDepthMask(false);
			gl.glViewport(0, 0, blurFrameBuffer.getDimensions().width, blurFrameBuffer.getDimensions().height);

			//bind the blur frame buffer
			try
			{
				//bind and clear the blur frame buffer
				blurFrameBuffer.bind(gl);
				gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

				//draw the scene, blurring vertically first, then horizontally
				gaussianBlurShader.use(gl, blurFrameBuffer.getDimensions(), false);
				FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTexture().getId());
				gaussianBlurShader.use(gl, blurFrameBuffer.getDimensions(), true);
				FrameBuffer.renderTexturedQuad(gl, blurFrameBuffer.getTexture().getId());
				gaussianBlurShader.unuse(gl);
			}
			finally
			{
				blurFrameBuffer.unbind(gl);
			}
		}
		finally
		{
			gl.glPopAttrib();
		}

		try
		{
			depthOfFieldShader.use(gl, frameBuffer.getDimensions(), (float) focus, (float) near, (float) far, 1f / 4f);
			FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTextures()[0].getId(), frameBuffer.getDepth().getId(),
					blurFrameBuffer.getTextures()[0].getId());
		}
		finally
		{
			depthOfFieldShader.unuse(gl);
		}
	}

	@Override
	protected void releaseEffect(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		depthOfFieldShader.deleteIfCreated(gl);
		gaussianBlurShader.deleteIfCreated(gl);
		blurFrameBuffer.deleteIfCreated(gl);
	}
}
