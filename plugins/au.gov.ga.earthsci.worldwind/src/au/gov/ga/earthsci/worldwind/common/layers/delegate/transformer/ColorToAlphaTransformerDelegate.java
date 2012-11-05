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
 * Implementation of {@link IImageTransformerDelegate} which applies a
 * color-to-alpha transformation on the incoming images. 
 * <p/>
 * The color-to-alpha implementation is similar to GIMP's: The alpha level of each processed pixel
 * is proportional to how close that pixel is to the target colour.
 * 
 * <p/>
 * 
 * <code>&lt;Delegate&gt;ColorToAlphaTransformer(r,g,b)&lt;/Delegate&gt;</code>
 * <ul>
 * 	<li>r = target colour red channel (integer in range [0, 255])
 *  <li>g = target colour green channel (integer in range [0, 255])
 *  <li>b = target colour blue channel (integer in range [0, 255])
 * </ul>
 * 
 * @see http://manual.gimp.org/en/plug-in-colortoalpha.html
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorToAlphaTransformerDelegate implements IImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "ColorToAlphaTransformer";

	protected final Color color;

	//for reflection instantiation
	@SuppressWarnings("unused")
	private ColorToAlphaTransformerDelegate()
	{
		this(Color.black);
	}

	public ColorToAlphaTransformerDelegate(Color color)
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
		return DEFINITION_STRING + "(" + color.getRed() + "," + color.getGreen() + ","
				+ color.getBlue() + ")";
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
				return new ColorToAlphaTransformerDelegate(color);
			}
		}
		return null;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image, IDelegatorTile tile)
	{
		if (image == null)
			return null;

		BufferedImage dst =
				new BufferedImage(image.getWidth(), image.getHeight(),
						BufferedImage.TYPE_INT_ARGB_PRE);

		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int rgb = image.getRGB(x, y);
				rgb = colorToAlpha(rgb, color);
				dst.setRGB(x, y, rgb);
			}
		}

		return dst;
	}

	/**
	 * Transform an ARGB color by removing a certain color and replacing it with
	 * transparency.
	 * 
	 * @param argb
	 *            Color to transform
	 * @param color
	 *            Color to remove
	 * @return Transformed ARGB color
	 */
	public static int colorToAlpha(int argb, Color color)
	{
		int a = (argb >> 24) & 0xff;
		int r = (argb >> 16) & 0xff;
		int g = (argb >> 8) & 0xff;
		int b = (argb) & 0xff;

		float pr = distancePercent(r, color.getRed(), 0, 255);
		float pg = distancePercent(g, color.getGreen(), 0, 255);
		float pb = distancePercent(b, color.getBlue(), 0, 255);
		float percent = Math.max(pr, Math.max(pg, pb));

		//(image - color) / alpha + color
		if (percent > 0)
		{
			r = (int) ((r - color.getRed()) / percent) + color.getRed();
			g = (int) ((g - color.getGreen()) / percent) + color.getGreen();
			b = (int) ((b - color.getBlue()) / percent) + color.getBlue();
		}
		a = (int) (a * percent);

		return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
	}

	/**
	 * Percent distance between value and distanceTo within the windows between
	 * min and distanceTo or distanceTo and max.
	 * 
	 * @param value
	 * @param distanceTo
	 * @param min
	 * @param max
	 * @return
	 */
	protected static float distancePercent(int value, int distanceTo, int min, int max)
	{
		float diff = 0f;
		if (value < distanceTo)
		{
			diff = (distanceTo - value) / (float) (distanceTo - min);
		}
		else if (value > distanceTo)
		{
			diff = (value - distanceTo) / (float) (max - distanceTo);
		}
		return Math.max(0f, Math.min(1f, diff));
	}
}
