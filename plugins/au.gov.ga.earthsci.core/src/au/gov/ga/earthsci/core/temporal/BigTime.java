/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.core.temporal;

import java.math.BigInteger;
import java.util.Date;

import au.gov.ga.earthsci.common.util.Validate;

/**
 * Represents an immutable time instant with (potentially) nanosecond precision.
 * Time is stored as an arbitrary precision integer as the number of nanoseconds
 * since {@code 0:00:00:0000 January 1, 1970 UTC} (i.e. the standard Unix date
 * epoch).
 * <p/>
 * All times are represented in UTC.
 * <p/>
 * While time instants can be represented to nanosecond precision, each
 * {@link BigTime} instance includes a {@code resolution} in nanoseconds, which
 * gives an indication of the scale at which the time instant can be considered
 * useful. For example, a time instant obtained from a Java {@link Date} object
 * might have a resolution of 1 second, while an instance representing a
 * geological time period may have a resolution of 1 million years. It would be
 * programmer error to use a {@link BigTime} instance at a resolution higher
 * than it's inherent resolution.
 * <p/>
 * This class can be used in place of the standard {@link Date} class when
 * either:
 * <ul>
 * <li>Precision higher than millisecond is required; or
 * <li>Dates greater than ~292 Million Years (past or future) need to be
 * represented (e.g. geological time scales)
 * </ul>
 * <p/>
 * <b>Note:</b> The natural ordering of this class (implemented in
 * {@link Comparable}) is inconsistent with {@code equals}. Two instances are
 * considered equal via the {@link #equals(Object)} method if they have the same
 * time instant AND the same resolution. The {@link #compareTo(BigTime)} method,
 * on the other hand, compares two instances
 * <em>at the lowest common resolution</em>. <br/>
 * For example, a time of {@code [2010-01-01 12:00:00:000 resolution DAY]} is
 * not equal to {@code [2010-01-01 12:05:01:000 resolution SECOND]} via
 * {@link #equals(Object)}, but is via {@link #compareTo(BigTime)}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class BigTime implements Comparable<BigTime>
{

	private static final BigInteger THOUSAND = BigInteger.valueOf(1000);
	private static final BigInteger MILLION = BigInteger.valueOf(1000000);

	private static final BigInteger SECONDS_IN_MINUTE = BigInteger.valueOf(60);
	private static final BigInteger MINUTES_IN_HOUR = BigInteger.valueOf(60);
	private static final BigInteger HOURS_IN_DAY = BigInteger.valueOf(24);
	private static final double DAYS_IN_YEAR = 365.242196; // Approximation to the mean tropical year

	public static final BigInteger NANOS_IN_MICROSECOND = THOUSAND;
	public static final BigInteger NANOS_IN_MILLISECOND = NANOS_IN_MICROSECOND.multiply(THOUSAND);
	public static final BigInteger NANOS_IN_SECOND = NANOS_IN_MILLISECOND.multiply(THOUSAND);
	public static final BigInteger NANOS_IN_MINUTE = NANOS_IN_SECOND.multiply(SECONDS_IN_MINUTE);
	public static final BigInteger NANOS_IN_HOUR = NANOS_IN_MINUTE.multiply(MINUTES_IN_HOUR);
	public static final BigInteger NANOS_IN_DAY = NANOS_IN_HOUR.multiply(HOURS_IN_DAY);
	public static final BigInteger NANOS_IN_YEAR = BigInteger
			.valueOf((long) (NANOS_IN_DAY.doubleValue() * DAYS_IN_YEAR));

	public static final BigInteger NANOSECOND_RESOLUTION = BigInteger.ONE;
	public static final BigInteger MICROSECOND_RESOLUTION = NANOS_IN_MICROSECOND;
	public static final BigInteger MILLISECOND_RESOLUTION = NANOS_IN_MILLISECOND;
	public static final BigInteger SECOND_RESOLUTION = NANOS_IN_SECOND;
	public static final BigInteger YEAR_RESOLUTION = NANOS_IN_YEAR;
	public static final BigInteger MA_RESOLUTION = NANOS_IN_YEAR.multiply(MILLION);
	public static final BigInteger BA_RESOLUTION = MA_RESOLUTION.multiply(THOUSAND);

	public static final BigInteger EARLIEST_DATE_VALUE_IN_NANOS = BigInteger.valueOf(Long.MIN_VALUE).multiply(
			NANOS_IN_MILLISECOND);
	public static final BigInteger LARGEST_DATE_VALUE_IN_NANOS = BigInteger.valueOf(Long.MAX_VALUE).multiply(
			NANOS_IN_MILLISECOND);

	/**
	 * Create a new {@link BigTime} instance that represents the same time as
	 * the given {@link Date}.
	 * 
	 * @return A new {@link BigTime} instance that represents the same time as
	 *         the given {@link Date}, with millisecond resolution.
	 */
	public static BigTime fromDate(Date d)
	{
		if (d == null)
		{
			return null;
		}

		return new BigTime(BigInteger.valueOf(d.getTime()).multiply(NANOS_IN_MILLISECOND), MILLISECOND_RESOLUTION);
	}

	/**
	 * Create a new {@link BigTime} instance that represents the current time.
	 * 
	 * @return a new {@link BigTime} instance representing the current instant
	 *         in time (to millisecond resolution)
	 */
	public static BigTime now()
	{
		return fromDate(new Date());
	}

	// Choice of nanoseconds as the quanta of time is somewhat arbitrary, but is
	// 1. Small enough for most (if not all) earth science simulations etc.; and
	// 2. The smallest unit of time the Java runtime uses (see System.nanoTime()).

	/**
	 * The number of nanoseconds since epoch (
	 * {@code 0:00:00:0000 January 1, 1970 UTC}) this instance represents.
	 */
	private BigInteger nansecondsSinceEpoch;

	/**
	 * The resolution of this instance, in nanoseconds. This is an indication of
	 * the minimum scale at which this instance can be considered useful.
	 * <p/>
	 * See constants defined in this class for useful resolutions.
	 */
	private BigInteger resolution;

	/**
	 * Create a new instance with the given nanosecond offset and resolution
	 */
	protected BigTime(long nanos, BigInteger resolution)
	{
		this(BigInteger.valueOf(nanos), resolution);
	}

	/**
	 * Create a new instance with the given nanosecond offset and nanosecond
	 * resolution
	 */
	public BigTime(BigInteger nanos)
	{
		this(nanos, NANOSECOND_RESOLUTION);
	}

	/**
	 * Create a new instance with the given nanosecond offset and resolution
	 */
	public BigTime(BigInteger nanos, BigInteger resolution)
	{
		Validate.notNull(nanos, "A valid nanosecond value is required"); //$NON-NLS-1$

		this.nansecondsSinceEpoch = nanos;
		this.resolution = resolution == null ? NANOSECOND_RESOLUTION : resolution;
	}

	@Override
	public int compareTo(BigTime o)
	{
		BigInteger lowestResolution = resolution.max(o.resolution);

		BigTime thisNormalised = changeResolution(lowestResolution).normalise();
		BigTime otherNormalised = o.changeResolution(lowestResolution).normalise();

		return thisNormalised.nansecondsSinceEpoch.compareTo(otherNormalised.nansecondsSinceEpoch);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof BigTime))
		{
			return false;
		}

		BigTime other = (BigTime) obj;

		return other.nansecondsSinceEpoch.equals(this.nansecondsSinceEpoch) && other.resolution.equals(this.resolution);
	}

	@Override
	public int hashCode()
	{
		return nansecondsSinceEpoch.hashCode() + resolution.hashCode();
	}

	/**
	 * Return the resolution of this instance, in nanoseconds.
	 * 
	 * @return the resolution of this instance.
	 * 
	 * @see #resolution
	 */
	public BigInteger getResolution()
	{
		return resolution;
	}

	/**
	 * @return the time this instance represents, in nanoseconds since epoch.
	 *         Should always be used in conjunction with the {@link #resolution}
	 *         .
	 * 
	 * @see #nansecondsSinceEpoch
	 * @see #resolution
	 */
	public BigInteger getNansecondsSinceEpoch()
	{
		return nansecondsSinceEpoch;
	}

	/**
	 * Returns whether or not this BigTime instance can be represented as a
	 * {@link Date} object (to the resolution a {@link Date} allows).
	 * 
	 * @return <code>true</code> if this instance is within the range of the
	 *         {@link Date} class
	 */
	public boolean isInDateRange()
	{
		return this.nansecondsSinceEpoch.compareTo(EARLIEST_DATE_VALUE_IN_NANOS) >= 0
				&& this.nansecondsSinceEpoch.compareTo(LARGEST_DATE_VALUE_IN_NANOS) <= 0;
	}

	/**
	 * Returns the {@link Date} representation of the time this instance
	 * represents, if it is possible.
	 * 
	 * @return The {@link Date} representation of this instance; or
	 *         <code>null</code> if this instance cannot be represented as a
	 *         {@link Date}.
	 * 
	 * @see #isInDateRange()
	 */
	public Date getDate()
	{
		if (!isInDateRange())
		{
			return null;
		}

		return new Date(this.nansecondsSinceEpoch.divide(NANOS_IN_MILLISECOND).longValue());
	}

	/**
	 * Return a new {@link BigTime} instance that represents the same time
	 * instant at the resolution of this instance, but with all information at
	 * higher resolutions removed.
	 * 
	 * @return A new instance that represents the same time instant, but with
	 *         all higher resolution information removed.
	 */
	public BigTime normalise()
	{
		return new BigTime(normaliseToResolution(nansecondsSinceEpoch, resolution), resolution);
	}

	/**
	 * Return a new {@link BigTime} instance that represents the same time
	 * instant at the provided resolution.
	 * <p/>
	 * Care should be taken if changing to a higher resolution as this may imply
	 * the time value is more precise than it may actually be.
	 * 
	 * @param resolution
	 *            The resolution to change to
	 * 
	 * @return A new instance representing the same time at the provided
	 *         resolution
	 */
	public BigTime changeResolution(BigInteger resolution)
	{
		return new BigTime(nansecondsSinceEpoch, resolution);
	}

	/**
	 * Calculate a normalised version of the provided nanosecond value to remove
	 * any information at resolutions higher than the provided value.
	 * <p/>
	 * For example, a value of 1021 normalised to microsecond resolution will
	 * return 1000.
	 * 
	 * @return A normalised version of the given nanosecond value
	 */
	private static BigInteger normaliseToResolution(BigInteger nanos, BigInteger resolution)
	{
		//		// Use as much precision as is available to reduce the large drift that occurs once the resolution
		//		// exceeds the range of a long value
		//		double decimalPart = resolution % 1f;
		//		double decimalPower = Math.pow(10, decimalPart);
		//		int count = (int)Math.floor(resolution) + 1;
		//		double d = decimalPower;
		//		while (d < Long.MAX_VALUE && count > 0)
		//		{
		//			count--;
		//			decimalPower = d;
		//			d *= 10;
		//		}
		//		BigInteger nanosInResolution = BigInteger.TEN.pow(count).multiply(BigInteger.valueOf((long)decimalPower));

		return nanos.divide(resolution).multiply(resolution);
	}
}
