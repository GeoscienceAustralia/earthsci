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

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.util.TileKey;

import java.net.MalformedURLException;

import com.jogamp.opengl.util.texture.TextureData;

/**
 * An individual tile used by an {@link IDelegatorLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDelegatorTile extends Cacheable
{
	/**
	 * @return Download priority for this tile.
	 */
	double getPriority(); //Tile

	/**
	 * @return Local tile cache path.
	 */
	String getPath(); //Tile

	/**
	 * @param imageFormat
	 * @return Remote URL used to download this tile.
	 * @throws MalformedURLException
	 */
	java.net.URL getResourceURL(String imageFormat) throws MalformedURLException; //Tile

	/**
	 * @return Tile's level's service.
	 */
	String getService(); //Level

	/**
	 * @return Tile's level's dataset.
	 */
	String getDataset(); //Level

	/**
	 * @return This tile's level number.
	 */
	int getLevelNumber(); //Level

	/**
	 * @return This tile's row.
	 */
	int getRow(); //Level

	/**
	 * @return This tile's column.
	 */
	int getColumn(); //Level

	/**
	 * @return This tile's {@link TileKey}, transformed by the
	 *         {@link ITileFactoryDelegate}.
	 */
	TileKey getTransformedTileKey();

	/**
	 * Set the texture data for this tile.
	 */
	void setTextureData(TextureData textureData);
}
