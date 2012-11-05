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

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Tile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileReaderDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.reader.MaskHelper;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.reader.MaskHelper.MaskInsideZipDelegate;
import au.gov.ga.earthsci.worldwind.common.util.IOUtil;

/**
 * Abstract class that acts as a super class of all {@link ITileReaderDelegate}s
 * that generate an image from elevation tiles.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class ElevationImageReaderDelegate implements ITileReaderDelegate
{
	protected final static String doublePattern = "((?:-?\\d*\\.\\d*)|(?:-?\\d+))";

	protected final String pixelType;
	protected final String byteOrder;
	protected final double missingDataSignal;

	protected final boolean checkForMask = true;

	public ElevationImageReaderDelegate(String pixelType, String byteOrder, double missingDataSignal)
	{
		this.pixelType = pixelType;
		this.byteOrder = byteOrder;
		this.missingDataSignal = missingDataSignal;
	}

	@Override
	public BufferedImage readImage(IDelegatorTile tile, URL url, Globe globe) throws IOException
	{
		if (!(tile instanceof Tile))
		{
			throw new IllegalArgumentException("Tile must be a " + Tile.class.getName());
		}
		return readImage((Tile) tile, url, globe);
	}

	/**
	 * @see ITileReaderDelegate#readImage(IDelegatorTile, URL, Globe)
	 */
	public BufferedImage readImage(Tile tile, URL url, Globe globe) throws IOException
	{
		if (checkForMask)
		{
			final BufferedImage[] mask = new BufferedImage[1];
			final BufferWrapper[] byteBuffer = new BufferWrapper[1];
			MaskInsideZipDelegate delegate = new MaskInsideZipDelegate()
			{
				@Override
				public void readEntry(ZipEntry entry, ZipInputStream zis) throws IOException
				{
					String lower = entry.getName().toLowerCase();
					if (lower.endsWith(".png"))
					{
						mask[0] = ImageIO.read(zis);
					}
					else
					{
						byteBuffer[0] = IOUtil.readByteBuffer(zis, pixelType, byteOrder);
					}
				}
			};

			//perform the mask search:
			URL maskUrl = MaskHelper.getMaskURL(url, 3, delegate);
			if (mask[0] == null)
			{
				try
				{
					mask[0] = ImageIO.read(maskUrl);
				}
				catch (Exception e)
				{
				}
			}
			if (byteBuffer[0] == null)
			{
				byteBuffer[0] = IOUtil.readByteBuffer(url, pixelType, byteOrder);
			}

			BufferedImage image =
					generateImage(byteBuffer[0], tile.getWidth(), tile.getHeight(), globe, tile.getSector());
			if (mask[0] == null)
				return image;
			return MaskHelper.compose(image, mask[0]);
		}
		else
		{
			BufferWrapper byteBuffer = IOUtil.readByteBuffer(url, pixelType, byteOrder);
			return generateImage(byteBuffer, tile.getWidth(), tile.getHeight(), globe, tile.getSector());
		}
	}

	/**
	 * Generate an image from elevation data.
	 * 
	 * @param elevations
	 *            Wrapped elevation data
	 * @param width
	 *            Width of the data tile
	 * @param height
	 *            Height of the data tile
	 * @param globe
	 *            Current globe
	 * @param sector
	 *            Sector of the data tile
	 * @return Image generated from the elevation data
	 */
	protected abstract BufferedImage generateImage(BufferWrapper elevations, int width, int height, Globe globe,
			Sector sector);
}
