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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileFactoryDelegate;

import com.jogamp.opengl.util.texture.Texture;

/**
 * Extension of the {@link TextureTile} class which uses a
 * {@link ITileFactoryDelegate} when creating sub tiles.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DelegatorTextureTile extends TextureTile implements IDelegatorTile
{
	protected final ITileFactoryDelegate<DelegatorTextureTile, Sector, Level> delegate;
	protected TileKey transformedTileKey;

	public DelegatorTextureTile(Sector sector, Level level, int row, int col,
			ITileFactoryDelegate<DelegatorTextureTile, Sector, Level> delegate)
	{
		super(sector, level, row, col);
		this.delegate = delegate;
	}

	@Override
	public String getService()
	{
		return getLevel().getService();
	}

	@Override
	public String getDataset()
	{
		return getLevel().getDataset();
	}

	/**
	 * Utility function to null the texture data, ignoring the thrown
	 * {@link NullPointerException}.
	 */
	private void nullTextureData()
	{
		try
		{
			setTextureData(null);
		}
		catch (NullPointerException e)
		{
			//ignore
		}
	}

	/**
	 * Get this tile's {@link TileKey}, transformed by the
	 * {@link ITileFactoryDelegate}.
	 * 
	 * @return {@link TileKey} transformed by {@link ITileFactoryDelegate}
	 */
	@Override
	public TileKey getTransformedTileKey()
	{
		if (transformedTileKey == null)
		{
			transformedTileKey = delegate.transformTileKey(getTileKey());
		}
		return transformedTileKey;
	}

	/* ************************************************************************************
	 * Below here is copied from TextureTile, with some modifications to use the delegate *
	 ************************************************************************************ */

	private long updateTime = 0;

	@Override
	public boolean isTextureExpired(long expiryTime)
	{
		return this.updateTime > 0 && this.updateTime < expiryTime;
	}

	@Override
	public TextureTile[] createSubTiles(Level nextLevel)
	{
		if (nextLevel == null)
		{
			String msg = Logging.getMessage("nullValue.LevelIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		Angle p0 = this.getSector().getMinLatitude();
		Angle p2 = this.getSector().getMaxLatitude();
		Angle p1 = Angle.midAngle(p0, p2);

		Angle t0 = this.getSector().getMinLongitude();
		Angle t2 = this.getSector().getMaxLongitude();
		Angle t1 = Angle.midAngle(t0, t2);

		String nextLevelCacheName = nextLevel.getCacheName();
		int nextLevelNum = nextLevel.getLevelNumber();
		int row = this.getRow();
		int col = this.getColumn();

		TextureTile[] subTiles = new TextureTile[4];

		TileKey key = new TileKey(nextLevelNum, 2 * row, 2 * col, nextLevelCacheName);
		TextureTile subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[0] = subTile;
		else
			subTiles[0] = delegate.createTextureTile(new Sector(p0, p1, t0, t1), nextLevel, 2 * row, 2 * col);
		//subTiles[0] = new TextureTile(new Sector(p0, p1, t0, t1), nextLevel, 2 * row, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[1] = subTile;
		else
			subTiles[1] = delegate.createTextureTile(new Sector(p0, p1, t1, t2), nextLevel, 2 * row, 2 * col + 1);
		//subTiles[1] = new TextureTile(new Sector(p0, p1, t1, t2), nextLevel, 2 * row, 2 * col + 1);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[2] = subTile;
		else
			subTiles[2] = delegate.createTextureTile(new Sector(p1, p2, t0, t1), nextLevel, 2 * row + 1, 2 * col);
		//subTiles[2] = new TextureTile(new Sector(p1, p2, t0, t1), nextLevel, 2 * row + 1, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[3] = subTile;
		else
			subTiles[3] = delegate.createTextureTile(new Sector(p1, p2, t1, t2), nextLevel, 2 * row + 1, 2 * col + 1);
		//subTiles[3] = new TextureTile(new Sector(p1, p2, t1, t2), nextLevel, 2 * row + 1, 2 * col + 1);

		return subTiles;
	}

	@Override
	public Texture getTexture(GpuResourceCache tc)
	{
		if (tc == null)
		{
			String message = Logging.getMessage("nullValue.TextureCacheIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		return tc.getTexture(getTransformedTileKey());
	}

	@Override
	public void setTexture(GpuResourceCache tc, Texture texture)
	{
		if (tc == null)
		{
			String message = Logging.getMessage("nullValue.TextureCacheIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		tc.put(getTransformedTileKey(), texture);
		this.updateTime = System.currentTimeMillis();

		// No more need for texture data; allow garbage collector and memory cache to reclaim it.
		// This also signals that new texture data has been converted.
		//this.textureData = null;
		nullTextureData();
		this.updateMemoryCache();
	}

	@Override
	protected TextureTile getTileFromMemoryCache(TileKey tileKey)
	{
		tileKey = delegate.transformTileKey(tileKey);
		return (TextureTile) getMemoryCache().getObject(tileKey);
	}

	@Override
	protected void updateMemoryCache()
	{
		if (getMemoryCache().getObject(getTransformedTileKey()) != null)
			getMemoryCache().add(getTransformedTileKey(), this);
	}
}
