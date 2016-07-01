/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;
import au.gov.ga.earthsci.worldwind.common.util.FileUtil;

/**
 * Color map reader for GOCAD .cmap files.
 *
 * @author Michael de Hoog
 */
public class GOCADColorMapReader implements IColorMapReader
{
	private static final Pattern COLOR_LINE_PATTERN = Pattern.compile("(\\d+)\\s+([\\d.]+)\\s+([\\d.]+)\\s+([\\d.]+)"); //$NON-NLS-1$

	@Override
	public String getName()
	{
		return "GOCAD color map reader";
	}

	@Override
	public String getDescription()
	{
		return "Reads color maps in the format supported by GOCAD";
	}

	@Override
	public boolean supports(Object source)
	{
		try
		{
			return read(source) != null;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	@Override
	public ColorMap read(Object source) throws IOException
	{
		if (!(source instanceof URL || source instanceof File || source instanceof String))
		{
			return null;
		}

		String extension = FileUtil.getExtension(source.toString());
		if (!extension.toLowerCase().equals("cmap")) //$NON-NLS-1$
		{
			return null;
		}

		InputStream is;
		if (source instanceof String)
		{
			is = new FileInputStream((String) source);
		}
		else if (source instanceof File)
		{
			is = new FileInputStream((File) source);
		}
		else
		{
			is = ((URL) source).openStream();
		}

		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			Map<Double, Color> colors = new HashMap<Double, Color>();

			String line;
			while ((line = reader.readLine()) != null)
			{
				Matcher matcher = COLOR_LINE_PATTERN.matcher(line);
				if (matcher.find())
				{
					double[] values = new double[4];
					for (int i = 0; i < values.length; i++)
					{
						values[i] = Double.parseDouble(matcher.group(i + 1));
					}
					double percent = values[0] / 255d;
					Color color = new Color((float) values[1], (float) values[2], (float) values[3]);
					colors.put(percent, color);
				}
			}

			String filenameWithoutExtension = FileUtil.stripExtension(FileUtil.getFilename(source.toString()));
			return new ColorMap("GOCAD " + filenameWithoutExtension,
					"GOCAD " + filenameWithoutExtension + " color map", colors, null,
					InterpolationMode.INTERPOLATE_RGB, true);
		}
		finally
		{
			is.close();
		}
	}
}
