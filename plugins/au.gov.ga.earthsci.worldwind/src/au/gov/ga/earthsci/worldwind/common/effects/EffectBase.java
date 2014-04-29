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
package au.gov.ga.earthsci.worldwind.common.effects;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.FrameBuffer;

/**
 * Abstract base implementation of the {@link Effect} interface. Most
 * {@link Effect} implementations should use this as their base class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class EffectBase implements Effect
{
	/**
	 * The frame buffer to draw to for this effect.
	 */
	protected final FrameBuffer frameBuffer = new FrameBuffer(1, true);

	/**
	 * Enabled flag
	 */
	protected boolean enabled = true;

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public final void bindFrameBuffer(DrawContext dc, Dimension dimensions)
	{
		GL2 gl = dc.getGL().getGL2();

		//this will create the framebuffer if it doesn't exist
		frameBuffer.resize(gl, dimensions);
		resizeExtraFrameBuffers(dc, dimensions);
		frameBuffer.bind(gl);
	}

	/**
	 * If this effect requires any extra frame buffers, resize them here.
	 * 
	 * @param dc
	 *            Draw context
	 * @param dimensions
	 *            Render dimensions
	 */
	protected void resizeExtraFrameBuffers(DrawContext dc, Dimension dimensions)
	{
	}

	@Override
	public final void unbindFrameBuffer(DrawContext dc, Dimension dimensions)
	{
		frameBuffer.unbind(dc.getGL().getGL2());
	}

	@Override
	public final void drawFrameBufferWithEffect(DrawContext dc, Dimension dimensions)
	{
		drawFrameBufferWithEffect(dc, dimensions, frameBuffer);
	}

	@Override
	public final void releaseResources(DrawContext dc)
	{
		frameBuffer.deleteIfCreated(dc.getGL().getGL2());
		releaseEffect(dc);
	}

	/**
	 * Called after the scene is rendered, and possibly after another
	 * {@link Effect}'s frame buffer has been bound. The effect should render
	 * it's framebuffer using it's effect shader here.
	 * 
	 * @param dc
	 *            Draw context
	 * @param dimensions
	 *            Dimensions of the viewport (includes render scale during
	 *            rendering)
	 * @param frameBuffer
	 *            {@link FrameBuffer} containing the scene to apply the effect
	 *            to.
	 */
	protected abstract void drawFrameBufferWithEffect(DrawContext dc, Dimension dimensions, FrameBuffer frameBuffer);

	/**
	 * Release any resources associated with this effect. This is called every
	 * frame if the effect is disabled.
	 * 
	 * @param dc
	 *            Draw context
	 */
	protected abstract void releaseEffect(DrawContext dc);
}
