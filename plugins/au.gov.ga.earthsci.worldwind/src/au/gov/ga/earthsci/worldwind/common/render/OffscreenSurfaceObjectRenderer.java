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
package au.gov.ga.earthsci.worldwind.common.render;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceObjectTileBuilder;
import gov.nasa.worldwind.util.OGLRenderToTextureSupport;

import javax.media.opengl.GL2;

/**
 * This {@link SurfaceObjectTileBuilder} subclass uses the
 * {@link FrameBufferStack} when binding the framebuffer used to render surface
 * objects.
 * <p>
 * The default {@link SurfaceObjectTileBuilder} uses an fbo, but doesn't use our
 * {@link FrameBufferStack}. This means that the {@link FrameBuffer} used when
 * rendering the animation is lost, causing the render to fail.
 * </p>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OffscreenSurfaceObjectRenderer extends SurfaceObjectTileBuilder
{
	public OffscreenSurfaceObjectRenderer()
	{
		super();
		this.rttSupport = new OffscreenOGLRenderToTextureSupport();
	}

	public class OffscreenOGLRenderToTextureSupport extends OGLRenderToTextureSupport
	{
		@Override
		protected void beginFramebufferObjectRendering(DrawContext dc)
		{
			int[] framebuffers = new int[1];

			GL2 gl = dc.getGL().getGL2();
			gl.glGenFramebuffers(1, framebuffers, 0);
			FrameBufferStack.push(gl, framebuffers[0]);

			this.framebufferObject = framebuffers[0];
			if (this.framebufferObject == 0)
			{
				throw new IllegalStateException("Frame Buffer Object not created.");
			}
		}

		@Override
		protected void endFramebufferObjectRendering(DrawContext dc)
		{
			int[] framebuffers = new int[] { this.framebufferObject };

			GL2 gl = dc.getGL().getGL2();
			FrameBufferStack.pop(gl);
			gl.glDeleteFramebuffers(1, framebuffers, 0);

			this.framebufferObject = 0;
		}
	}
}
