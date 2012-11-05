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
package au.gov.ga.earthsci.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import java.util.List;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * {@link FastShape} subclass which defines top and bottom elevation offsets.
 * When calculating the {@link FastShape}s vertices, if a position is a
 * {@link TopBottomPosition}, it is offset by either the top or bottom elevation
 * offset. This class can also define a {@link LatLon} offset.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TopBottomFastShape extends FastShape
{
	protected double topElevationOffset = 0d;
	protected double bottomElevationOffset = 0d;
	protected LatLon latlonOffset = LatLon.ZERO;

	public TopBottomFastShape(List<Position> positions, int mode)
	{
		super(positions, mode);
	}

	public TopBottomFastShape(List<Position> positions, int[] indices, int mode)
	{
		super(positions, indices, mode);
	}

	/**
	 * @return The elevation offset by which top positions are offset.
	 */
	public double getTopElevationOffset()
	{
		return topElevationOffset;
	}

	/**
	 * Set the elevation offset by which top positions are offset.
	 * 
	 * @param topElevationOffset
	 */
	public void setTopElevationOffset(double topElevationOffset)
	{
		if (this.topElevationOffset != topElevationOffset)
		{
			verticesDirty = true;
			this.topElevationOffset = topElevationOffset;
		}
	}

	/**
	 * @return The elevation offset by which bottom positions are offset.
	 */
	public double getBottomElevationOffset()
	{
		return bottomElevationOffset;
	}

	/**
	 * Set the elevation offset by which bottom positions are offset.
	 * 
	 * @param bottomElevationOffset
	 */
	public void setBottomElevationOffset(double bottomElevationOffset)
	{
		if (this.bottomElevationOffset != bottomElevationOffset)
		{
			verticesDirty = true;
			this.bottomElevationOffset = bottomElevationOffset;
		}
	}

	/**
	 * @return The {@link LatLon} offset by which each position is offset.
	 */
	public LatLon getLatlonOffset()
	{
		return latlonOffset;
	}

	/**
	 * Set the {@link LatLon} offset by which each position is offset.
	 * 
	 * @param latlonOffset
	 */
	public void setLatlonOffset(LatLon latlonOffset)
	{
		if (!LatLon.equals(latlonOffset, this.latlonOffset))
		{
			verticesDirty = true;
			this.latlonOffset = latlonOffset;
		}
	}

	@Override
	protected double calculateElevationOffset(LatLon position)
	{
		double elevationOffset = super.calculateElevationOffset(position);
		if (position instanceof TopBottomPosition)
		{
			elevationOffset += ((TopBottomPosition) position).isBottom() ? bottomElevationOffset : topElevationOffset;
		}
		return elevationOffset;
	}

	@Override
	protected LatLon calculateLatLonOffset()
	{
		return latlonOffset;
	}
}
