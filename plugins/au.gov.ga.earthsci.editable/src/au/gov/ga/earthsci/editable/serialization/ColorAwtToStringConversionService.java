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

import org.eclipse.sapphire.ConversionException;
import org.eclipse.sapphire.ConversionService;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class ColorAwtToStringConversionService extends ConversionService<Color, String>
{
	public ColorAwtToStringConversionService()
	{
		super(Color.class, String.class);
	}

	@Override
	public String convert(Color color) throws ConversionException
	{
		int a = color.getAlpha();
		String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); //$NON-NLS-1$
		if (a != 255)
		{
			hex += String.format("%02x", a); //$NON-NLS-1$
		}
		return hex;
	}
}
