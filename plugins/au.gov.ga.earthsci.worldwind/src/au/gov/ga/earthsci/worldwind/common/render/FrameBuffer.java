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

import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Dimension;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Helper class for the creation and binding of an OpenGL Frame Buffer Object.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class FrameBuffer
{
	private final FrameBufferTexture[] textures;
	private final FrameBufferDepthBuffer depth = new FrameBufferDepthBuffer();
	private int frameBufferId = 0;
	private Dimension currentDimensions = null;

	/**
	 * Create a new frame buffer.
	 */
	public FrameBuffer()
	{
		this(1);
	}

	/**
	 * Create a new frame buffer.
	 * 
	 * @param textureCount
	 *            Number of textures to bind to this frame buffer
	 */
	public FrameBuffer(int textureCount)
	{
		this(textureCount, false);
	}

	/**
	 * Create a new frame buffer.
	 * 
	 * @param textureCount
	 *            Number of textures to bind to this frame buffer
	 * @param depthAsTexture
	 *            Use a texture for the depth buffer
	 */
	public FrameBuffer(int textureCount, boolean depthAsTexture)
	{
		if (textureCount < 1)
		{
			throw new IllegalArgumentException("Must be at least one texture bound to the frame buffer");
		}
		textures = new FrameBufferTexture[textureCount];
		for (int i = 0; i < textureCount; i++)
		{
			textures[i] = new FrameBufferTexture();
		}
		depth.setTexture(depthAsTexture);
	}

	/**
	 * Create a frame buffer, and its texture and depth buffer (but don't bind)
	 * 
	 * @param gl
	 *            GL context
	 * @param dimensions
	 *            Frame buffer dimensions
	 */
	public void create(GL2 gl, Dimension dimensions)
	{
		//generate a texture, depth buffer, and frame buffer
		frameBufferId = generateFrameBuffer(gl);
		depth.create(gl, dimensions);
		for (FrameBufferTexture texture : textures)
		{
			texture.create(gl, dimensions);
		}

		//bind the frame buffer
		bind(gl);
		//bind the color and depth attachments to the frame buffer
		int colorAttachment = GL2.GL_COLOR_ATTACHMENT0;
		for (FrameBufferTexture texture : textures)
		{
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, colorAttachment++, texture.getTarget(),
					texture.getId(), 0);
		}
		if (depth.isTexture())
		{
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_TEXTURE_2D,
					depth.getId(), 0);
		}
		else
		{
			gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_RENDERBUFFER,
					depth.getId());
		}

		//check to see if the frame buffer is supported and complete
		checkFrameBuffer(gl);

		//unbind the frame buffer (bound later)
		unbind(gl);

		currentDimensions = dimensions;
	}

	/**
	 * Ensure this frame buffer has the correct dimensions. If the frame buffer
	 * has not yet been created, it is created now. If it has already been
	 * created, and the dimensions are different, it is deleted and recreated
	 * with the new dimensions.
	 * 
	 * @param gl
	 * @param dimensions
	 */
	public void resize(GL2 gl, Dimension dimensions)
	{
		Validate.notNull(dimensions, "Dimensions cannot be null");

		if (isCreated() && dimensions.equals(currentDimensions))
		{
			return; //already the correct dimensions
		}

		delete(gl);
		create(gl, dimensions);
	}

	/**
	 * Bind the frame buffer
	 */
	public void bind(GL2 gl)
	{
		FrameBufferStack.push(gl, frameBufferId);
	}

	/**
	 * Unbind the frame buffer
	 */
	public void unbind(GL2 gl)
	{
		FrameBufferStack.pop(gl);
	}

	/**
	 * Performs necessary cleanup to remove the frame buffer
	 */
	public void delete(GL2 gl)
	{
		if (isCreated())
		{
			gl.glDeleteFramebuffers(1, new int[] { frameBufferId }, 0);
			frameBufferId = 0;
		}
		for (FrameBufferTexture texture : textures)
		{
			texture.delete(gl);
		}
		depth.delete(gl);
		currentDimensions = null;
	}

	/**
	 * Delete if created.
	 * 
	 * @param gl
	 * @see FrameBuffer#delete(GL2)
	 */
	public void deleteIfCreated(GL2 gl)
	{
		if (isCreated())
		{
			delete(gl);
		}
	}

	public boolean isCreated()
	{
		return frameBufferId > 0;
	}

	public FrameBufferTexture getTexture()
	{
		return textures[0];
	}

	public FrameBufferTexture[] getTextures()
	{
		return textures;
	}

	public FrameBufferDepthBuffer getDepth()
	{
		return depth;
	}

	public Dimension getDimensions()
	{
		return currentDimensions;
	}

	/**
	 * @return The ID of the generated frame buffer object
	 */
	protected int generateFrameBuffer(GL2 gl)
	{
		int[] frameBuffers = new int[1];
		gl.glGenFramebuffers(1, frameBuffers, 0);
		if (frameBuffers[0] <= 0)
		{
			throw new IllegalStateException("Error generating frame buffer");
		}
		return frameBuffers[0];
	}

	/**
	 * Check that the frame buffer is complete; if not, throws an exception
	 */
	protected void checkFrameBuffer(GL2 gl)
	{
		int status = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		if (status == GL2.GL_FRAMEBUFFER_UNSUPPORTED)
		{
			throw new IllegalStateException("Frame buffer unsupported, or parameters incorrect");
		}
		else if (status != GL2.GL_FRAMEBUFFER_COMPLETE)
		{
			throw new IllegalStateException("Frame buffer incomplete");
		}
	}

	/**
	 * Draw a texture on a quad, covering the entire viewport
	 */
	public static void renderTexturedQuad(GL2 gl, int... textureIds)
	{
		renderTexturedQuadUsingTarget(gl, GL2.GL_TEXTURE_2D, textureIds);
	}

	/**
	 * Draw a texture on a quad, covering the entire viewport
	 */
	public static void renderTexturedQuadUsingTarget(GL2 gl, int target, int... textureIds)
	{
		OGLStackHandler oglsh = new OGLStackHandler();
		oglsh.pushModelviewIdentity(gl);
		oglsh.pushProjectionIdentity(gl);
		oglsh.pushAttrib(gl, GL2.GL_ENABLE_BIT);
		try
		{
			gl.glEnable(target);
			for (int i = 0; i < textureIds.length; i++)
			{
				gl.glActiveTexture(GL2.GL_TEXTURE0 + i);
				gl.glBindTexture(target, textureIds[i]);
			}

			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glTexCoord2f(0, 0);
				gl.glVertex3i(-1, -1, -1);
				gl.glTexCoord2f(1, 0);
				gl.glVertex3i(1, -1, -1);
				gl.glTexCoord2f(1, 1);
				gl.glVertex3i(1, 1, -1);
				gl.glTexCoord2f(0, 1);
				gl.glVertex3i(-1, 1, -1);
			}
			gl.glEnd();
		}
		finally
		{
			oglsh.pop(gl);
		}
	}
}
