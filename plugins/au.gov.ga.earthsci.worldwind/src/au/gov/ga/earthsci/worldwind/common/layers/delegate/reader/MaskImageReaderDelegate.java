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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.reader;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.Globe;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileReaderDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.reader.MaskHelper.MaskInsideZipDelegate;

/**
 * Implementation of {@link ITileReaderDelegate} which supports reading an image
 * from a zip file. Supports image masks, which may be saved as a separate image
 * within the zip file. Also supports searching for image masks within a
 * directory relative to the input URL, which is useful for local file tilesets
 * with masks.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MaskImageReaderDelegate implements ITileReaderDelegate
{
	private final static String DEFINITION_STRING = "MaskReader";

	//dataset/level/row/tile.jpg
	//mask/level/row/tile.png
	private int upDirectoryCount = 3;

	public MaskImageReaderDelegate()
	{
	}

	public MaskImageReaderDelegate(int upDirectoryCount)
	{
		this.upDirectoryCount = upDirectoryCount;
	}

	@Override
	public BufferedImage readImage(IDelegatorTile tile, URL url, Globe globe) throws IOException
	{
		final boolean[] wasInsideZip = new boolean[1];
		final BufferedImage[] images = new BufferedImage[2];
		MaskInsideZipDelegate delegate = new MaskInsideZipDelegate()
		{
			@Override
			public void readEntry(ZipEntry entry, ZipInputStream zis) throws IOException
			{
				wasInsideZip[0] = true;
				BufferedImage bi = ImageIO.read(zis);
				String lower = entry.getName().toLowerCase();
				int index = lower.contains("mask") || bi.getColorModel().hasAlpha() ? 1 : 0;
				images[index] = bi;
			}
		};
		
		//perform the mask search:
		URL maskUrl = MaskHelper.getMaskURL(url, upDirectoryCount, delegate);

		BufferedImage image = images[0], mask = images[1];
		if (!wasInsideZip[0])
		{
			try
			{
				image = ImageIO.read(url);
				mask = ImageIO.read(maskUrl);
			}
			catch (Exception e)
			{
			}
		}
		
		//if either image and mask don't exist, at least return one of them
		if (image == null)
			return mask;
		if (mask == null)
			return image;
		//compose the image and mask together
		return MaskHelper.compose(image, mask);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new MaskImageReaderDelegate();
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
