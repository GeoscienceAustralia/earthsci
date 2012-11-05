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

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.util.TileKey;

import java.net.URL;

/**
 * Represents a single tile for the {@link TiledCurtainLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainTile implements Cacheable
{
	private final CurtainLevel level;
	private final Segment segment;
	private final int row;
	private final int column;
	private final TileKey tileKey;
	private double priority = Double.MAX_VALUE; // Default is minimum priority
	// The following is late bound because it's only selectively needed and costly to create
	private String path;

	public CurtainTile(Segment segment, CurtainLevel level, int row, int column)
	{
		this.segment = segment;
		this.level = level;
		this.row = row;
		this.column = column;
		this.tileKey = new CurtainTileKey(this);
	}

	@Override
	public long getSizeInBytes()
	{
		// Return just an approximate size
		long size = 0;

		if (this.segment != null)
			size += this.segment.getSizeInBytes();

		if (this.path != null)
			size += this.path.length();

		size += 32; // to account for the references and the TileKey size

		return size;
	}

	public CurtainLevel getLevel()
	{
		return level;
	}

	public Segment getSegment()
	{
		return segment;
	}

	public int getRow()
	{
		return row;
	}

	public int getColumn()
	{
		return column;
	}

	public TileKey getTileKey()
	{
		return tileKey;
	}

	public int getLevelNumber()
	{
		return level.getLevelNumber();
	}

	public String getPath()
	{
		if (this.path == null)
		{
			this.path = this.level.getPath() + "/" + this.row + "/" + this.row + "_" + this.column;
			if (!this.level.isEmpty())
				path += this.level.getFormatSuffix();
		}

		return this.path;
	}

	public String getPathBase()
	{
		String path = this.getPath();

		return path.contains(".") ? path.substring(0, path.lastIndexOf(".")) : path;
	}

	public String getLabel()
	{
		return row + "," + column + "@" + level.getLevelNumber();
	}

	public URL getResourceURL() throws java.net.MalformedURLException
	{
		return level != null ? level.getTileResourceURL(this, null) : null;
	}

	public URL getResourceURL(String imageFormat) throws java.net.MalformedURLException
	{
		return level != null ? level.getTileResourceURL(this, imageFormat) : null;
	}

	public double getPriority()
	{
		return priority;
	}

	public void setPriority(double priority)
	{
		this.priority = priority;
	}

	@Override
	public boolean equals(Object o)
	{
		// Equality based only on the tile key
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final CurtainTile tile = (CurtainTile) o;

		return !(tileKey != null ? !tileKey.equals(tile.tileKey) : tile.tileKey != null);
	}

	@Override
	public int hashCode()
	{
		return (tileKey != null ? tileKey.hashCode() : 0);
	}

	@Override
	public String toString()
	{
		return this.getPath();
	}
}
