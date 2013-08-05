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
 * Math utility methods
 */
public class MathUtil
{

	private MathUtil()
	{
	}

	/**
	 * Clamp the provided value to the range specified by
	 * <code>[min, max]</code>
	 */
	public static int clamp(int value, int min, int max)
	{
		if (min > max)
		{
			return clamp(value, max, min);
		}
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Clamp the provided value to the range specified by
	 * <code>[min, max]</code>
	 */
	public static double clamp(double value, double min, double max)
	{
		if (min > max)
		{
			return clamp(value, max, min);
		}
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Clamp the provided value to the range specified by
	 * <code>[min, max]</code>
	 */
	public static float clamp(float value, float min, float max)
	{
		if (min > max)
		{
			return clamp(value, max, min);
		}
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Is x a power of 2?
	 * 
	 * @param x
	 * @return True if x is a power of 2
	 */
	public static boolean isPowerOfTwo(long x)
	{
		return x != 0 && (x & (x - 1)) == 0;
	}

	/**
	 * Calculate the previous power of 2. If x is a power of 2, x is returned,
	 * otherwise the greatest power of two that is less than x is returned.
	 * 
	 * @param x
	 * @return Greatest power of two less than or equal to x
	 */
	public static int previousPowerOfTwo(int x)
	{
		x = x | (x >> 1);
		x = x | (x >> 2);
		x = x | (x >> 4);
		x = x | (x >> 8);
		x = x | (x >> 16);
		return x - (x >> 1);
	}

	/**
	 * Calculate the next power of 2. If x is a power of 2, x is returned,
	 * otherwise the smallest power of two that is greater than x is returned.
	 * 
	 * @param x
	 * @return Smallest power of two greater than or equal to x
	 */
	public static int nextPowerOfTwo(int x)
	{
		x--;
		x |= x >> 1;
		x |= x >> 2;
		x |= x >> 4;
		x |= x >> 8;
		x |= x >> 16;
		x++;
		return x;
	}
}
