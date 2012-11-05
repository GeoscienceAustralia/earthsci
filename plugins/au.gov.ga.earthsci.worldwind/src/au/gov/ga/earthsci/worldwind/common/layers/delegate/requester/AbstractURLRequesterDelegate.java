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
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorLayer;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileRequesterDelegate;

/**
 * Implementation of {@link ITileRequesterDelegate} which performs the same
 * texture tile requests as {@link BasicTiledImageLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractURLRequesterDelegate<TILE extends IDelegatorTile> implements ITileRequesterDelegate<TILE>
{
	protected final static String DEFINITION_STRING = "URLRequester";
	
	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
	
	@Override
	public void forceTextureLoad(TILE tile, IDelegatorLayer<TILE> layer)
	{
		final URL textureURL = getLocalTileURL(tile, layer, true);

		if (textureURL != null && !layer.isTextureFileExpired(tile, textureURL, layer.getDataFileStore()))
		{
			loadTexture(tile, textureURL, layer);
		}
	}

	@Override
	public Runnable createRequestTask(TILE tile, IDelegatorLayer<TILE> layer)
	{
		return new RequestTask<TILE>(tile, layer, this);
	}

	@Override
	public URL getLocalTileURL(TILE tile, IDelegatorLayer<TILE> layer, boolean searchClassPath)
	{
		return layer.getDataFileStore().findFile(tile.getPath(), searchClassPath);
	}

	protected boolean loadTexture(TILE tile, URL textureURL, IDelegatorLayer<TILE> layer)
	{
		return layer.loadTexture(tile, textureURL);
	}

	/* **********************************************************************************************
	 * Below here is copied from BasicTiledImageLayer, with some modifications to use the delegates *
	 ********************************************************************************************** */

	private static class RequestTask<TILE extends IDelegatorTile> implements Runnable, Comparable<RequestTask<TILE>>
	{
		private final TILE tile;
		private final IDelegatorLayer<TILE> layer;
		private final AbstractURLRequesterDelegate<TILE> delegate;

		private RequestTask(TILE tile, IDelegatorLayer<TILE> layer, AbstractURLRequesterDelegate<TILE> delegate)
		{
			this.layer = layer;
			this.tile = tile;
			this.delegate = delegate;
		}

		@Override
		public void run()
		{
			// TODO: check to ensure load is still needed

			final java.net.URL textureURL = delegate.getLocalTileURL(tile, layer, false);
			if (textureURL != null && !this.layer.isTextureFileExpired(tile, textureURL, this.layer.getDataFileStore()))
			{
				if (delegate.loadTexture(tile, textureURL, layer))
				{
					layer.unmarkResourceAbsent(this.tile);
					this.layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
				else
				{
					// Assume that something's wrong with the file and delete it.
					this.layer.getDataFileStore().removeFile(textureURL);
					String message = Logging.getMessage("generic.DeletedCorruptDataFile", textureURL);
					Logging.logger().info(message);
				}
			}

			this.layer.retrieveRemoteTexture(this.tile, null);
		}

		/**
		 * @param that
		 *            the task to compare
		 * 
		 * @return -1 if <code>this</code> less than <code>that</code>, 1 if
		 *         greater than, 0 if equal
		 * 
		 * @throws IllegalArgumentException
		 *             if <code>that</code> is null
		 */
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
			{
				return true;
			}
			if (o == null || getClass() != o.getClass())
			{
				return false;
			}

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
