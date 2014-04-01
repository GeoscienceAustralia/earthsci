/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

/**
 * Basic implementation of {@link Bounds}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Bounds
{
	public final Position minimum;
	public final Position maximum;
	public final Position center;
	public final Angle deltaLatitude;
	public final Angle deltaLongitude;
	public final double deltaElevation;

	public static Bounds fromSector(Sector sector)
	{
		return fromSector(sector, 0, 0);
	}

	public static Bounds fromSector(Sector sector, double minimumElevation, double maximumElevation)
	{
		Position minimum = new Position(sector.getMinLatitude(), sector.getMinLongitude(), minimumElevation);
		Position maximum = new Position(sector.getMaxLatitude(), sector.getMaxLongitude(), maximumElevation);
		return new Bounds(minimum, maximum);
	}

	public static Bounds union(Bounds bounds1, Bounds bounds2)
	{
		if (bounds1 == null)
		{
			return bounds2;
		}
		if (bounds2 == null)
		{
			return bounds1;
		}
		Position minimum1 = bounds1.getMinimum();
		Position minimum2 = bounds2.getMinimum();
		Position maximum1 = bounds1.getMaximum();
		Position maximum2 = bounds2.getMaximum();
		Position minimum = Position.fromDegrees(
				Math.min(minimum1.latitude.degrees, minimum2.latitude.degrees),
				Math.min(minimum1.longitude.degrees, minimum2.longitude.degrees),
				Math.min(minimum1.elevation, minimum2.elevation));
		Position maximum = Position.fromDegrees(
				Math.max(maximum1.latitude.degrees, maximum2.latitude.degrees),
				Math.max(maximum1.longitude.degrees, maximum2.longitude.degrees),
				Math.max(maximum1.elevation, maximum2.elevation));
		return new Bounds(minimum, maximum);
	}

	public static Bounds union(Bounds bounds, Position position)
	{
		if (position == null)
		{
			return bounds;
		}
		if (bounds == null)
		{
			return new Bounds(position);
		}
		Position bminimum = bounds.getMinimum();
		Position bmaximum = bounds.getMaximum();
		Position minimum = Position.fromDegrees(
				Math.min(bminimum.latitude.degrees, position.latitude.degrees),
				Math.min(bminimum.longitude.degrees, position.longitude.degrees),
				Math.min(bminimum.elevation, position.elevation));
		Position maximum = Position.fromDegrees(
				Math.max(bmaximum.latitude.degrees, position.latitude.degrees),
				Math.max(bmaximum.longitude.degrees, position.longitude.degrees),
				Math.max(bmaximum.elevation, position.elevation));
		return new Bounds(minimum, maximum);
	}

	public Bounds(Position position)
	{
		this(position, position);
	}

	public Bounds(Position minimum, Position maximum)
	{
		this.minimum = minimum;
		this.maximum = maximum;
		this.center = Position.interpolate(0.5, minimum, maximum);
		this.deltaLatitude = maximum.latitude.subtract(minimum.latitude);
		this.deltaLongitude = maximum.longitude.subtract(minimum.longitude);
		this.deltaElevation = maximum.elevation - minimum.elevation;
	}

	/**
	 * @return Minimum corner position of the bounding box
	 */
	public Position getMinimum()
	{
		return minimum;
	}

	/**
	 * @return Maximum corner position of the bounding box
	 */
	public Position getMaximum()
	{
		return maximum;
	}

	/**
	 * @return Center of the bounding box, sitting halfway on the line between
	 *         the minimum and maximum corners
	 */
	public Position getCenter()
	{
		return center;
	}

	public Sector toSector()
	{
		return new Sector(minimum.latitude, maximum.latitude, minimum.longitude, maximum.longitude);
	}

	/**
	 * Calculate the union between these bounds and the given bounds.
	 * 
	 * @param bounds
	 * @return Union between this and <code>bounds</code>
	 */
	public Bounds union(Bounds bounds)
	{
		return Bounds.union(this, bounds);
	}

	/**
	 * Calculate the union between these bounds and the given position.
	 * 
	 * @param position
	 * @return Union between this and <code>position</code>
	 */
	public Bounds union(Position position)
	{
		return Bounds.union(this, position);
	}

	/**
	 * Do these bounds contain the given position, inclusive?
	 * 
	 * @param position
	 * @return True if the bounds contain <code>position</code>
	 */
	public boolean contains(Position position)
	{
		return contains(position, true);
	}

	/**
	 * Do these bounds contain the given position?
	 * 
	 * @param position
	 * @param inclusive
	 *            If true, includes positions that lie on the boundary of the
	 *            bounds
	 * @return True if the bounds contain <code>position</code>
	 */
	public boolean contains(Position position, boolean inclusive)
	{
		return lessThan(minimum.latitude.degrees, position.latitude.degrees, inclusive) &&
				lessThan(position.latitude.degrees, maximum.latitude.degrees, inclusive) &&
				lessThan(minimum.longitude.degrees, position.longitude.degrees, inclusive) &&
				lessThan(position.longitude.degrees, maximum.longitude.degrees, inclusive) &&
				lessThan(minimum.elevation, position.elevation, inclusive) &&
				lessThan(position.elevation, maximum.elevation, inclusive);
	}

	private static boolean lessThan(double d1, double d2, boolean orEqualTo)
	{
		return orEqualTo ? d1 <= d2 : d1 < d2;
	}
}
