package au.gov.ga.earthsci.model.bounds;

import java.util.Arrays;
import java.util.Collection;

import au.gov.ga.earthsci.common.math.vector.Vector3;
import au.gov.ga.earthsci.common.util.Range;

/**
 * A simple immutable implementation of {@link IBoundingVolume} that uses
 * min/max values in 3 dimensions to represent an axis-aligned bounding box
 * volume.
 * <p/>
 * Boundary inclusion (whether a point at the boundary of the box is considered
 * within the volume) can be set at construction time.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BoundingBox implements IBoundingVolume
{

	private Range<Double> xRange;
	private Range<Double> yRange;
	private Range<Double> zRange;


	/**
	 * Create a new axis-aligned bounding box with the provided values.
	 * <p/>
	 * Boundary inclusion is applied.
	 */
	public BoundingBox(double minX, double maxX,
			double minY, double maxY,
			double minZ, double maxZ)
	{
		this(minX, maxX, minY, maxY, minZ, maxZ, true);
	}

	/**
	 * Create a new axis-aligned bounding box with the provided values.
	 * <p/>
	 * Boundary inclusion is applied as specified.
	 */
	public BoundingBox(double minX, double maxX,
			double minY, double maxY,
			double minZ, double maxZ,
			boolean includeBoundary)
	{
		xRange = new Range<Double>(minX, includeBoundary, maxX, includeBoundary);
		yRange = new Range<Double>(minY, includeBoundary, maxY, includeBoundary);
		zRange = new Range<Double>(minZ, includeBoundary, maxZ, includeBoundary);
	}

	@Override
	public boolean contains(Vector3 point)
	{
		if (point == null)
		{
			return false;
		}

		return xRange.contains(point.x) &&
				yRange.contains(point.y) &&
				zRange.contains(point.z);
	}

	@Override
	public boolean containsAll(Vector3... points)
	{
		if (points == null || points.length == 0)
		{
			return false;
		}

		return containsAll(Arrays.asList(points));
	}

	@Override
	public boolean containsAll(Collection<Vector3> points)
	{
		if (points == null || points.isEmpty())
		{
			return false;
		}

		for (Vector3 point : points)
		{
			if (!contains(point))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean containsAny(Vector3... points)
	{
		if (points == null || points.length == 0)
		{
			return false;
		}

		return containsAny(Arrays.asList(points));
	}

	@Override
	public boolean containsAny(Collection<Vector3> points)
	{
		if (points == null || points.isEmpty())
		{
			return false;
		}

		for (Vector3 point : points)
		{
			if (contains(point))
			{
				return true;
			}
		}
		return false;
	}

}
