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
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.AbsentResourceList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Represents a single LOD level for the {@link TiledCurtainLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainLevel extends AVListImpl implements Comparable<CurtainLevel>
{
	private final AVList params;
	private final int levelNumber;
	private final String levelName; // null or empty level name signifies no data resources associated with this level
	private final int levelWidth;
	private final int levelHeight;
	private final int tileWidth;
	private final int tileHeight;
	private final String cacheName;
	private final String service;
	private final String dataset;
	private final String formatSuffix;
	private final double texelSize;
	private final String path;
	private final CurtainTileUrlBuilder urlBuilder;
	private long expiryTime;
	private boolean active = true;

	private final int columnCount;
	private final int rowCount;

	// Absent tiles: A tile is deemed absent if a specified maximum number of attempts have been made to retrieve it.
	// Retrieval attempts are governed by a minimum time interval between successive attempts. If an attempt is made
	// within this interval, the tile is still deemed to be absent until the interval expires.
	private final AbsentResourceList absentTiles;
	int DEFAULT_MAX_ABSENT_TILE_ATTEMPTS = 2;
	int DEFAULT_MIN_ABSENT_TILE_CHECK_INTERVAL = 10000; // milliseconds

	public CurtainLevel(AVList params)
	{
		if (params == null)
		{
			String message = Logging.getMessage("nullValue.LevelConfigParams");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.params = params.copy(); // Private copy to insulate from subsequent changes by the app
		String message = this.validate(params);
		if (message != null)
		{
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		String ln = this.params.getStringValue(AVKey.LEVEL_NAME);
		this.levelName = ln != null ? ln : "";

		this.levelNumber = (Integer) this.params.getValue(AVKey.LEVEL_NUMBER);
		this.levelWidth = (Integer) this.params.getValue(AVKeyMore.LEVEL_WIDTH);
		this.levelHeight = (Integer) this.params.getValue(AVKeyMore.LEVEL_HEIGHT);
		int tileWidth = (Integer) this.params.getValue(AVKey.TILE_WIDTH);
		int tileHeight = (Integer) this.params.getValue(AVKey.TILE_HEIGHT);
		this.cacheName = this.params.getStringValue(AVKey.DATA_CACHE_NAME);
		this.service = this.params.getStringValue(AVKey.SERVICE);
		this.dataset = this.params.getStringValue(AVKey.DATASET_NAME);
		this.formatSuffix = this.params.getStringValue(AVKey.FORMAT_SUFFIX);
		this.urlBuilder = (CurtainTileUrlBuilder) this.params.getValue(AVKey.TILE_URL_BUILDER);
		this.expiryTime = AVListImpl.getLongValue(params, AVKey.EXPIRY_TIME, 0L);

		Angle curtainLength = ((Path) this.params.getValue(AVKeyMore.PATH)).getLength();
		//Angle curtainLength = (Angle) this.params.getValue(AVKeyMore.CURTAIN_LENGTH);
		this.texelSize = curtainLength.radians / this.levelWidth;

		//work out this level's tile width/height
		int widthsPerTile = Util.previousPowerOfTwo(tileWidth / this.levelWidth);
		int heightsPerTile = Util.previousPowerOfTwo(tileHeight / this.levelHeight);
		if (widthsPerTile > 1)
		{
			tileWidth /= widthsPerTile;
			tileHeight *= widthsPerTile;
		}
		if (heightsPerTile > 1)
		{
			tileWidth *= heightsPerTile;
			tileHeight /= heightsPerTile;
		}

		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.columnCount = (levelWidth - 1) / tileWidth + 1;
		this.rowCount = (levelHeight - 1) / tileHeight + 1;

		this.path = this.cacheName + "/" + this.levelName;

		Integer maxAbsentTileAttempts = (Integer) this.params.getValue(AVKey.MAX_ABSENT_TILE_ATTEMPTS);
		if (maxAbsentTileAttempts == null)
			maxAbsentTileAttempts = DEFAULT_MAX_ABSENT_TILE_ATTEMPTS;

		Integer minAbsentTileCheckInterval = (Integer) this.params.getValue(AVKey.MIN_ABSENT_TILE_CHECK_INTERVAL);
		if (minAbsentTileCheckInterval == null)
			minAbsentTileCheckInterval = DEFAULT_MIN_ABSENT_TILE_CHECK_INTERVAL;

		this.absentTiles = new AbsentResourceList(maxAbsentTileAttempts, minAbsentTileCheckInterval);
	}

	private String validate(AVList params)
	{
		StringBuffer sb = new StringBuffer();

		Object o = params.getValue(AVKey.LEVEL_NUMBER);
		if (o == null || !(o instanceof Integer) || ((Integer) o) < 0)
			sb.append(Logging.getMessage("term.levelNumber")).append(" ");

		o = params.getValue(AVKey.LEVEL_NAME);
		if (o == null || !(o instanceof String))
			sb.append(Logging.getMessage("term.levelName")).append(" ");

		o = params.getValue(AVKeyMore.LEVEL_WIDTH);
		if (o == null || !(o instanceof Integer) || ((Integer) o) < 0)
			sb.append(getMessage(getTermLevelWidthKey())).append(" ");

		o = params.getValue(AVKeyMore.LEVEL_HEIGHT);
		if (o == null || !(o instanceof Integer) || ((Integer) o) < 0)
			sb.append(getMessage(getTermLevelHeightKey())).append(" ");

		o = params.getValue(AVKey.TILE_WIDTH);
		if (o == null || !(o instanceof Integer) || ((Integer) o) < 0)
			sb.append(Logging.getMessage("term.tileWidth")).append(" ");

		o = params.getValue(AVKey.TILE_HEIGHT);
		if (o == null || !(o instanceof Integer) || ((Integer) o) < 0)
			sb.append(Logging.getMessage("term.tileHeight")).append(" ");

		o = params.getValue(AVKeyMore.PATH);
		if (o == null || !(o instanceof Path) || ((Path) o).getLength().radians <= 0)
			sb.append(getMessage(getTermPathKey())).append(" ");

		o = params.getValue(AVKey.DATA_CACHE_NAME);
		if (o == null || !(o instanceof String) || ((String) o).length() < 1)
			sb.append(Logging.getMessage("term.fileStoreFolder")).append(" ");

		o = params.getValue(AVKey.TILE_URL_BUILDER);
		if (o == null || !(o instanceof CurtainTileUrlBuilder))
			sb.append(Logging.getMessage("term.tileURLBuilder")).append(" ");

		o = params.getValue(AVKey.EXPIRY_TIME);
		if (o != null && (!(o instanceof Long) || ((Long) o) < 1))
			sb.append(Logging.getMessage("term.expiryTime")).append(" ");

		if (params.getStringValue(AVKey.LEVEL_NAME).length() > 0)
		{
			o = params.getValue(AVKey.DATASET_NAME);
			if (o == null || !(o instanceof String) || ((String) o).length() < 1)
				sb.append(Logging.getMessage("term.datasetName")).append(" ");

			o = params.getValue(AVKey.FORMAT_SUFFIX);
			if (o == null || !(o instanceof String) || ((String) o).length() < 1)
				sb.append(Logging.getMessage("term.formatSuffix")).append(" ");
		}

		if (sb.length() == 0)
			return null;

		return Logging.getMessage("layers.LevelSet.InvalidLevelDescriptorFields", sb.toString());
	}

	public final AVList getParams()
	{
		return params;
	}

	public String getPath()
	{
		return this.path;
	}

	public final int getLevelNumber()
	{
		return this.levelNumber;
	}

	public String getLevelName()
	{
		return this.levelName;
	}

	public final int getTileWidth()
	{
		return this.tileWidth;
	}

	public final int getTileHeight()
	{
		return this.tileHeight;
	}

	public final String getFormatSuffix()
	{
		return this.formatSuffix;
	}

	public final String getService()
	{
		return this.service;
	}

	public final String getDataset()
	{
		return this.dataset;
	}

	public final String getCacheName()
	{
		return this.cacheName;
	}

	public final double getTexelSize()
	{
		return this.texelSize;
	}

	public final boolean isEmpty()
	{
		return this.levelName == null || this.levelName.equals("") || !this.active;
	}

	public final void markResourceAbsent(long tileNumber)
	{
		if (tileNumber >= 0)
			this.absentTiles.markResourceAbsent(tileNumber);
	}

	public final boolean isResourceAbsent(long tileNumber)
	{
		return this.absentTiles.isResourceAbsent(tileNumber);
	}

	public final void unmarkResourceAbsent(long tileNumber)
	{
		if (tileNumber >= 0)
			this.absentTiles.unmarkResourceAbsent(tileNumber);
	}

	public final long getExpiryTime()
	{
		return this.expiryTime;
	}

	public final void setExpiryTime(long expTime)
	{
		this.expiryTime = expTime;
	}

	public final int getLevelWidth()
	{
		return levelWidth;
	}

	public final int getLevelHeight()
	{
		return levelHeight;
	}

	public final int getColumnCount()
	{
		return columnCount;
	}

	public final int getRowCount()
	{
		return rowCount;
	}

	public boolean isActive()
	{
		return this.active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public AbsentResourceList getAbsentTiles()
	{
		return absentTiles;
	}

	@Override
	public Object setValue(String key, Object value)
	{
		if (key != null && key.equals(AVKey.MAX_ABSENT_TILE_ATTEMPTS) && value instanceof Integer)
			this.absentTiles.setMaxTries((Integer) value);
		else if (key != null && key.equals(AVKey.MIN_ABSENT_TILE_CHECK_INTERVAL) && value instanceof Integer)
			this.absentTiles.setMinCheckInterval((Integer) value);

		return super.setValue(key, value);
	}

	@Override
	public Object getValue(String key)
	{
		if (key != null && key.equals(AVKey.MAX_ABSENT_TILE_ATTEMPTS))
			return this.absentTiles.getMaxTries();
		else if (key != null && key.equals(AVKey.MIN_ABSENT_TILE_CHECK_INTERVAL))
			return this.absentTiles.getMinCheckInterval();

		return super.getValue(key);
	}

	/**
	 * Returns the URL necessary to retrieve the specified tile.
	 * 
	 * @param tile
	 *            the tile who's resources will be retrieved.
	 * @param imageFormat
	 *            a string identifying the mime type of the desired image format
	 * 
	 * @return the resource URL.
	 * 
	 * @throws java.net.MalformedURLException
	 *             if the URL cannot be formed from the tile's parameters.
	 * @throws IllegalArgumentException
	 *             if <code>tile</code> is null.
	 */
	public java.net.URL getTileResourceURL(CurtainTile tile, String imageFormat) throws java.net.MalformedURLException
	{
		if (tile == null)
		{
			String msg = Logging.getMessage("nullValue.TileIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		return this.urlBuilder.getURL(tile, imageFormat);
	}

	/*public Segment computeSegmentForPosition(Angle latitude, Angle longitude, LatLon tileOrigin)
	{
		if (latitude == null || longitude == null)
		{
			String message = Logging.getMessage("nullValue.LatLonIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (tileOrigin == null)
		{
			String message = Logging.getMessage("nullValue.TileOriginIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Compute the tile's SW lat/lon based on its row/col in the level's data set.
		Angle dLat = this.getTileDelta().getLatitude();
		Angle dLon = this.getTileDelta().getLongitude();
		Angle latOrigin = tileOrigin.getLatitude();
		Angle lonOrigin = tileOrigin.getLongitude();

		int row = Tile.computeRow(dLat, latitude, latOrigin);
		int col = Tile.computeColumn(dLon, longitude, lonOrigin);
		Angle minLatitude = Tile.computeRowLatitude(row, dLat, latOrigin);
		Angle minLongitude = Tile.computeColumnLongitude(col, dLon, lonOrigin);

		return new Sector(minLatitude, minLatitude.add(dLat), minLongitude, minLongitude.add(dLon));
	}*/

	public Segment computeSegmentForKey(TileKey key)
	{
		if (key == null)
		{
			String msg = Logging.getMessage("nullValue.KeyIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		return computeSegmentForRowColumn(key.getRow(), key.getColumn());
	}

	public Segment computeSegmentForRowColumn(int row, int column)
	{
		double startX = computeStartPercentForColumn(column);
		double endX = computeStartPercentForColumn(column + 1);
		double startY = computeStartPercentForRow(row);
		double endY = computeStartPercentForRow(row + 1);

		return new Segment(startX, endX, startY, endY);
	}

	private double computeStartPercentForColumn(int column)
	{
		double x = column * tileWidth;
		return Util.clamp(x / levelWidth, 0, 1);
	}

	private double computeStartPercentForRow(int row)
	{
		double y = row * tileHeight;
		return Util.clamp(y / levelHeight, 0, 1);
	}

	@Override
	public int compareTo(CurtainLevel that)
	{
		if (that == null)
		{
			String msg = Logging.getMessage("nullValue.LevelIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		return this.levelNumber < that.levelNumber ? -1 : this.levelNumber == that.levelNumber ? 0 : 1;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final CurtainLevel level = (CurtainLevel) o;

		if (levelNumber != level.levelNumber)
			return false;
		if (tileHeight != level.tileHeight)
			return false;
		if (tileWidth != level.tileWidth)
			return false;
		if (levelHeight != level.levelHeight)
			return false;
		if (levelWidth != level.levelWidth)
			return false;
		if (cacheName != null ? !cacheName.equals(level.cacheName) : level.cacheName != null)
			return false;
		if (dataset != null ? !dataset.equals(level.dataset) : level.dataset != null)
			return false;
		if (formatSuffix != null ? !formatSuffix.equals(level.formatSuffix) : level.formatSuffix != null)
			return false;
		if (levelName != null ? !levelName.equals(level.levelName) : level.levelName != null)
			return false;
		if (service != null ? !service.equals(level.service) : level.service != null)
			return false;
		//noinspection RedundantIfStatement
		//		if (texelSize != level.texelSize)
		//			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result;
		result = levelNumber;
		result = 29 * result + (levelName != null ? levelName.hashCode() : 0);
		result = 29 * result + tileWidth;
		result = 29 * result + tileHeight;
		result = 29 * result + levelWidth;
		result = 29 * result + levelHeight;
		result = 29 * result + (formatSuffix != null ? formatSuffix.hashCode() : 0);
		result = 29 * result + (service != null ? service.hashCode() : 0);
		result = 29 * result + (dataset != null ? dataset.hashCode() : 0);
		result = 29 * result + (cacheName != null ? cacheName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return this.path;
	}
}
