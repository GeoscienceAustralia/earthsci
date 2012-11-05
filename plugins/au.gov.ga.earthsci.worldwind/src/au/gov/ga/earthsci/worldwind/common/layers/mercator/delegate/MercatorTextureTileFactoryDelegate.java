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
package au.gov.ga.earthsci.worldwind.common.layers.mercator.delegate;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.TileKey;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileFactoryDelegate;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Implementation of {@link ITileFactoryDelegate} which creates
 * {@link MercatorTextureTile}s. Also transforms tile's {@link TileKey} by
 * adding a random string to the data cache name. This means that layers with
 * the same data cache name will be cached separately in memory.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MercatorTextureTileFactoryDelegate implements IMercatorImageTileFactoryDelegate
{
	private final static String DEFINITION_STRING = "MercatorTextureTile";

	public static TileKey appendToTileKeyCacheName(TileKey tileKey, String cacheName)
	{
		return new TileKey(tileKey.getLevelNumber(), tileKey.getRow(), tileKey.getColumn(), tileKey.getCacheName()
				+ cacheName);
	}

	private final String random = "_" + Util.randomString(8);

	@Override
	public DelegatorMercatorTextureTile createTextureTile(MercatorSector sector, Level level, int row, int col)
	{
		return new DelegatorMercatorTextureTile(sector, level, row, col, this);
	}

	@Override
	public TileKey transformTileKey(TileKey tileKey)
	{
		return appendToTileKeyCacheName(tileKey, random);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new MercatorTextureTileFactoryDelegate();
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
