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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.requester;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorLayer;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileRequesterDelegate;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Implementation of {@link ITileRequesterDelegate} which provides loading from
 * tilesets stored in the local filesystem. This means that tiles are not
 * downloaded/cached, but are loaded directly from the tileset.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractLocalRequesterDelegate<TILE extends IDelegatorTile> implements
		ITileRequesterDelegate<TILE>
{
	protected final static String DEFINITION_STRING = "LocalRequester";
	
	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
	
	@Override
	public void forceTextureLoad(TILE tile, IDelegatorLayer<TILE> layer)
	{
		loadTexture(tile, layer);
	}

	@Override
	public Runnable createRequestTask(TILE tile, IDelegatorLayer<TILE> layer)
	{
		return new RequestTask<TILE>(tile, layer, this);
	}

	@Override
	public URL getLocalTileURL(TILE tile, IDelegatorLayer<TILE> layer, boolean searchClassPath)
	{
		return getTileURL(tile, layer);
	}

	/**
	 * Load the texture for a tile.
	 * 
	 * @param tile
	 *            Tile for which the texture should be loaded
	 * @param layer
	 *            Layer to call loadTexture() on
	 * @return true if the texture was loaded
	 */
	protected boolean loadTexture(TILE tile, IDelegatorLayer<TILE> layer)
	{
		URL url = getLocalTileURL(tile, layer, false);
		if (url == null)
			return false;
		return layer.loadTexture(tile, url);
	}

	/**
	 * Return a URL which points to the tile's texture.
	 * 
	 * @param tile
	 *            Tile to get texture URL for
	 * @param layer
	 *            Tile's layer
	 * @return Tile's texture URL
	 */
	protected URL getTileURL(TILE tile, IDelegatorLayer<TILE> layer)
	{
		return Util.getLocalTileURL(tile.getService(), tile.getDataset(), tile.getLevelNumber(), tile.getRow(),
				tile.getColumn(), layer.getContext(), layer.getDefaultImageFormat(), "jpg");
	}

	/**
	 * Task which simply calls loadTexture(), and then (un)marks the tile
	 * absent. Instances of this class are returned by the createRequestTask()
	 * function.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class RequestTask<TILE extends IDelegatorTile> implements Runnable, Comparable<RequestTask<TILE>>
	{
		private final IDelegatorLayer<TILE> layer;
		private final TILE tile;
		private final AbstractLocalRequesterDelegate<TILE> delegate;

		private RequestTask(TILE tile, IDelegatorLayer<TILE> layer, AbstractLocalRequesterDelegate<TILE> delegate)
		{
			this.layer = layer;
			this.tile = tile;
			this.delegate = delegate;
		}

		@Override
		public void run()
		{
			if (delegate.loadTexture(tile, layer))
			{
				layer.unmarkResourceAbsent(tile);
				layer.firePropertyChange(AVKey.LAYER, null, this);
			}
			else
			{
				layer.markResourceAbsent(tile);
			}
		}

		@Override
		public int compareTo(RequestTask<TILE> that)
		{
			if (that == null)
			{
				String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}
			return this.tile.getPriority() == that.tile.getPriority() ? 0 : this.tile.getPriority() < that.tile
					.getPriority() ? -1 : 1;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RequestTask<?> that = (RequestTask<?>) o;

			// Don't include layer in comparison so that requests are shared among layers
			return !(tile != null ? !tile.equals(that.tile) : that.tile != null);
		}

		@Override
		public int hashCode()
		{
			return (tile != null ? tile.hashCode() : 0);
		}

		@Override
		public String toString()
		{
			return this.tile.toString();
		}
	}
}
