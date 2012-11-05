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
package au.gov.ga.earthsci.worldwind.common.layers.curtain.delegate;

import gov.nasa.worldwind.util.TileKey;
import au.gov.ga.earthsci.worldwind.common.layers.curtain.CurtainLevel;
import au.gov.ga.earthsci.worldwind.common.layers.curtain.CurtainTextureTile;
import au.gov.ga.earthsci.worldwind.common.layers.curtain.Segment;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileFactoryDelegate;

/**
 * {@link IDelegatorTile} implementation which represents a tile for a curtain
 * layer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DelegatorCurtainTextureTile extends CurtainTextureTile implements IDelegatorTile
{
	protected final ITileFactoryDelegate<DelegatorCurtainTextureTile, Segment, CurtainLevel> delegate;
	protected TileKey transformedTileKey;

	public DelegatorCurtainTextureTile(Segment segment, CurtainLevel level, int row, int column,
			ITileFactoryDelegate<DelegatorCurtainTextureTile, Segment, CurtainLevel> delegate)
	{
		super(segment, level, row, column);
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

	@Override
	protected CurtainTextureTile createTextureTile(Segment segment, CurtainLevel level, int row, int column)
	{
		return delegate.createTextureTile(segment, level, row, column);
	}

	@Override
	public TileKey getTransformedTileKey()
	{
		if (transformedTileKey == null)
		{
			transformedTileKey = delegate.transformTileKey(super.getTileKey());
		}
		return transformedTileKey;
	}

	@Override
	public TileKey getTileKey()
	{
		//In this instance, we can override getTileKey(), because it is not final in CurtainTile.
		//This means we don't have to override the other texture handling functions, which was
		//required in the DelegatorTextureTile because Tile.getTileKey() is final.
		return getTransformedTileKey();
	}

	@Override
	protected CurtainTextureTile getTileFromMemoryCache(TileKey tileKey)
	{
		tileKey = delegate.transformTileKey(tileKey);
		return super.getTileFromMemoryCache(tileKey);
	}
}
