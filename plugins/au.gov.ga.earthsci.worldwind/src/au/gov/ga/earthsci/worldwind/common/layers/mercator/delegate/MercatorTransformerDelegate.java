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
package au.gov.ga.earthsci.worldwind.common.layers.mercator.delegate;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.mercator.MercatorSector;

import java.awt.image.BufferedImage;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IImageTransformerDelegate;

/**
 * {@link IImageTransformerDelegate} that transforms an image in the mercator
 * projection into a geodetic projection.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MercatorTransformerDelegate implements IImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "MercatorTransformer";

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (DEFINITION_STRING.equalsIgnoreCase(definition))
		{
			return new MercatorTransformerDelegate();
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image, IDelegatorTile tile)
	{
		MercatorSector sector;
		if (tile instanceof DelegatorMercatorTextureTile)
		{
			sector = ((DelegatorMercatorTextureTile) tile).getMercatorSector();
		}
		else
		{
			return image;
		}

		int type = image.getType();
		if (type == 0)
			type = BufferedImage.TYPE_INT_RGB;
		BufferedImage trans = new BufferedImage(image.getWidth(), image.getHeight(), type);
		double miny = sector.getMinLatPercent();
		double maxy = sector.getMaxLatPercent();
		for (int y = 0; y < image.getHeight(); y++)
		{
			double sy = 1.0 - y / (double) (image.getHeight() - 1);
			Angle lat = Angle.fromRadians(sy * sector.getDeltaLatRadians() + sector.getMinLatitude().radians);
			double dy = 1.0 - (MercatorSector.gudermannianInverse(lat) - miny) / (maxy - miny);
			dy = Math.max(0.0, Math.min(1.0, dy));
			int iy = (int) (dy * (image.getHeight() - 1));

			for (int x = 0; x < image.getWidth(); x++)
			{
				trans.setRGB(x, y, image.getRGB(x, iy));
			}
		}
		return trans;
	}
}
