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
package au.gov.ga.earthsci.common.color;

import java.awt.Color;

import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A utility class that can sample a {@link ColorMap} and return the result in a
 * number of formats useful in different applications.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ColorMapSampler
{

	private ColorMapSampler()
	{
	}

	/**
	 * Sample the given color map and return an array of sampled {@link Color}
	 * objects.
	 * <p/>
	 * The map will be sampled in the range {@code [minValue, maxValue]} using
	 * {@code numSamples} samples.
	 * <p/>
	 * Equivalent to the call:
	 * 
	 * <pre>
	 * sample(map, numSamples, minValue, maxValue, new Color[numSamples], 0);
	 * </pre>
	 * 
	 * @param map
	 *            The map to sample from
	 * @param numSamples
	 *            The number of samples to take from the map
	 * @param minValue
	 *            The minimum data value to use when sampling
	 * @param maxValue
	 *            The maximum data value to use when sampling
	 * 
	 * @return an array of {@link Color} objects sampled from the map. The
	 *         result will have length {@code numSamples}.
	 */
	@SuppressWarnings("nls")
	public static Color[] sample(ColorMap map, int numSamples, double minValue, double maxValue)
	{
		Validate.isTrue(numSamples >= 0, "numSamples must be a non-negative integer.");
		return sample(map, numSamples, minValue, maxValue, new Color[numSamples], 0);
	}

	/**
	 * Sample the given color map and populate the given target array with
	 * {@link Color} objects.
	 * <p/>
	 * The map will be sampled in the range {@code [minValue, maxValue]} using
	 * {@code numSamples} samples.
	 * 
	 * @param map
	 *            The map to sample from. Must be non-null.
	 * @param numSamples
	 *            The number of samples to take from the map. Must be a non
	 * @param minValue
	 *            The minimum data value to use when sampling
	 * @param maxValue
	 *            The maximum data value to use when sampling
	 * @param target
	 *            The array to put samples into. Must have
	 *            {@code length >= numSamples + offset}.
	 * @param offset
	 *            The offset into the array at which sample should be added
	 * 
	 * @return The target array
	 */
	@SuppressWarnings("nls")
	public static Color[] sample(final ColorMap map, final int numSamples,
			double minValue, double maxValue,
			final Color[] target, final int offset)
	{
		Validate.notNull(map, "A ColorMap is required");
		Validate.isTrue(numSamples >= 0, "numSamples must be a non-negative integer.");
		Validate.notNull(target, "A target array is required");
		Validate.isTrue(offset >= 0, "Offset must be a non-negative integer");
		Validate.isTrue(target.length >= numSamples + offset,
				"The target array is not large enough to contain the desired number of samples. Got " +
						target.length + ", need " + (numSamples + offset));

		minValue = Math.min(minValue, maxValue);
		maxValue = Math.max(minValue, maxValue);

		// Special case - take single samples as the minValue
		if (numSamples == 1)
		{
			target[offset] = map.getColor(minValue, minValue, maxValue);
			return target;
		}

		// General case - for more than 1 sample, sample along the interval [minValue, maxValue]
		for (int i = 0; i < numSamples; i++)
		{
			double sampleValue = ((double) i / (numSamples - 1)) * (maxValue - minValue) + minValue;
			Color color = map.getColor(sampleValue, minValue, maxValue);
			target[offset + i] = color;
		}
		return target;
	}

}
