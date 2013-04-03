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
package au.gov.ga.earthsci.core.math.interpolation;

import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.core.math.vector.Vector;

/**
 * A simple linear interpolator that interpolates linearly between a start and
 * end vector
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LinearInterpolator<V extends Vector<V>> implements Interpolator<V>
{

	private V start;
	private V end;

	/**
	 * Constructor. Initialises the start and end vectors.
	 * 
	 * @param start
	 * @param end
	 */
	public LinearInterpolator(V start, V end)
	{
		Validate.notNull(start, "A start vector is required"); //$NON-NLS-1$
		Validate.notNull(end, "An end vector is required"); //$NON-NLS-1$

		this.start = start;
		this.end = end;
	}

	@Override
	public V computeValue(double percent)
	{
		return start.interpolate(end, percent);
	}

	/**
	 * @return the start
	 */
	public V getStart()
	{
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(V start)
	{
		Validate.notNull(start, "A start vector is required"); //$NON-NLS-1$
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public V getEnd()
	{
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(V end)
	{
		Validate.notNull(end, "An end vector is required"); //$NON-NLS-1$
		this.end = end;
	}

}
