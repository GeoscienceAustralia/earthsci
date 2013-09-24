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

import au.gov.ga.earthsci.common.color.ColorType.Channel;
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

	/**
	 * Sample the given color map and return an array of sampled colors as float
	 * values per channel, controlled by the provided {@link ColorType}.
	 * <p/>
	 * The map will be sampled in the range {@code [minValue, maxValue]} using
	 * {@code numSamples} samples.
	 * <p/>
	 * Equivalent to the call:
	 * 
	 * <pre>
	 * sample(map, numSamples, minValue, maxValue, new float[numSamples * type.getNumComponents()], 0);
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
	 * @param type
	 *            The type of color value to store in the float array
	 * 
	 * @return an array of float color values sampled from the map. The result
	 *         will have length {@code numSamples * type.getNumComponents()}.
	 */
	@SuppressWarnings("nls")
	public static float[] sample(ColorMap map, int numSamples,
			double minValue, double maxValue, ColorType type)
	{
		Validate.isTrue(numSamples >= 0, "numSamples must be a non-negative integer.");
		Validate.notNull(type, "A color type must be provided");
		return sample(map, numSamples, minValue, maxValue, new float[numSamples * type.getNumComponents()], 0, type);
	}

	/**
	 * Sample the given color map and populate the given target array with float
	 * values for each color channel, set as per the provided {@link ColorType}.
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
	 *            {@code length >= (numSamples * type.getNumComponents()) + offset}
	 *            .
	 * @param offset
	 *            The offset into the array at which sample should be added
	 * @param type
	 *            The color type (and pattern) to use when writing color
	 *            channels into the target array
	 * 
	 * @return The target array
	 */
	@SuppressWarnings("nls")
	public static float[] sample(final ColorMap map, final int numSamples,
			double minValue, double maxValue,
			final float[] target, final int offset,
			ColorType type)
	{
		Validate.notNull(map, "A ColorMap is required");
		Validate.isTrue(numSamples >= 0, "numSamples must be a non-negative integer.");
		Validate.notNull(target, "A target array is required");
		Validate.notNull(type, "A target color type is required");
		Validate.isTrue(offset >= 0, "Offset must be a non-negative integer");
		Validate.isTrue(target.length >= (numSamples * type.getNumComponents()) + offset,
				"The target array is not large enough to contain the desired number of samples. Got " +
						target.length + ", need " + ((numSamples * type.getNumComponents()) + offset));

		minValue = Math.min(minValue, maxValue);
		maxValue = Math.max(minValue, maxValue);

		// Special case - take single samples as the minValue
		if (numSamples == 1)
		{
			Color c = map.getColor(minValue, minValue, maxValue);
			toFloats(c, target, null, offset, type);
			return target;
		}

		// General case - for more than 1 sample, sample along the interval [minValue, maxValue]
		float[] rgbTemp = new float[4];
		for (int i = 0; i < numSamples; i++)
		{
			double sampleValue = ((double) i / (numSamples - 1)) * (maxValue - minValue) + minValue;
			Color color = map.getColor(sampleValue, minValue, maxValue);
			toFloats(color, target, rgbTemp, offset + (i * type.getNumComponents()), type);
		}
		return target;
	}

	private static void toFloats(Color c, float[] target, float[] rgbTemp, int offset, ColorType type)
	{
		float[] rgb = c.getRGBColorComponents(rgbTemp);
		if (type.hasChannel(Channel.RED))
		{
			target[offset + type.getChannelIndex(Channel.RED)] = rgb[0];
		}
		if (type.hasChannel(Channel.GREEN))
		{
			target[offset + type.getChannelIndex(Channel.GREEN)] = rgb[1];
		}
		if (type.hasChannel(Channel.BLUE))
		{
			target[offset + type.getChannelIndex(Channel.BLUE)] = rgb[2];
		}
		if (type.hasChannel(Channel.ALPHA))
		{
			target[offset + type.getChannelIndex(Channel.ALPHA)] = (c.getAlpha() / 255.0f);
		}
	}

}
