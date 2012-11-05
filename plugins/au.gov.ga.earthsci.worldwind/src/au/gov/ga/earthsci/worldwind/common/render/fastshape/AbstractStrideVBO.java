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

/**
 * {@link AbstractVBO} subclass for strided data (eg vertex data with 3 elements
 * x,y,z, or color data with 4 elements r,g,b,a).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractStrideVBO<ARRAY> extends AbstractVBO<ARRAY>
{
	private int elementStride;

	public AbstractStrideVBO(int elementStride)
	{
		this.elementStride = elementStride;
	}

	/**
	 * @return Number of values per element in this VBO.
	 */
	public int getElementStride()
	{
		return elementStride;
	}

	/**
	 * Set the number of values per element in this VBO.
	 * 
	 * @param elementStride
	 */
	public void setElementStride(int elementStride)
	{
		this.elementStride = elementStride;
	}
}
