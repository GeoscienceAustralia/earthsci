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
package au.gov.ga.earthsci.worldwind.common.render.fastshape;

import java.nio.Buffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;

/**
 * Abstract class representing an OpenGL VBO (Vertex Buffer Object). Contains
 * methods for binding the buffer and updating the buffer data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractVBO<ARRAY>
{
	private ARRAY buffer = null;
	private int vboId = -1;
	private boolean dirty = false;
	private Lock lock = new ReentrantLock();
	private boolean uploadRequired = true;

	/**
	 * @return This VBO's data array
	 */
	public ARRAY getBuffer()
	{
		return buffer;
	}

	/**
	 * Set this VBO's data array. The array will be passed to the video card
	 * upon the next call to {@link #bind(GL)}.
	 * 
	 * @param buffer
	 */
	public void setBuffer(ARRAY buffer)
	{
		if (!uploadRequired)
		{
			uploadRequired = this.buffer != buffer;
		}
		this.buffer = buffer;
		markDirty();
	}

	/**
	 * Mark this VBO's data array as dirty. The array will be passed to the
	 * video card upon the next call to {@link #bind(GL)}. Also called
	 * internally by {@link #setBuffer(ARRAY)}.
	 */
	public void markDirty()
	{
		dirty = true;
	}

	/**
	 * Mark this VBO as locked for writing. While this is locked, the
	 * {@link #bind(GL)} method will not upload the buffer to the video card.
	 */
	public void lock()
	{
		lock.lock();
	}

	/**
	 * Mark this VBO as unlocked. The {@link #bind(GL)} method will now be able
	 * to upload the buffer if it's dirty.
	 */
	public void unlock()
	{
		lock.unlock();
	}

	/**
	 * Bind this VBO's buffer. Also uploads the buffer to the video card if it
	 * is dirty.
	 * 
	 * @param gl
	 *            OpenGL context
	 */
	public void bind(GL2 gl)
	{
		if (vboId < 0)
		{
			int[] vboIds = new int[1];
			gl.glGenBuffers(vboIds.length, vboIds, 0);
			vboId = vboIds[0];
		}
		gl.glBindBuffer(getTarget(), vboId);
		if (dirty)
		{
			boolean locked = false;
			if (uploadRequired)
			{
				lock.lock();
				uploadRequired = false;
				locked = true;
			}
			else
			{
				locked = lock.tryLock();
			}
			if (locked)
			{
				try
				{
					Buffer b = wrapBuffer(buffer);
					gl.glBufferData(getTarget(), b.limit() * getDataSize(), b.rewind(), GL2.GL_STATIC_DRAW);
					dirty = false;
				}
				finally
				{
					lock.unlock();
				}
			}
		}
	}

	/**
	 * Unbind this VBO.
	 * 
	 * @param gl
	 *            OpenGL context
	 */
	public void unbind(GL2 gl)
	{
		gl.glBindBuffer(getTarget(), 0);
	}

	/**
	 * @return OpenGL target to which to bind/unbind this VBO. Either
	 *         {@link GL#GL_ARRAY_BUFFER} or {@link GL#GL_ELEMENT_ARRAY_BUFFER}.
	 */
	protected abstract int getTarget();

	/**
	 * Wrap this VBO's array in a java.nio {@link Buffer} class. Used when
	 * uploading the array to the video card.
	 * 
	 * @param buffer
	 *            Array to wrap
	 * @return Wrapped buffer
	 */
	protected abstract Buffer wrapBuffer(ARRAY buffer);

	/**
	 * @return Data size of each buffer element, in bytes.
	 */
	protected abstract int getDataSize();
}