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
package au.gov.ga.earthsci.worldwind.common.util;

/**
 * Represents an immutable range of comparable values (eg. <code>[minValue, maxValue]</code>)
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Range<C extends Comparable<C>>
{
	private C minValue;
	private boolean includeMin = true;
	
	private C maxValue;
	private boolean includeMax = true;
	
	
	/**
	 * Create a inclusive range <code>[minValue, maxValue]</code>
	 */
	public Range(C minValue, C maxValue)
	{
		this.minValue = minValue == null ? null : min(minValue, maxValue);
		this.maxValue = maxValue == null ? null : max(minValue, maxValue);
	}
	
	private C min(C value1, C value2)
	{
		if (value1 == null)
		{
			return value2;
		}
		if (value2 == null)
		{
			return value1;
		}
		if (value1.compareTo(value2) <= 0)
		{
			return value1;
		}
		return value2;
	}
	
	private C max(C value1, C value2)
	{
		if (value1 == null)
		{
			return value2;
		}
		if (value2 == null)
		{
			return value1;
		}
		if (value1.compareTo(value2) > 0)
		{
			return value1;
		}
		return value2;
	}
	
	/**
	 * Create new range, specifying the inclusivity.
	 */
	public Range(C minValue, boolean includeMin,  C maxValue, boolean includeMax)
	{
		this(minValue, maxValue);
		this.includeMin = includeMin;
		this.includeMax = includeMax;
	}

	/**
	 * @return Whether this range is open on the left (lower) side
	 */
	public boolean isOpenLeft()
	{
		return minValue == null;
	}
	
	/**
	 * @return Whether this range is inclusive of the left (minimum) value
	 */
	public boolean isInclusiveLeft()
	{
		return !isOpenLeft() && includeMin;
	}
	
	/**
	 * @return Whether this range is open on the right (higher) side
	 */
	public boolean isOpenRight()
	{
		return maxValue == null;
	}
	
	/**
	 * @return Whether this range is inclusive of the right (maximum) value
	 */
	public boolean isInclusiveRight()
	{
		return !isOpenRight() && includeMax;
	}
	
	public C getMinValue()
	{
		return minValue;
	}
	
	public C getMaxValue()
	{
		return maxValue;
	}
	
	/**
	 * @return Whether the provided value is contained in this range
	 */
	public boolean contains(C value)
	{
		if (value == null)
		{
			return false;
		}
		
		return greaterThanMin(value) && lessThanMax(value);
	}

	private boolean lessThanMax(C value)
	{
		if (isOpenRight())
		{
			return true;
		}
		
		int compareValue = maxValue.compareTo(value);
		
		if (isInclusiveRight())
		{
			return compareValue >= 0;
		}
		return compareValue > 0;
	}

	private boolean greaterThanMin(C value)
	{
		if (isOpenLeft())
		{
			return true;
		}
		
		int compareValue = minValue.compareTo(value);
		
		if (isInclusiveLeft())
		{
			return compareValue <= 0;
		}
		return compareValue < 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		Range<C> other = null;
		try
		{
			other = (Range<C>)obj;
		}
		catch (Exception e)
		{
			return false;
		}
		
		return this.includeMax == other.includeMax && this.includeMin == other.includeMin && nullSafeEquals(minValue, other.minValue) && nullSafeEquals(maxValue, other.maxValue);
	}
	
	private boolean nullSafeEquals(Object o1, Object o2)
	{
		if (o1 == null && o2 == null)
		{
			return true;
		}
		
		if (o1 != null && o2 != null)
		{
			return o1.equals(o2);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return (minValue == null ? minValue.hashCode() : 31) + (maxValue == null ? maxValue.hashCode() : 131);
	}
	
	/**
	 * @return A new range that is the union of this range and the provided other
	 */
	public Range<C> union(Range<C> other)
	{
		C min = this.minValue == null || other.minValue == null ? null : min(this.minValue, other.minValue);
		boolean includeMin = min != null && ((min == this.minValue && this.includeMin) || (min == other.minValue && other.includeMin));
		
		C max = this.maxValue == null || other.maxValue == null ? null : max(this.maxValue, other.maxValue);
		boolean includeMax = max != null && ((max == this.maxValue && this.includeMax) || (max == other.maxValue && other.includeMax));
		
		return new Range<C>(min, includeMin, max, includeMax);
	}
}
