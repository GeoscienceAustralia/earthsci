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
package au.gov.ga.earthsci.worldwind.common.layers.delegate;

import gov.nasa.worldwind.globes.Globe;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Instances of {@link ITileReaderDelegate} are used when reading images from
 * file URLs (ie during texture load). This is useful if there is a need to read
 * an image from a custom file format (such as a zip file).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ITileReaderDelegate extends IDelegate
{
	/**
	 * Read an image from a url.
	 * 
	 * @param tile
	 *            Tile for which to read the image
	 * @param url
	 *            URL to read the image from
	 * @param globe
	 *            Current globe; can be used for vertex calculations if required
	 * @return Loaded image
	 * @throws IOException
	 *             If image reading fails
	 */
	<TILE extends IDelegatorTile> BufferedImage readImage(TILE tile, URL url, Globe globe) throws IOException;
}
