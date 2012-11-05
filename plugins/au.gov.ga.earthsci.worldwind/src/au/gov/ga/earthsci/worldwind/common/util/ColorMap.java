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

import java.awt.Color;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Color map that allows mapping from a double value to a color. Contains
 * interpolation functionality for interpolating between the two closest color
 * mappings for a certain double value.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorMap extends TreeMap<Double, Color>
{
	private boolean interpolateHue = true;
	private boolean valuesPercentages = false;

	/**
	 * @return Should the interpolation be performed in the HSB color space? If
	 *         not, the RGB color space is used.
	 */
	public boolean isInterpolateHue()
	{
		return interpolateHue;
	}

	/**
	 * Set whether the interpolation should be performed in the HSB color space.
	 * 
	 * @param interpolateHue
	 */
	public void setInterpolateHue(boolean interpolateHue)
	{
		this.interpolateHue = interpolateHue;
	}

	/**
	 * @return Should the values mapped to the colors in this map be treated as
	 *         percentages?
	 */
	public boolean isValuesPercentages()
	{
		return valuesPercentages;
	}

	/**
	 * Set whether the values mapped to the colors in the map should be treated
	 * as percentages.
	 * 
	 * @param valuesPercentages
	 */
	public void setValuesPercentages(boolean valuesPercentages)
	{
		this.valuesPercentages = valuesPercentages;
	}

	/**
	 * Calculate the color for the given double value. If there is no exact
	 * mapping for this value, the two closest color mappings either side of the
	 * provided value are interpolated. If this contains no colors, black is
	 * returned.
	 * 
	 * @param value
	 * @return Color at value
	 */
	public Color calculateColor(double value)
	{
		Entry<Double, Color> lessEntry = floorEntry(value);
		Entry<Double, Color> greaterEntry = ceilingEntry(value);
		double mixer = 0;
		if (lessEntry != null && greaterEntry != null)
		{
			double window = greaterEntry.getKey() - lessEntry.getKey();
			if (window > 0)
			{
				mixer = (value - lessEntry.getKey()) / window;
			}
		}
		Color color0 = lessEntry == null ? null : lessEntry.getValue();
		Color color1 = greaterEntry == null ? null : greaterEntry.getValue();
		return Util.interpolateColor(color0, color1, mixer, interpolateHue);
	}

	/**
	 * Calculate the color for the given double value as a percentage between
	 * the given minimum and maximum. The given value is scaled between 0 and 1
	 * before passing to the {@link #calculateColor(double)} function.
	 * 
	 * @param value
	 * @param minimum
	 * @param maximum
	 * @return Color at value
	 */
	public Color calculateColorAsPercentage(double value, double minimum, double maximum)
	{
		return calculateColor((value - minimum) / (maximum - minimum));
	}

	/**
	 * Calculate the color for the given double value. If
	 * {@link #isValuesPercentages()} is true, the given value is scaled between
	 * 0 and 1 (using the given minimum and maximum) before being passed to the
	 * {@link #calculateColor(double)} function.
	 * 
	 * @param value
	 * @param minimum
	 * @param maximum
	 * @return Color at value
	 */
	public Color calculateColorNotingIsValuesPercentages(double value, double minimum, double maximum)
	{
		if (isValuesPercentages())
			return calculateColorAsPercentage(value, minimum, maximum);
		return calculateColor(value);
	}
}
