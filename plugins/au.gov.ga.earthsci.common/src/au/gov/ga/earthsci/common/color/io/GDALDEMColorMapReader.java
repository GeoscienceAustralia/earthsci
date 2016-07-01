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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;
import au.gov.ga.earthsci.common.color.MutableColorMap;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Reads {@link ColorMap} instances from colour map files of the format
 * supported by the <a
 * href="http://www.gdal.org/gdaldem.html#gdaldem_color_relief">gdaldem</a>
 * utility.
 * <p/>
 * This reader will also look for special comments in the file (lines beginning
 * with '#') that can be used to control how the map behaves. These comments
 * have the form:
 * 
 * <pre>
 * #ATTRIBUTE=VALUE
 * </pre>
 * 
 * For example, to set the name for the map:
 * 
 * <pre>
 * #NAME=My colour map
 * </pre>
 * <p/>
 * Valid attribute names are:
 * <p/>
 * <dl>
 * <dt>NAME</dt>
 * <dd>The name for the colour map</dd>
 * <dt>DESCRIPTION</dt>
 * <dd>The description for the colour map</dd>
 * <dt>MODE</dt>
 * <dd>The interpolation mode to use for the colour map. Valid values are:
 * <ul>
 * <li>NEAREST_MATCH</li>
 * <li>EXACT_MATCH</li>
 * <li>INTERPOLATE_RGB</li>
 * <li>INTERPOLATE_HUE</li>
 * </ul>
 * If not specified, INTERPOLATE_RGB is used.
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class GDALDEMColorMapReader implements IColorMapReader
{

	private static final Logger logger = LoggerFactory.getLogger(GDALDEMColorMapReader.class);

	private static final Pattern ATTRIBUTE_LINE_PATTERN = Pattern.compile("#\\s*(\\w+)\\s*=\\s*(.*?)\\s*$"); //$NON-NLS-1$
	private static final Pattern COLOR_LINE_PATTERN = Pattern
			.compile("(?:((?:\\d+(?:\\.\\d+)?%?)|(?:nv))[ \t:,]+)((?:(?:\\d+(?:\\.\\d+)?)[ \t:,]*){3,4})"); //$NON-NLS-1$
	private static final Pattern NAMED_COLOR_LINE_PATTERN = Pattern
			.compile("(?:((?:\\d+(?:\\.\\d+)?%?)|(?:nv))[ \t:,]+)(\\w+)"); //$NON-NLS-1$

	private static final Map<String, Color> NAMED_COLORS = new HashMap<String, Color>()
	{
		{
			put("WHITE", Color.WHITE); //$NON-NLS-1$
			put("BLACK", Color.BLACK); //$NON-NLS-1$
			put("RED", Color.RED); //$NON-NLS-1$
			put("GREEN", Color.GREEN); //$NON-NLS-1$
			put("BLUE", Color.BLUE); //$NON-NLS-1$
			put("YELLOW", Color.YELLOW); //$NON-NLS-1$
			put("MAGENTA", Color.MAGENTA); //$NON-NLS-1$
			put("CYAN", Color.CYAN); //$NON-NLS-1$
			put("AQUA", Color.CYAN); //$NON-NLS-1$
			put("GRAY", Color.GRAY); //$NON-NLS-1$
			put("GREY", Color.GRAY); //$NON-NLS-1$
			put("ORANGE", Color.ORANGE); //$NON-NLS-1$
			put("BROWN", new Color(150, 75, 0)); //$NON-NLS-1$
			put("PURPLE", new Color(128, 0, 128)); //$NON-NLS-1$
			put("VIOLET", new Color(128, 0, 128)); //$NON-NLS-1$
			put("INDIGO", new Color(75, 0, 130)); //$NON-NLS-1$
		}
	};

	@Override
	public String getName()
	{
		return Messages.GDALDEMColorMapReader_ReaderName;
	}

	@Override
	public String getDescription()
	{
		return Messages.GDALDEMColorMapReader_ReaderDescription;
	}

	@Override
	public boolean supports(Object source)
	{
		if (source == null)
		{
			return false;
		}

		try
		{
			return open(source) != null;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	@Override
	public ColorMap read(Object source) throws IOException
	{
		BufferedReader reader = open(source);
		if (reader == null)
		{
			return null;
		}

		MutableColorMap map = new MutableColorMap();

		String line = null;
		while ((line = reader.readLine()) != null)
		{
			Matcher m = null;

			m = ATTRIBUTE_LINE_PATTERN.matcher(line);
			if (m.matches())
			{
				String name = m.group(1);
				String value = m.group(2);

				setAttribute(map, name, value);
				continue;
			}

			m = NAMED_COLOR_LINE_PATTERN.matcher(line);
			if (m.matches())
			{
				String value = m.group(1);
				String namedColor = m.group(2);

				addEntry(map, value, namedColor);
				continue;
			}

			m = COLOR_LINE_PATTERN.matcher(line);
			if (m.matches())
			{
				String value = m.group(1);
				String[] colorComponents = m.group(2).split("[\\s,:]+"); //$NON-NLS-1$
				addEntry(map, value, colorComponents);
				continue;
			}

		}

		reader.close();

		return map.snapshot();
	}

	private void addEntry(MutableColorMap map, String value, String namedColor)
	{
		if (map == null || value == null || namedColor == null)
		{
			return;
		}

		Color color = NAMED_COLORS.get(namedColor.toUpperCase());
		if (color == null)
		{
			logger.debug("Unknown color '{}'", namedColor); //$NON-NLS-1$
			return;
		}

		addEntry(map, value, color);
	}

	private void addEntry(MutableColorMap map, String value, String[] colorComponents)
	{
		if (map == null || value == null || colorComponents == null || colorComponents.length == 0)
		{
			return;
		}

		Color color = toColor(colorComponents);
		if (color == null)
		{
			logger.debug("Unknown color " + Arrays.asList(colorComponents)); //$NON-NLS-1$
			return;
		}

		addEntry(map, value, color);
	}

	private void addEntry(MutableColorMap map, String value, Color color)
	{
		if (value.equalsIgnoreCase("nv")) //$NON-NLS-1$
		{
			map.setNodataColour(color);
			return;
		}

		// We can't mix percentages and absolute values in the ColorMap, so first one wins.
		if (isPercentage(value))
		{
			if (!map.isPercentageBased() && !map.isEmpty())
			{
				return;
			}
			else if (map.isEmpty())
			{
				map.setValuesArePercentages(true, 0.0, 1.0);
			}
		}
		else
		{
			if (map.isPercentageBased() && !map.isEmpty())
			{
				return;
			}
		}

		double theValue = toValue(value);

		map.addEntry(theValue, color);
	}

	private void setAttribute(MutableColorMap map, String name, String value)
	{
		if (map == null || name == null || value == null)
		{
			return;
		}

		if ("NAME".equalsIgnoreCase(name)) //$NON-NLS-1$
		{
			map.setName(value);
			return;
		}

		if ("DESCRIPTION".equalsIgnoreCase(name)) //$NON-NLS-1$
		{
			map.setDescription(value);
			return;
		}

		if ("MODE".equalsIgnoreCase(name)) //$NON-NLS-1$
		{
			try
			{
				InterpolationMode mode = InterpolationMode.valueOf(value.toUpperCase());
				map.setMode(mode);
			}
			catch (Exception e)
			{
				// Must be an invalid mode name
			}
			return;
		}
	}

	private double toValue(String valueString)
	{
		boolean isPercentage = isPercentage(valueString);
		if (isPercentage)
		{
			valueString = valueString.substring(0, valueString.length() - 1);
		}

		Double value = Double.valueOf(valueString);
		if (isPercentage)
		{
			value = value / 100.0;
		}

		return value;
	}

	private boolean isPercentage(String valueString)
	{
		return valueString.endsWith("%"); //$NON-NLS-1$
	}

	private Color toColor(String[] components)
	{
		if (components == null || components.length < 3 || components.length > 4)
		{
			return null;
		}

		Integer r, g, b, a;

		r = toColorChannel(components[0]);
		g = toColorChannel(components[1]);
		b = toColorChannel(components[2]);
		if (components.length > 3)
		{
			a = toColorChannel(components[3]);
		}
		else
		{
			a = 255;
		}

		if (r == null || g == null || b == null || a == null)
		{
			return null;
		}
		return new Color(r, g, b, a);
	}

	private Integer toColorChannel(String value)
	{
		try
		{
			return Util.clamp(Integer.parseInt(value), 0, 255);
		}
		catch (Exception e1)
		{
			try
			{
				float val = Float.parseFloat(value);
				if (val > 1.0)
				{
					return (int) val;
				}
				return (int) (val * 255);

			}
			catch (Exception e2)
			{
				return null;
			}
		}
	}

	private BufferedReader open(Object source) throws IOException
	{
		InputStream is = null;
		if (source instanceof String)
		{
			is = new ByteArrayInputStream(((String) source).getBytes());
		}
		else if (source instanceof InputStream)
		{
			is = (InputStream) source;
		}
		else if (source instanceof File)
		{
			is = new FileInputStream((File) source);
		}
		else if (source instanceof URL)
		{
			is = ((URL) source).openStream();
		}

		if (is == null)
		{
			return null;
		}
		return new BufferedReader(new InputStreamReader(is));
	}

}
