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

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

/**
 * Unit tests for the {@link BigTime} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BigTimeTest
{

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");

	@Test
	public void testCreateFromDateWithNull() throws Exception
	{
		Date d = null;
		BigTime result = BigTime.fromDate(d);

		assertNull(result);
	}

	@Test
	public void testCreateFromDateWithPast() throws Exception
	{
		Date d = dateFormat.parse("1900-01-01 12:00:00:000");
		BigTime result = BigTime.fromDate(d);

		assertNotNull(result);
		assertEquals(d.getTime() * 1000000, result.getNansecondsSinceEpoch().longValue());
		assertEquals(BigTime.MILLISECOND_RESOLUTION, result.getResolution());
	}

	@Test
	public void testCreateFromDateWithFuture() throws Exception
	{
		Date d = dateFormat.parse("2100-01-01 12:00:00:123");
		BigTime result = BigTime.fromDate(d);

		assertNotNull(result);
		assertEquals(d.getTime() * 1000000, result.getNansecondsSinceEpoch().longValue());
		assertEquals(BigTime.MILLISECOND_RESOLUTION, result.getResolution());
	}

	@Test
	public void testCreateFromNow() throws Exception
	{
		BigTime result = BigTime.now();

		assertNotNull(result);
		// No real way to test accuracy of time - have to assume its ok 
		assertEquals(BigTime.MILLISECOND_RESOLUTION, result.getResolution());
	}

	@Test
	public void testEqualsWithNull() throws Exception
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = null;

		assertFalse(t1.equals(t2));
	}

	@Test
	public void testEqualsWithSelf() throws Exception
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = t1;

		assertTrue(t1.equals(t2));
	}

	@Test
	public void testEqualsWithSameTimeSameResolution() throws Exception
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);

		assertTrue(t1.equals(t2));
	}

	@Test
	public void testEqualsWithDifferentTimeSameResolution() throws Exception
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = new BigTime(1001, BigTime.NANOSECOND_RESOLUTION);

		assertFalse(t1.equals(t2));
	}

	@Test
	public void testEqualsWithSameTimeDifferentResolution() throws Exception
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = new BigTime(1000, BigTime.MICROSECOND_RESOLUTION);

		assertFalse(t1.equals(t2));
	}

	@Test
	public void testEqualsWithDifferentTimeDifferentResolution() throws Exception
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = new BigTime(1001, BigTime.MICROSECOND_RESOLUTION);

		assertFalse(t1.equals(t2));
	}

	@Test
	public void testNormaliseSimplePowerOfTen()
	{
		BigTime t = new BigTime(1999999, BigTime.MILLISECOND_RESOLUTION);

		BigTime normalised = t.normalise();

		assertNotNull(normalised);

		assertEquals(BigInteger.valueOf(1000000), normalised.getNansecondsSinceEpoch());
		assertEquals(BigTime.MILLISECOND_RESOLUTION, normalised.getResolution());
	}

	@Test
	public void testNormaliseComplexResolution()
	{
		BigTime t =
				new BigTime(BigInteger.valueOf((long) 1e9).multiply(BigTime.NANOS_IN_YEAR).add(BigInteger.ONE),
						BigTime.BA_RESOLUTION);

		BigTime normalised = t.normalise();

		assertNotNull(normalised);

		assertEquals(BigInteger.valueOf((long) 1e9).multiply(BigTime.NANOS_IN_YEAR),
				normalised.getNansecondsSinceEpoch());
		assertEquals(BigTime.BA_RESOLUTION, normalised.getResolution());
	}

	@Test(expected = NullPointerException.class)
	public void testCompareToWithNull()
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = null;

		t1.compareTo(t2);
	}

	@Test
	public void testCompareToWithSame()
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = t1;

		assertTrue(t1.compareTo(t2) == 0);
	}

	@Test
	public void testCompareToWithSameInstantSameResolutions()
	{
		BigTime t1 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = new BigTime(1000, BigTime.NANOSECOND_RESOLUTION);

		assertTrue(t1.compareTo(t2) == 0);
	}

	@Test
	public void testCompareToWithSameInstantDifferentResolutions()
	{
		BigTime t1 = new BigTime(1234567, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = new BigTime(1234323, BigTime.MICROSECOND_RESOLUTION);

		assertTrue(t1.compareTo(t2) == 0);
	}

	@Test
	public void testCompareToWithLessThanSameResolution()
	{
		BigTime t1 = new BigTime(-1000000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = new BigTime(1000000, BigTime.NANOSECOND_RESOLUTION);

		assertTrue(t1.compareTo(t2) < 0);
	}

	@Test
	public void testCompareToWithGreaterThanSameResolution()
	{
		BigTime t1 = new BigTime(1000000, BigTime.NANOSECOND_RESOLUTION);
		BigTime t2 = new BigTime(-1000000, BigTime.NANOSECOND_RESOLUTION);

		assertTrue(t1.compareTo(t2) > 0);
	}

	@Test
	public void testInDateRangeMinDate()
	{
		BigTime t = BigTime.fromDate(new Date(Long.MIN_VALUE));

		assertTrue(t.isInDateRange());
	}

	@Test
	public void testInDateRangeBeforeMinDate()
	{
		BigTime t =
				new BigTime(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigTime.NANOS_IN_MILLISECOND)
						.subtract(BigInteger.ONE));

		assertFalse(t.isInDateRange());
	}

	@Test
	public void testInDateRangeMaxDate()
	{
		BigTime t = BigTime.fromDate(new Date(Long.MAX_VALUE));

		assertTrue(t.isInDateRange());
	}

	@Test
	public void testInDateRangeAfterMaxDate()
	{
		BigTime t =
				new BigTime(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigTime.NANOS_IN_MILLISECOND)
						.add(BigInteger.ONE));

		assertFalse(t.isInDateRange());
	}

	@Test
	public void testGetDateInDateRange()
	{
		Date source = new Date(1000);
		BigTime t = BigTime.fromDate(source);

		Date result = t.getDate();

		assertEquals(source, result);
	}

	@Test
	public void testGetDateOutsideDateRange()
	{
		BigTime t =
				new BigTime(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigTime.NANOS_IN_MILLISECOND)
						.add(BigInteger.ONE));

		assertNull(t.getDate());
	}
}
