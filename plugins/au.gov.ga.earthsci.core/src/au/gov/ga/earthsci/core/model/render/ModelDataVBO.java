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
package au.gov.ga.earthsci.core.model.render;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import au.gov.ga.earthsci.model.data.IModelData;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.AbstractVBO;

/**
 * A simple representation of an OpenGL vertex buffer object that provides
 * helper methods for binding a Java {@link ByteBuffer}
 * 
 * @see http://www.opengl.org/wiki/Vertex_Specification#Vertex_Buffer_Object
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ModelDataVBO extends AbstractVBO<ByteBuffer>
{

	private final IModelData data;
	private final int glTarget;

	/**
	 * Create a new VBO with target {@link GL#GL_ARRAY_BUFFER}
	 */
	public static ModelDataVBO createDataVBO(IModelData data)
	{
		return new ModelDataVBO(GL.GL_ARRAY_BUFFER, data);
	}

	/**
	 * Create a new VBO with target {@link GL#GL_ELEMENT_ARRAY_BUFFER}
	 */
	public static ModelDataVBO createIndexVBO(IModelData data)
	{
		return new ModelDataVBO(GL.GL_ELEMENT_ARRAY_BUFFER, data);
	}

	/**
	 * Create a new vertex buffer object for the given model data
	 * 
	 * @param glTarget
	 *            The GL target to bind the buffer to
	 * @param buffer
	 *            The buffer to bind to this VBO
	 * @param dataSize
	 *            The size (in bytes) of the data contained in the buffer
	 */
	public ModelDataVBO(int glTarget, IModelData modelData)
	{
		super();
		this.glTarget = glTarget;
		this.data = modelData;

		setBuffer(data.getSource());
	}

	@Override
	protected int getTarget()
	{
		return glTarget;
	}

	@Override
	protected Buffer wrapBuffer(ByteBuffer buffer)
	{
		return buffer;
	}

	@Override
	protected int getDataSize()
	{
		return data.getBufferType().getNumberOfBytes();
	}

}
