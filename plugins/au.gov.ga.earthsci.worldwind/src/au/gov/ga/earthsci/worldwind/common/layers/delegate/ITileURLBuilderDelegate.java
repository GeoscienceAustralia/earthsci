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

import gov.nasa.worldwind.util.Tile;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Instances of the {@link ITileURLBuilderDelegate} are used to customize the
 * URL used to download tiles from a remote server.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ITileURLBuilderDelegate extends IDelegate
{
	/**
	 * Calculate the URL used to download the given tile from the remote server.
	 * Usually this is simply forwarded to the
	 * {@link Tile#getResourceURL(String)} method.
	 * 
	 * @param tile
	 *            Tile to calculate the remote resource url for
	 * @param imageFormat
	 *            a string identifying the mime type of the desired image format
	 * @return URL used to download the given tile
	 * @throws MalformedURLException
	 */
	URL getRemoteTileURL(IDelegatorTile tile, String imageFormat) throws MalformedURLException;
}
