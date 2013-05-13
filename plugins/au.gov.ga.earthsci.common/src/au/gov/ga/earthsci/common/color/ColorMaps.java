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
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;

/**
 * A class that gives static access to commonly used colour maps
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ColorMaps
{

	private ColorMaps()
	{
	}

	private static ColorMap RBG_RAINBOW_MAP;
	private static ColorMap RGB_RAINBOW_MAP;

	/**
	 * Return a standard red-blue-green rainbow colour map
	 */
	public static ColorMap getRBGRainbowMap()
	{
		if (RBG_RAINBOW_MAP == null)
		{
			Map<Double, Color> entries = new HashMap<Double, Color>();
			entries.put(0.0, Color.RED);
			entries.put(0.5, Color.BLUE);
			entries.put(1.0, Color.GREEN);
			RBG_RAINBOW_MAP =
					new ColorMap(Messages.ColorMaps_RBGName, Messages.ColorMaps_RBGDescription,
							entries, null, InterpolationMode.INTERPOLATE_HUE, true);
		}
		return RBG_RAINBOW_MAP;
	}

	/**
	 * Return a standard red-green-blue rainbow colour map
	 */
	public static ColorMap getRGBRainbowMap()
	{
		if (RGB_RAINBOW_MAP == null)
		{
			Map<Double, Color> entries = new HashMap<Double, Color>();
			entries.put(0.0, Color.RED);
			entries.put(0.5, Color.GREEN);
			entries.put(1.0, Color.BLUE);
			RGB_RAINBOW_MAP =
					new ColorMap(Messages.ColorMaps_RGBName, Messages.ColorMaps_RGBDescription,
							entries, null, InterpolationMode.INTERPOLATE_HUE, true);
		}
		return RGB_RAINBOW_MAP;
	}

}
