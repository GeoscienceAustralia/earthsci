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

import java.net.URL;

/**
 * Instances of {@link ITileRequesterDelegate} are used to customise the
 * requesting of tile textures.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ITileRequesterDelegate<TILE extends IDelegatorTile> extends IDelegate
{
	/**
	 * Load a texture immediately if cached.
	 * 
	 * @param tile
	 *            Tile to load texture for
	 * @param layer
	 *            Tile's layer
	 */
	void forceTextureLoad(TILE tile, IDelegatorLayer<TILE> layer);

	/**
	 * Create a new Runnable which will make a request for tile's texture.
	 * 
	 * @param tile
	 *            Tile to request texture for
	 * @param layer
	 *            Tile's layer
	 * @return Runnable that will make the tile request
	 */
	Runnable createRequestTask(TILE tile, IDelegatorLayer<TILE> layer);

	/**
	 * Return the tile's file URL within the local filesystem (ie the path where
	 * the tile is stored in the cache; or - in the case of a local tileset -
	 * the file URL where the tile is stored).
	 * 
	 * @param tile
	 *            Tile to get file URL for
	 * @param layer
	 *            Tile's layer
	 * @param searchClassPath
	 *            Should the classpath be searched when finding the tile's file?
	 * @return tile's local file URL
	 */
	URL getLocalTileURL(TILE tile, IDelegatorLayer<TILE> layer, boolean searchClassPath);
}
