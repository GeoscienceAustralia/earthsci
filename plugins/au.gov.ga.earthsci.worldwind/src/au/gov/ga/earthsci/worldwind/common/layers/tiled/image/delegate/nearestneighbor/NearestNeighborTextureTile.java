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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Level;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileFactoryDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.DelegatorTextureTile;

import com.jogamp.opengl.util.texture.Texture;

/**
 * {@link TextureTile} which performs NearestNeighbor Magnification at when
 * viewing the layer's lowest level (highest resolution).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NearestNeighborTextureTile extends DelegatorTextureTile
{
	private int numLevels = 0;

	public NearestNeighborTextureTile(Sector sector, Level level, int row, int col,
			ITileFactoryDelegate<DelegatorTextureTile, Sector, Level> delegate)
	{
		super(sector, level, row, col, delegate);

		AVList params = level.getParams();
		Object o = params.getValue(AVKey.NUM_LEVELS);
		if (o != null && o instanceof Integer)
			numLevels = (Integer) o;
	}

	@Override
	protected void setTextureParameters(DrawContext dc, Texture t)
	{
		super.setTextureParameters(dc, t);

		//set the magnification filter to nearest neighbor if this tile's level is the layer's last level
		if ((numLevels > 0 && getLevelNumber() >= numLevels - 1) || dc.getCurrentLayer().isAtMaxResolution())
		{
			dc.getGL().glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		}
	}
}
