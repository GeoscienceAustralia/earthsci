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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.nearestneighbor;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Level;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileFactoryDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.DelegatorTextureTile;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.TextureTileFactoryDelegate;

/**
 * Implementation of {@link ITileFactoryDelegate} which creates
 * {@link NearestNeighborTextureTile}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NearestNeighborTextureTileFactoryDelegate extends TextureTileFactoryDelegate
{
	private final static String DEFINITION_STRING = "NearestNeighborTile";

	@Override
	public DelegatorTextureTile createTextureTile(Sector sector, Level level, int row, int col)
	{
		return new NearestNeighborTextureTile(sector, level, row, col, this);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
		{
			return new NearestNeighborTextureTileFactoryDelegate();
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
