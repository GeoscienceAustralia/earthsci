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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IImageTransformerDelegate;

/**
 * A delegate that limits each colour channel of the target image to be less than a 
 * provided limiting colour.
 * <p/>
 * <code>&lt;Delegate&gt;ColorLimitTransformer(r,g,b)&lt;/Delegate&gt;</code>
 * <ul>
 * 	<li>r = limit for the red channel (integer in range [0, 255])
 *  <li>g = limit for the green channel (integer in range [0, 255])
 *  <li>b = limit for the blue channel (integer in range [0, 255])
 * </ul>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorLimitTransformerDelegate implements IImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "ColorLimitTransformer";

	protected final Color color;

	//for reflection instantiation
	@SuppressWarnings("unused")
	private ColorLimitTransformerDelegate()
	{
		this(Color.black);
	}

	public ColorLimitTransformerDelegate(Color color)
	{
		this.color = color;
	}

	public Color getColor()
	{
		return color;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\d+),(\\d+),(\\d+)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				int r = Integer.parseInt(matcher.group(1));
				int g = Integer.parseInt(matcher.group(2));
				int b = Integer.parseInt(matcher.group(3));
				Color color = new Color(r, g, b);
				return new ColorLimitTransformerDelegate(color);
			}
		}
		return null;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image, IDelegatorTile tile)
	{
		if (image == null)
			return null;

		BufferedImage dst = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int rgb = image.getRGB(x, y);
				rgb = limitColor(rgb, color);
				dst.setRGB(x, y, rgb);
			}
		}

		return dst;
	}

	protected static int limitColor(int argb, Color color)
	{
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = (argb) & 0xff;

		if (r > color.getRed() || g > color.getGreen() || b > color.getBlue())
		{
			a = 0;
		}

		return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
	}
}
