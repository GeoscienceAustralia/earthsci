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
package au.gov.ga.earthsci.worldwind.common.layers.curtain;

import gov.nasa.worldwind.util.TileKey;

/**
 * Class used for uniquely identifying a {@link CurtainTile}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainTileKey extends TileKey
{
	public CurtainTileKey(int level, int row, int col, String cacheName)
	{
		super(level, row, col, cacheName);
	}

	public CurtainTileKey(CurtainTile tile)
	{
		this(tile.getLevel().getLevelNumber(), tile.getRow(), tile.getColumn(), tile.getLevel().getCacheName());
	}
}
