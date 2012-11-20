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
package au.gov.ga.earthsci.worldwind.common.layers.geonames;

import java.util.HashMap;

import au.gov.ga.earthsci.worldwind.common.util.ColorFont;

/**
 * Simple map between a String and {@link ColorFont}. Used for providing
 * different font/color combinations for different attribute values.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorFontProvider extends HashMap<String, ColorFont>
{
	private ColorFont def;

	public ColorFontProvider()
	{
		def = new ColorFont(null, null, null);
	}

	public ColorFontProvider(ColorFont def)
	{
		this.def = def;
	}

	@Override
	public ColorFont get(Object key)
	{
		ColorFont font = super.get(key);
		if (font == null)
		{
			font = def;
		}
		return font;
	}
}
