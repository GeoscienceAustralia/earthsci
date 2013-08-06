package au.gov.ga.earthsci.model.geometry;


/**
 * A container for pre-computed and cached statistics about a model geometry.
 * <p/>
 * These may be populated at read time from metadata, or calculated as needed.
 * <p/>
 * These stats may be used to create bounding boxes, colour ramps, optimisations
 * etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ModelGeometryStatistics
{

	private Double minLat;
	private Double maxLat;

	private Double minLon;
	private Double maxLon;

	private Double minElevation;
	private Double maxElevation;

	private long numPoints;

	/**
	 * Create a new, empty statistics instance
	 */
	public ModelGeometryStatistics()
	{

	}

	/**
	 * Create a new statistics instance seeded with the given initial values
	 */
	public ModelGeometryStatistics(Double lat, Double lon, Double elevation)
	{
		updateStats(lat, lon, elevation);
	}

	/**
	 * Create a new, empty statistics instance
	 */
	public ModelGeometryStatistics(Double minLat, Double maxLat,
			Double minLon, Double maxLon,
			Double minElevation, Double maxElevation)
	{
		updateStats(minLat, minLon, minElevation);
		updateStats(maxLat, maxLon, maxElevation);
		numPoints = 0;
	}

	public Double getMinLat()
	{
		return minLat;
	}

	public Double getMaxLat()
	{
		return maxLat;
	}

	public Double getMinLon()
	{
		return minLon;
	}

	public Double getMaxLon()
	{
		return maxLon;
	}

	public Double getMinElevation()
	{
		return minElevation;
	}

	public Double getMaxElevation()
	{
		return maxElevation;
	}

	/**
	 * Update the statistics with the given latitude, longitude and elevation
	 * values
	 * <p/>
	 * The total number of points recorded will also be incremented.
	 */
	public void updateStats(Double lat, Double lon, Double elevation)
	{
		updateLatStats(lat);
		updateLonStats(lon);
		updateElevationStats(elevation);
		numPoints++;
	}

	/**
	 * Update the longitude statistics with the given value
	 */
	public void updateLonStats(Double val)
	{
		updateMinLon(val);
		updateMaxLon(val);
	}

	/**
	 * Update the {@link #minLon} value to the minimum of {@link #minLon} and
	 * the provided value.
	 */
	public void updateMinLon(Double val)
	{
		if (val == null)
		{
			return;
		}
		if (minLon == null)
		{
			minLon = val;
			return;
		}
		minLon = Math.min(minLon, val);
	}

	/**
	 * Update the {@link #maxLat} value to the maximum of {@link #maxLat} and
	 * the provided value.
	 */
	public void updateMaxLon(Double val)
	{
		if (val == null)
		{
			return;
		}
		if (maxLon == null)
		{
			maxLon = val;
			return;
		}
		maxLon = Math.max(maxLon, val);
	}

	/**
	 * Update the latitude statistics with the given value
	 */
	public void updateLatStats(Double val)
	{
		updateMinLat(val);
		updateMaxLat(val);
	}

	/**
	 * Update the {@link #minLat} value to the minimum of {@link #minLat} and
	 * the provided value.
	 */
	public void updateMinLat(Double val)
	{
		if (val == null)
		{
			return;
		}
		if (minLat == null)
		{
			minLat = val;
			return;
		}
		minLat = Math.min(minLat, val);
	}

	/**
	 * Update the {@link #maxLat} value to the maximum of {@link #maxLat} and
	 * the provided value.
	 */
	public void updateMaxLat(Double val)
	{
		if (val == null)
		{
			return;
		}
		if (maxLat == null)
		{
			maxLat = val;
			return;
		}
		maxLat = Math.max(maxLat, val);
	}

	/**
	 * Update the elevation statistics with the given value
	 */
	public void updateElevationStats(Double val)
	{
		updateMinElevation(val);
		updateMaxElevation(val);
	}

	/**
	 * Update the {@link #minElevation} value to the minimum of
	 * {@link #minElevation} and the provided value.
	 */
	public void updateMinElevation(Double val)
	{
		if (val == null)
		{
			return;
		}
		if (minElevation == null)
		{
			minElevation = val;
			return;
		}
		minElevation = Math.min(minElevation, val);
	}

	/**
	 * Update the {@link #maxElevation} value to the maximum of
	 * {@link #maxElevation} and the provided value.
	 */
	public void updateMaxElevation(Double val)
	{
		if (val == null)
		{
			return;
		}
		if (maxElevation == null)
		{
			maxElevation = val;
			return;
		}
		maxElevation = Math.max(maxElevation, val);
	}

	public long getNumPoints()
	{
		return numPoints;
	}

	public void setNumPoints(long numPoints)
	{
		this.numPoints = numPoints;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append("ModelGeometryStatistics[").append('\n');
		result.append("   Lat: [").append(minLat).append(", ").append(maxLat).append("]\n");
		result.append("   Lon: [").append(minLon).append(", ").append(maxLon).append("]\n");
		result.append("   Elevation: [").append(minElevation).append(", ").append(maxElevation).append("]\n");
		result.append("   Num points: " + numPoints).append('\n');
		result.append("]");
		return result.toString();
	}
}
