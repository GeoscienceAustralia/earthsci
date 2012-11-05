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
import java.nio.FloatBuffer;

import javax.media.opengl.GL2;

/**
 * {@link AbstractVBO} implementation for float VBOs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FloatVBO extends AbstractStrideVBO<float[]>
{
	public FloatVBO(int elementStride)
	{
		super(elementStride);
	}

	@Override
	protected int getTarget()
	{
		return GL2.GL_ARRAY_BUFFER;
	}

	@Override
	protected Buffer wrapBuffer(float[] buffer)
	{
		return FloatBuffer.wrap(buffer);
	}

	@Override
	protected int getDataSize()
	{
		return Float.SIZE / 8;
	}
}