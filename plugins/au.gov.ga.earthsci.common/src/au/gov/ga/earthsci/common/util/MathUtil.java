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

}
