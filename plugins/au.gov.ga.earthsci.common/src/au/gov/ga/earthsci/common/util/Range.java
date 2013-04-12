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
package au.gov.ga.earthsci.common.util;

/**
 * Represents an immutable range of comparable values (eg.
 * <code>[minValue, maxValue]</code>)
 * <p/>
 * The range may have open ends.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Range<C extends Comparable<C>>
{

	// Delegate to the plugin version - this is done to reduce coupling between core and ww plugins

	private au.gov.ga.earthsci.worldwind.common.util.Range<C> delegate;

	public Range(C minValue, boolean includeMin, C maxValue, boolean includeMax)
	{
		delegate = new au.gov.ga.earthsci.worldwind.common.util.Range<C>(minValue, includeMin, maxValue, includeMax);
	}

	public Range(C minValue, C maxValue)
	{
		delegate = new au.gov.ga.earthsci.worldwind.common.util.Range<C>(minValue, maxValue);
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#isOpenLeft()
	 */
	public boolean isOpenLeft()
	{
		return delegate.isOpenLeft();
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#isInclusiveLeft()
	 */
	public boolean isInclusiveLeft()
	{
		return delegate.isInclusiveLeft();
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#isOpenRight()
	 */
	public boolean isOpenRight()
	{
		return delegate.isOpenRight();
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#isInclusiveRight()
	 */
	public boolean isInclusiveRight()
	{
		return delegate.isInclusiveRight();
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#getMinValue()
	 */
	public C getMinValue()
	{
		return delegate.getMinValue();
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#getMaxValue()
	 */
	public C getMaxValue()
	{
		return delegate.getMaxValue();
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#contains(java.lang.Comparable)
	 */
	public boolean contains(C value)
	{
		return delegate.contains(value);
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return delegate.equals(obj);
	}

	/**
	 * @see au.gov.ga.earthsci.worldwind.common.util.Range#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return delegate.hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return delegate.toString();
	}



}
