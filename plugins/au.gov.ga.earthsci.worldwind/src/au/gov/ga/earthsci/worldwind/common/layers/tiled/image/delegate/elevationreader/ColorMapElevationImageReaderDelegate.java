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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.elevationreader;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * Treats the retrieved image tiles as an elevation model, and applies a colour
 * map based on the elevations recorded in the retrieved tiles.
 * <p/>
 * <code>&lt;Delegate&gt;ColorMapReader(pixelType,byteOrder,missingData)&lt;/Delegate&gt;</code>
 * Where:
 * <ul>
 * <li>pixelType = the pixel format of the elevation tiles (one of "
 * <code>Float32</code>", "<code>Int32</code>", "<code>Int16</code>" or "
 * <code>Int8</code>")
 * <li>byteOrder = the byte order of the elevation tiles (one of "
 * <code>little</code>" or "<code>big</code>")
 * <li>missingData = the value used in the elevation tiles to represent missing
 * data (float)
 * </ul>
 * <p/>
 * When parsing from a layer definition file, the colour map must be provided in
 * the layer xml, as follows:
 * 
 * <pre>
 * &lt;ColorMap interpolateHue="true"&gt;
 *   &lt;Entry elevation="e" red="r" green="g" blue="b" alpha="a"/&gt;
 *   ...
 * &lt;/ColorMap&gt;
 * </pre>
 * 
 * Where:
 * <ul>
 * <li>interpolateHue = if true, will use the HSB colour space and interpolate
 * between elevations using hue. If false, will use the RGB colour space and
 * interpolate each channel.
 * <li>elevation = the elevation at which this colour map entry applies (float
 * in metres)
 * <li>red = the red channel of the colour entry (integer in range [0, 255])
 * <li>green = the green channel of the colour entry (integer in range [0, 255])
 * <li>blue = the blue channel of the colour entry (integer in range [0, 255])
 * <li>alpha = the alpha channel of the colour entry (integer in range [0, 255])
 * </ul>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorMapElevationImageReaderDelegate extends ElevationImageReaderDelegate
{
	private final static String DEFINITION_STRING = "ColorMapReader";

	private final ColorMap colorMap;

	@SuppressWarnings("unused")
	private ColorMapElevationImageReaderDelegate()
	{
		this(AVKey.INT16, AVKey.LITTLE_ENDIAN, -Double.MAX_VALUE, new ColorMap());
	}

	public ColorMapElevationImageReaderDelegate(String pixelType, String byteOrder, Double missingDataSignal,
			ColorMap colorMap)
	{
		super(pixelType, byteOrder, missingDataSignal);
		this.colorMap = colorMap;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\w+),(\\w+)," + doublePattern + "\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				String pixelType = matcher.group(1);
				String byteOrder = matcher.group(2);
				double missingDataSignal = Double.parseDouble(matcher.group(3));

				ColorMap colorMap = XMLUtil.getColorMap(layerElement, "ColorMap", null);
				return new ColorMapElevationImageReaderDelegate(WWXML.parseDataType(pixelType),
						WWXML.parseByteOrder(byteOrder), missingDataSignal, colorMap);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		if (layerElement != null)
		{
			XMLUtil.appendColorMap(layerElement, "ColorMap", colorMap);
		}
		return DEFINITION_STRING + "(" + WWXML.dataTypeAsText(pixelType) + "," + WWXML.byteOrderAsText(byteOrder) + ","
				+ missingDataSignal + ")";
	}

	@Override
	protected BufferedImage generateImage(BufferWrapper elevations, int width, int height, Globe globe, Sector sector)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0, i = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++, i++)
			{
				double elevation = elevations.getDouble(i);
				if (elevation == missingDataSignal)
				{
					image.setRGB(x, y, 0);
				}
				else
				{
					int rgba = colorMap.calculateColor(elevation).getRGB();
					image.setRGB(x, y, rgba);
				}
			}
		}

		return image;
	}
}
