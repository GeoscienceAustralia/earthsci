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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IImageTransformerDelegate;

/**
 * Applies a resize transformation to each retrieved tile such that the resulting image dimensions match
 * those specified in the delegate definition. Where the target dimensions do not match those of the 
 * retrieved image tile, bilinear interpolation is used for resizing the tile.
 * <p/>
 * Useful for resizing tiles on-the-fly to the standard 512x512 size.
 * <p/>
 * <code>&lt;Delegate&gt;ResizeTransformer(w,h)&lt;/Delegate&gt;</code>
 * <ul>
 * 	<li>w = target image width (integer)
 *  <li>h = target image height (integer)
 * </ul> 
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ResizeTransformerDelegate implements IImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "ResizeTransformer";

	private final int width;
	private final int height;

	@SuppressWarnings("unused")
	private ResizeTransformerDelegate()
	{
		this(512, 512);
	}

	public ResizeTransformerDelegate(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image, IDelegatorTile tile)
	{
		if (image.getWidth() == width && image.getHeight() == height)
			return image;

		BufferedImage resized = new BufferedImage(width, height, image.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resized;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\d+),(\\d+)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				int width = Integer.parseInt(matcher.group(1));
				int height = Integer.parseInt(matcher.group(2));
				return new ResizeTransformerDelegate(width, height);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + width + "," + height + ")";
	}
}
