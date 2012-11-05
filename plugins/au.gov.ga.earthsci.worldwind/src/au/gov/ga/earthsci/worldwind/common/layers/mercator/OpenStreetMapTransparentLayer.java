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
package au.gov.ga.earthsci.worldwind.common.layers.mercator;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Mercator tiled image layer that displays maps from the OpenStreetMap.
 * Attempts to convert any common fill colors to real transparency.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OpenStreetMapTransparentLayer extends BasicMercatorTiledImageLayer
{
	public OpenStreetMapTransparentLayer()
	{
		super(makeLevels());
		this.setUseTransparentTextures(true);
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME, "OpenStreetMap Mapnik Transparent");
		params.setValue(AVKey.SERVICE, "http://a.tile.openstreetmap.org/");
		params.setValue(AVKey.DATASET_NAME, "mapnik");
		params.setValue(AVKey.FORMAT_SUFFIX, ".png");
		params.setValue(AVKey.NUM_LEVELS, 16);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)));
		params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
		params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

		return new LevelSet(params);
	}

	private static class URLBuilder implements TileUrlBuilder
	{
		@Override
		public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
		{
			int level = tile.getLevelNumber() + 3;
			int column = tile.getColumn();
			int row = (1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow();
			return new URL(tile.getLevel().getService() + level + "/" + column + "/" + row + ".png");
		}
	}

	@Override
	protected BufferedImage modifyImage(BufferedImage image)
	{
		BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				newImage.setRGB(x, y, convertTransparent(image.getRGB(x, y)));
		return newImage;
	}

	private int convertTransparent(int rgb)
	{
		int a = 255;
		//transparent colours
		if (rgb == -921880 || rgb == -4861744 || rgb == -856344 || rgb == -856087)
			a = 0;
		return (rgb & 0xffffff) | (a << 24);
	}

	@Override
	public String toString()
	{
		return "OpenStreetMap Mapnik";
	}
}
