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
 * Implementation of {@link IImageTransformerDelegate} which converts a targeted
 * colour to transparency with fuzziness.
 * <p/>
 * Similar to the {@link ColorToAlphaTransformerDelegate}, but sets any colour within <code>fuzz</code> percent
 * of the target colour to fully transparent.
 * <p/>
 * Useful for masking images that do not have an alpha channel but provide a 'mask' colour.
 * <p/>
 * <code>&lt;Delegate&gt;TransparentColorTransformer(r,g,b,fuzz)&lt;/Delegate&gt;</code>
 * <ul>
 * 	<li>r = target colour red channel (integer in range [0, 255])
 *  <li>g = target colour green channel (integer in range [0, 255])
 *  <li>b = target colour blue channel (integer in range [0, 255])
 *  <li>fuzz = controls how close to the target colour a pixel must be to be made transparent 
 *  (double in range [0, 1], 0 = only the target colour, 1 = all colours)
 * </ul>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TransparentColorTransformerDelegate implements IImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "TransparentColorTransformer";

	protected final Color color;
	protected final double fuzz;

	//for reflection instantiation
	@SuppressWarnings("unused")
	private TransparentColorTransformerDelegate()
	{
		this(Color.black, 0d);
	}

	public TransparentColorTransformerDelegate(Color color, double fuzz)
	{
		this.color = color;
		this.fuzz = fuzz;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image, IDelegatorTile tile)
	{
		int fuzzi = Math.max(0, Math.min(255, (int) Math.round(fuzz * 255d)));
		BufferedImage trans =
				new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

		int cr = color.getRed();
		int cg = color.getGreen();
		int cb = color.getBlue();

		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				int rgb = image.getRGB(x, y);
				int sr = (rgb >> 16) & 0xff;
				int sg = (rgb >> 8) & 0xff;
				int sb = (rgb >> 0) & 0xff;
				if (cr - fuzzi <= sr && sr <= cr + fuzzi && cg - fuzzi <= sg && sg <= cg + fuzzi
						&& cb - fuzzi <= sb && sb <= cb + fuzzi)
				{
					rgb = (rgb & 0xffffff);
				}
				trans.setRGB(x, y, rgb);
			}
		}

		return trans;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\d+),(\\d+),(\\d+),(\\d*\\.?\\d*)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				int r = Integer.parseInt(matcher.group(1));
				int g = Integer.parseInt(matcher.group(2));
				int b = Integer.parseInt(matcher.group(3));
				double fuzz = Double.parseDouble(matcher.group(4));
				Color color = new Color(r, g, b);
				return new TransparentColorTransformerDelegate(color, fuzz);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + color.getRed() + "," + color.getGreen() + ","
				+ color.getBlue() + "," + fuzz + ")";
	}
}
