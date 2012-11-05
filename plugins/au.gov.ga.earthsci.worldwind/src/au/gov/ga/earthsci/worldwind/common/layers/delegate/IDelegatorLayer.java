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

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.util.TileKey;

import java.net.URL;

import com.jogamp.opengl.util.texture.TextureData;

/**
 * A layer that uses {@link IDelegate} to do it's work.
 * 
 * @param <TILE>
 *            The tile type for this layer.
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDelegatorLayer<TILE extends IDelegatorTile> extends AVList
{
	/**
	 * Download data for tile, using the provided postProcessor.
	 */
	void retrieveRemoteTexture(TILE tile, RetrievalPostProcessor postProcessor); //BasicTiledImageLayer

	/**
	 * @return The {@link FileStore} for this layer.
	 */
	FileStore getDataFileStore(); //AbstractLayer

	/**
	 * Mark the provided tile as an absent resource.
	 */
	void markResourceAbsent(TILE tile); //LevelSet

	/**
	 * Un-mark the provided tile as an absent resource.
	 */
	void unmarkResourceAbsent(TILE tile); //LevelSet

	/**
	 * @return Has the provided texture file expired?
	 */
	boolean isTextureFileExpired(TILE tile, URL textureURL, FileStore fileStore); //BasicTiledImageLayer

	/**
	 * Load a texture from a URL (must be file protocol) and set the tile's
	 * texture data to the loaded texture. Should be called by the
	 * {@link ITileRequesterDelegate}.
	 * 
	 * @param tile
	 *            Tile to set texture data
	 * @param textureURL
	 *            File URL of the texture
	 * @return true if the texture data was loaded successfully
	 */
	boolean loadTexture(TILE tile, URL textureURL);

	/**
	 * Read image from a File URL and return it as {@link TextureData}. Called
	 * by {@link IDelegatorLayer#loadTexture(IDelegatorTile, URL)}.
	 * 
	 * @param tile
	 *            Tile for which to read a texture
	 * @param url
	 *            File URL to read from
	 * @return TextureData read from url
	 */
	TextureData readTexture(TILE tile, URL url);

	/**
	 * Add tile to the MemoryCache. Should use the
	 * {@link IDelegatorTile#getTransformedTileKey()} for the {@link TileKey}.
	 * 
	 * @param tile
	 *            Tile to cache
	 */
	void addTileToCache(IDelegatorTile tile);

	/**
	 * @return This layer's URL context.
	 */
	URL getContext();

	/**
	 * @return Default image format for this layer.
	 */
	String getDefaultImageFormat(); //TiledImageLayer
}
