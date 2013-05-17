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
package au.gov.ga.earthsci.common.color.io;

import java.awt.Color;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;
import au.gov.ga.earthsci.common.color.ColorMapBuilder;
import au.gov.ga.earthsci.common.util.Util;

/**
 * Reads {@link ColorMap} from a compact string format:
 * 
 * @see CompactStringColorMapWriter
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class CompactStringColorMapReader implements IColorMapReader
{

	public static final String FORMAT =
			"(.+?)\\|(.*?)\\|(.*?)\\|([0,1]?)\\|([-]?[\\d]*)\\|((?:(?:[-]?[\\d]+(?:\\.[\\d]+)),(?:[-]?[\\d]+)(?:,)?)+)"; //$NON-NLS-1$

	@Override
	public String getName()
	{
		return Messages.CompactStringColorMapReader_ReaderName;
	}

	@Override
	public String getDescription()
	{
		return Messages.CompactStringColorMapReader_ReaderDescription;
	}

	@Override
	public boolean supports(Object source)
	{
		return source instanceof String && ((String) source).matches(FORMAT);
	}

	@Override
	public ColorMap read(Object source) throws IOException
	{
		if (!supports(source))
		{
			throw new IllegalArgumentException("Source not supported by this reader. Use supports() to test."); //$NON-NLS-1$
		}

		Matcher m = Pattern.compile(FORMAT).matcher((String) source);
		m.find();

		ColorMapBuilder builder = new ColorMapBuilder();

		builder.named(m.group(1));

		builder.describedAs(Util.isEmpty(m.group(2)) ? null : m.group(2));

		builder.using(Util.isEmpty(m.group(3)) ?
				InterpolationMode.INTERPOLATE_RGB :
				InterpolationMode.valueOf(m.group(3)));

		builder.withValuesAsPercentages(!m.group(4).equals("0")); //$NON-NLS-1$

		builder.withNodata(Util.isEmpty(m.group(5)) ? null : new Color(Integer.parseInt(m.group(5)), true));

		String[] entryParts = m.group(6).split(","); //$NON-NLS-1$
		for (int i = 0; i < entryParts.length; i += 2)
		{
			double value = Double.parseDouble(entryParts[i]);
			Color color = new Color(Integer.parseInt(entryParts[i + 1]), true);

			builder.withEntry(value, color);
		}

		return builder.build();
	}

}
