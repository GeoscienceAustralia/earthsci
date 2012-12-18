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
package au.gov.ga.earthsci.core.util;

import org.eclipse.swt.graphics.Color;

/**
 * Collection of static utility methods for SWT support.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SWTUtil
{
	private static final float RGB_VALUE_MULTIPLIER = 0.8f;

	/**
	 * Create a color with a darker hue than the given color.
	 * 
	 * @param color
	 * @return Darker color
	 */
	public static Color darker(Color color)
	{
		return new Color(null, (int) (color.getRed() * RGB_VALUE_MULTIPLIER),
				(int) (color.getGreen() * RGB_VALUE_MULTIPLIER), (int) (color.getBlue() * RGB_VALUE_MULTIPLIER));
	}

	/**
	 * Create a color with a lighter hue than the given color.
	 * 
	 * @param color
	 * @return Lighter color
	 */
	public static Color lighter(Color color)
	{
		return new Color(null, Math.max(2, Math.min((int) (color.getRed() / RGB_VALUE_MULTIPLIER), 255)), Math.max(2,
				Math.min((int) (color.getGreen() / RGB_VALUE_MULTIPLIER), 255)), Math.max(2,
				Math.min((int) (color.getBlue() / RGB_VALUE_MULTIPLIER), 255)));
	}

	/**
	 * Should the given color be darkened, or lightened? Returns true if the
	 * average value for the red/green/blue components is greater than 128.
	 * 
	 * @param color
	 * @return True if the given color should be darkened
	 */
	public static boolean shouldDarken(Color color)
	{
		return (color.getRed() + color.getGreen() + color.getBlue()) > 128 * 3;
	}
}
