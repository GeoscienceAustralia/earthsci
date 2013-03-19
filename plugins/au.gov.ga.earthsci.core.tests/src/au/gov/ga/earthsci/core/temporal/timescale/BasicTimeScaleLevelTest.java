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
package au.gov.ga.earthsci.core.temporal.timescale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

/**
 * Unit tests for the {@link BasicTimeScaleLevel} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicTimeScaleLevelTest
{

	@Test
	public void testCreateWithValid()
	{
		String name = "name";
		String description = "description";
		BigInteger resolution = BigInteger.valueOf(1000);
		int order = 1;
		
		BasicTimeScaleLevel classUnderTest = new BasicTimeScaleLevel(name, description, resolution, order);
		
		assertEquals(name, classUnderTest.getName());
		assertEquals(description, classUnderTest.getDescription());
		assertEquals(resolution, classUnderTest.getResolution());
		assertEquals(order, classUnderTest.getOrder());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithBlankName()
	{
		String name = "";
		String description = "description";
		BigInteger resolution = BigInteger.valueOf(1000);
		int order = 1;
		
		new BasicTimeScaleLevel(name, description, resolution, order);
	}
	
	@Test()
	public void testCreateWithBlankDescription()
	{
		String name = "name";
		String description = "";
		BigInteger resolution = BigInteger.valueOf(1000);
		int order = 1;
		
		BasicTimeScaleLevel classUnderTest = new BasicTimeScaleLevel(name, description, resolution, order);
		
		assertEquals(name, classUnderTest.getName());
		assertEquals(description, classUnderTest.getDescription());
		assertEquals(resolution, classUnderTest.getResolution());
		assertEquals(order, classUnderTest.getOrder());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNoResolution()
	{
		String name = "name";
		String description = "description";
		BigInteger resolution = null;
		int order = 1;
		
		new BasicTimeScaleLevel(name, description, resolution, order);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithZeroResolution()
	{
		String name = "name";
		String description = "description";
		BigInteger resolution = BigInteger.valueOf(0);
		int order = 1;
		
		new BasicTimeScaleLevel(name, description, resolution, order);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNegativeResolution()
	{
		String name = "name";
		String description = "description";
		BigInteger resolution = BigInteger.valueOf(-1000);
		int order = 1;
		
		new BasicTimeScaleLevel(name, description, resolution, order);
	}
	
	@Test()
	public void testCreateWithZeroOrder()
	{
		String name = "name";
		String description = "description";
		BigInteger resolution = BigInteger.valueOf(1000);
		int order = 0;
		
		BasicTimeScaleLevel classUnderTest = new BasicTimeScaleLevel(name, description, resolution, order);
		
		assertEquals(name, classUnderTest.getName());
		assertEquals(description, classUnderTest.getDescription());
		assertEquals(resolution, classUnderTest.getResolution());
		assertEquals(order, classUnderTest.getOrder());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNegativeOrder()
	{
		String name = "name";
		String description = "description";
		BigInteger resolution = BigInteger.valueOf(1000);
		int order = -1;
		
		new BasicTimeScaleLevel(name, description, resolution, order);
	}

	@Test
	public void testCompareToLessThan()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 1);
		BasicTimeScaleLevel l2 = new BasicTimeScaleLevel("name2", "description2", BigInteger.ONE, 2);
		
		assertTrue(l1.compareTo(l2) < 0);
	}
	
	@Test
	public void testCompareToEqualTo()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 1);
		BasicTimeScaleLevel l2 = new BasicTimeScaleLevel("name2", "description2", BigInteger.ONE, 1);
		
		assertTrue(l1.compareTo(l2) == 0);
	}
	
	@Test
	public void testCompareToGreaterThan()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 2);
		BasicTimeScaleLevel l2 = new BasicTimeScaleLevel("name2", "description2", BigInteger.ONE, 1);
		
		assertTrue(l1.compareTo(l2) > 0);
	}

	@Test
	public void testEqualsWithSelf()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 2);
		BasicTimeScaleLevel l2 = l1;
		
		assertTrue(l1.equals(l2));
	}
	
	@Test
	public void testEqualsWithEqual()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 2);
		BasicTimeScaleLevel l2 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 2);
		
		assertTrue(l1.equals(l2));
		assertTrue(l2.equals(l1));
	}
	
	@Test
	public void testEqualsWithDifferentName()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 2);
		BasicTimeScaleLevel l2 = new BasicTimeScaleLevel("name2", "description1", BigInteger.TEN, 2);
		
		assertFalse(l1.equals(l2));
		assertFalse(l2.equals(l1));
	}
	
	@Test
	public void testEqualsWithDifferentDescription()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 2);
		BasicTimeScaleLevel l2 = new BasicTimeScaleLevel("name1", "description2", BigInteger.TEN, 2);
		
		assertTrue(l1.equals(l2));
		assertTrue(l2.equals(l1));
	}
	
	@Test
	public void testEqualsWithDifferentResolution()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 2);
		BasicTimeScaleLevel l2 = new BasicTimeScaleLevel("name1", "description1", BigInteger.ONE, 2);
		
		assertFalse(l1.equals(l2));
		assertFalse(l2.equals(l1));
	}
	
	@Test
	public void testEqualsWithDifferentOrder()
	{
		BasicTimeScaleLevel l1 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 2);
		BasicTimeScaleLevel l2 = new BasicTimeScaleLevel("name1", "description1", BigInteger.TEN, 1);
		
		assertFalse(l1.equals(l2));
		assertFalse(l2.equals(l1));
	}
}

