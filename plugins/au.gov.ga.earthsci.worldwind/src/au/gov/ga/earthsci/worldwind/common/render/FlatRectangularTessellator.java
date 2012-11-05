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
package au.gov.ga.earthsci.worldwind.common.render;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.RectangularTessellator;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import au.gov.ga.earthsci.worldwind.common.terrain.WireframeRectangularTessellator;

/**
 * Custom {@link RectangularTessellator} that generates flat sector geometry (by
 * forcing vertical exaggeration to 0).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FlatRectangularTessellator extends WireframeRectangularTessellator
{
	@Override
	public SectorGeometryList tessellate(DrawContext dc)
	{
		Integer oldMaxLevel = Configuration.getIntegerValue(AVKey.RECTANGULAR_TESSELLATOR_MAX_LEVEL);
		try
		{
			Configuration.setValue(AVKey.RECTANGULAR_TESSELLATOR_MAX_LEVEL, 4);
			DrawContextDelegateVerticalExaggerationOverride odc =
					new DrawContextDelegateVerticalExaggerationOverride(dc);
			odc.overrideVerticalExaggeration(0);
			return super.tessellate(odc);
		}
		finally
		{
			Configuration.setValue(AVKey.RECTANGULAR_TESSELLATOR_MAX_LEVEL, oldMaxLevel);
		}
	}

	@Override
	protected CacheKey createCacheKey(DrawContext dc, RectTile tile)
	{
		//dodgy way to make key different from super-class': make the density negative
		return new CacheKey(dc, tile.getSector(), -tile.getDensity());
	}
}
