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

import static au.gov.ga.earthsci.worldwind.common.util.message.CommonMessageConstants.*;
import static au.gov.ga.earthsci.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.transform.URLTransformer;

/**
 * Represents a set of LOD levels for the {@link TiledCurtainLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainLevelSet extends WWObjectImpl
{
	private final List<CurtainLevel> levels = new ArrayList<CurtainLevel>();

	public CurtainLevelSet(AVList params)
	{
		StringBuffer sb = new StringBuffer();

		int numLevels = 0;
		Object o = params.getValue(AVKey.NUM_LEVELS);
		if (o == null || !(o instanceof Integer) || (numLevels = (Integer) o) < 1)
			sb.append(Logging.getMessage("term.numLevels")).append(" ");

		int numEmptyLevels = 0;
		o = params.getValue(AVKey.NUM_EMPTY_LEVELS);
		if (o != null && o instanceof Integer && (Integer) o > 0)
			numEmptyLevels = (Integer) o;

		String[] inactiveLevels = null;
		o = params.getValue(AVKey.INACTIVE_LEVELS);
		if (o != null && !(o instanceof String))
			sb.append(Logging.getMessage("term.inactiveLevels")).append(" ");
		else if (o != null)
			inactiveLevels = ((String) o).split(",");

		o = params.getValue(AVKeyMore.FULL_WIDTH);
		if (o == null || !(o instanceof Integer))
			sb.append(getMessage(getTermFullWidthKey())).append(" ");

		o = params.getValue(AVKeyMore.FULL_HEIGHT);
		if (o == null || !(o instanceof Integer))
			sb.append(getMessage(getTermFullHeightKey())).append(" ");

		o = params.getValue(AVKeyMore.PATH);
		if (o == null || !(o instanceof Path))
			sb.append(getMessage(getTermPathKey())).append(" ");

		if (sb.length() > 0)
		{
			String message = Logging.getMessage("layers.LevelSet.InvalidLevelDescriptorFields", sb.toString());
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		int fullWidth = (Integer) params.getValue(AVKeyMore.FULL_WIDTH);
		int fullHeight = (Integer) params.getValue(AVKeyMore.FULL_HEIGHT);

		params = params.copy(); // copy so as not to modify the user's params

		CurtainTileUrlBuilder tub = (CurtainTileUrlBuilder) params.getValue(AVKey.TILE_URL_BUILDER);
		if (tub == null)
		{
			final String paramsImageFormat = (String) params.getValue(AVKey.IMAGE_FORMAT);

			params.setValue(AVKey.TILE_URL_BUILDER, new CurtainTileUrlBuilder()
			{
				@Override
				public URL getURL(CurtainTile tile, String imageFormat) throws MalformedURLException
				{
					String service = tile.getLevel().getService();
					if (service == null || service.length() < 1)
						return null;

					service = URLTransformer.transform(service);

					StringBuffer sb = new StringBuffer(service);
					if (sb.lastIndexOf("?") < 0)
						sb.append("?");
					else
						sb.append("&");

					sb.append("T=");
					sb.append(tile.getLevel().getDataset());
					sb.append("&L=");
					sb.append(tile.getLevel().getLevelName());
					sb.append("&X=");
					sb.append(tile.getColumn());
					sb.append("&Y=");
					sb.append(tile.getRow());

					String format = imageFormat != null ? imageFormat : paramsImageFormat;
					if (format != null)
						sb.append("&F=" + format);

					return new URL(sb.toString());
				}
			});
		}

		Dimension fullSize = new Dimension(fullWidth, fullHeight);
		Dimension[] levelSizes = new Dimension[numLevels];
		for (int i = numLevels - 1; i >= 0; i--)
		{
			levelSizes[i] = fullSize;
			fullSize = new Dimension((fullSize.width + 1) / 2, (fullSize.height + 1) / 2);
		}

		for (int i = 0; i < numLevels; i++)
		{
			params.setValue(AVKey.LEVEL_NAME, i < numEmptyLevels ? "" : Integer.toString(i - numEmptyLevels));
			params.setValue(AVKey.LEVEL_NUMBER, i);

			params.setValue(AVKeyMore.LEVEL_WIDTH, levelSizes[i].width);
			params.setValue(AVKeyMore.LEVEL_HEIGHT, levelSizes[i].height);

			this.levels.add(new CurtainLevel(params));
		}

		if (inactiveLevels != null)
		{
			for (String s : inactiveLevels)
			{
				int i = Integer.parseInt(s);
				this.getLevel(i).setActive(false);
			}
		}
	}

	public CurtainLevelSet(CurtainLevelSet source)
	{
		if (source == null)
		{
			String msg = Logging.getMessage("nullValue.LevelSetIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		for (CurtainLevel level : source.levels)
		{
			this.levels.add(level); // Levels are final, so it's safe to copy references.
		}
	}

	@Override
	public Object setValue(String key, Object value)
	{
		// Propogate the setting to all levels
		for (CurtainLevel level : this.levels)
		{
			level.setValue(key, value);
		}

		return super.setValue(key, value);
	}

	@Override
	public Object getValue(String key)
	{
		Object value = super.getValue(key);

		if (value != null)
			return value;

		// See if any level has it
		for (CurtainLevel level : this.getLevels())
		{
			if (level != null && (value = level.getValue(key)) != null)
				return value;
		}

		return null;
	}

	public final List<CurtainLevel> getLevels()
	{
		return this.levels;
	}

	public final CurtainLevel getLevel(int levelNumber)
	{
		return (levelNumber >= 0 && levelNumber < this.levels.size()) ? this.levels.get(levelNumber) : null;
	}

	public final int getNumLevels()
	{
		return this.levels.size();
	}

	public final CurtainLevel getFirstLevel()
	{
		return this.getLevel(0);
	}

	public final CurtainLevel getLastLevel()
	{
		return this.getLevel(this.getNumLevels() - 1);
	}

	public final CurtainLevel getNextToLastLevel()
	{
		return this.getLevel(this.getNumLevels() > 1 ? this.getNumLevels() - 2 : 0);
	}

	public final boolean isFinalLevel(int levelNum)
	{
		return levelNum == this.getNumLevels() - 1;
	}

	public final boolean isLevelEmpty(int levelNumber)
	{
		return this.levels.get(levelNumber).isEmpty();
	}

	private int numColumnsInLevel(CurtainLevel level)
	{
		return level.getColumnCount();
	}

	private long getTileNumber(CurtainTile tile)
	{
		return tile.getRow() < 0 ? -1 : tile.getRow() * this.numColumnsInLevel(tile.getLevel()) + tile.getColumn();
	}

	private long getTileNumber(TileKey tileKey)
	{
		return tileKey.getRow() < 0 ? -1 : tileKey.getRow()
				* this.numColumnsInLevel(this.getLevel(tileKey.getLevelNumber())) + tileKey.getColumn();
	}

	/**
	 * Instructs the level set that a tile is likely to be absent.
	 * 
	 * @param tile
	 *            The tile to mark as having an absent resource.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>tile</code> is null
	 */
	public final void markResourceAbsent(CurtainTile tile)
	{
		if (tile == null)
		{
			String msg = Logging.getMessage("nullValue.TileIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		tile.getLevel().markResourceAbsent(this.getTileNumber(tile));
	}

	/**
	 * Indicates whether a tile has been marked as absent.
	 * 
	 * @param tileKey
	 *            The key of the tile in question.
	 * 
	 * @return <code>true</code> if the tile is marked absent, otherwise
	 *         <code>false</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>tile</code> is null
	 */
	public final boolean isResourceAbsent(TileKey tileKey)
	{
		if (tileKey == null)
		{
			String msg = Logging.getMessage("nullValue.TileKeyIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		CurtainLevel level = this.getLevel(tileKey.getLevelNumber());
		return level.isEmpty() || level.isResourceAbsent(this.getTileNumber(tileKey));
	}

	/**
	 * Indicates whether a tile has been marked as absent.
	 * 
	 * @param tile
	 *            The tile in question.
	 * 
	 * @return <code>true</code> if the tile is marked absent, otherwise
	 *         <code>false</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>tile</code> is null
	 */
	public final boolean isResourceAbsent(CurtainTile tile)
	{
		if (tile == null)
		{
			String msg = Logging.getMessage("nullValue.TileIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		return tile.getLevel().isEmpty() || tile.getLevel().isResourceAbsent(this.getTileNumber(tile));
	}

	/**
	 * Removes the absent-tile mark associated with a tile, if one is
	 * associatied.
	 * 
	 * @param tile
	 *            The tile to unmark.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>tile</code> is null
	 */
	public final void unmarkResourceAbsent(CurtainTile tile)
	{
		if (tile == null)
		{
			String msg = Logging.getMessage("nullValue.TileIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		tile.getLevel().unmarkResourceAbsent(this.getTileNumber(tile));
	}

	// Create the Segment corresponding to a specified key.
	public Segment computeSegmentForKey(TileKey key)
	{
		if (key == null)
		{
			String msg = Logging.getMessage("nullValue.KeyIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		CurtainLevel level = this.getLevel(key.getLevelNumber());
		return level.computeSegmentForKey(key);
	}

	public void setExpiryTime(long expiryTime)
	{
		for (CurtainLevel level : this.levels)
		{
			level.setExpiryTime(expiryTime);
		}
	}
}
