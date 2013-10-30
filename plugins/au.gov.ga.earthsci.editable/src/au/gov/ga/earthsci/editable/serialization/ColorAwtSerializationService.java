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
package au.gov.ga.earthsci.editable.serialization;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.sapphire.services.ValueSerializationService;

/**
 * {@link ValueSerializationService} for {@link java.awt.Color} objects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorAwtSerializationService extends ValueSerializationService
{
	@Override
	public String encode(Object value)
	{
		if (!(value instanceof Color))
		{
			return null;
		}
		Color color = (Color) value;
		int a = color.getAlpha();
		String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); //$NON-NLS-1$
		if (a != 255)
		{
			hex += String.format("%02x", a); //$NON-NLS-1$
		}
		return hex;
	}

	@Override
	protected Object decodeFromString(String value)
	{
		if (value == null)
		{
			return null;
		}

		Pattern pattern = Pattern.compile("#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})?"); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(value);
		if (!matcher.matches())
		{
			return null;
		}

		String rs = matcher.group(1);
		String gs = matcher.group(2);
		String bs = matcher.group(3);
		String as = matcher.group(4);
		int r = Integer.parseInt(rs, 16);
		int g = Integer.parseInt(gs, 16);
		int b = Integer.parseInt(bs, 16);
		int a = as == null ? 255 : Integer.parseInt(as, 16);
		return new Color(r, g, b, a);
	}
}
