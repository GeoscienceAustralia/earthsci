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

import java.awt.Dimension;

import javax.media.opengl.GL2;

/**
 * Class used by the {@link FrameBuffer} to generate/store the depth buffer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FrameBufferDepthBuffer
{
	private int id = 0;

	private boolean texture = false;
	private int textureTarget = GL2.GL_TEXTURE_2D;
	private int textureMode = GL2.GL_INTENSITY; //or GL_LUMINANCE
	private int textureType = GL2.GL_UNSIGNED_BYTE; //or GL_FLOAT for float
	private int internalFormat = GL2.GL_DEPTH_COMPONENT24; //or GL_DEPTH_COMPONENT32F_NV for float

	protected void create(GL2 gl, Dimension dimensions)
	{
		delete(gl);

		int[] renderBuffers = new int[1];
		if (texture)
		{
			gl.glGenTextures(1, renderBuffers, 0);
		}
		else
		{
			gl.glGenRenderbuffers(1, renderBuffers, 0);
		}
		if (renderBuffers[0] <= 0)
		{
			throw new IllegalStateException("Error generating depth buffer for frame buffer");
		}
		id = renderBuffers[0];

		if (texture)
		{
			gl.glBindTexture(textureTarget, id);

			gl.glTexParameteri(textureTarget, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
			gl.glTexParameteri(textureTarget, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glTexParameteri(textureTarget, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(textureTarget, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(textureTarget, GL2.GL_DEPTH_TEXTURE_MODE, textureMode);
			gl.glTexParameteri(textureTarget, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_NONE);

			gl.glTexImage2D(textureTarget, 0, internalFormat, dimensions.width, dimensions.height, 0,
					GL2.GL_DEPTH_COMPONENT, textureType, null);
			gl.glBindTexture(textureTarget, 0);
		}
		else
		{
			gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, id);
			gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, internalFormat, dimensions.width, dimensions.height);
			gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
		}
	}

	protected void delete(GL2 gl)
	{
		if (isCreated())
		{
			if (texture)
			{
				gl.glDeleteTextures(1, new int[] { id }, 0);
			}
			else
			{
				gl.glDeleteRenderbuffers(1, new int[] { id }, 0);
			}
			id = 0;
		}
	}

	public boolean isCreated()
	{
		return id > 0;
	}

	public int getId()
	{
		return id;
	}

	public boolean isTexture()
	{
		return texture;
	}

	public void setTexture(boolean texture)
	{
		this.texture = texture;
	}

	public int getTextureTarget()
	{
		return textureTarget;
	}

	public void setTextureTarget(int textureTarget)
	{
		this.textureTarget = textureTarget;
	}

	public int getTextureMode()
	{
		return textureMode;
	}

	public void setTextureMode(int textureMode)
	{
		this.textureMode = textureMode;
	}

	public int getTextureType()
	{
		return textureType;
	}

	public void setTextureType(int textureType)
	{
		this.textureType = textureType;
	}

	public int getInternalFormat()
	{
		return internalFormat;
	}

	public void setInternalFormat(int internalFormat)
	{
		this.internalFormat = internalFormat;
	}
}
