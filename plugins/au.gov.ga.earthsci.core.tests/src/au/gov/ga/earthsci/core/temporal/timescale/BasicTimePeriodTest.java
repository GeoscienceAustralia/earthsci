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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

import au.gov.ga.earthsci.core.temporal.BigTime;
import au.gov.ga.earthsci.core.temporal.timescale.BasicTimePeriod.Builder;

/**
 * Unit tests for the {@link BasicTimePeriod} and associated {@link Builder}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicTimePeriodTest
{

	private static final ITimeScaleLevel level1 = new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 1);
	private static final ITimeScaleLevel level2 = new BasicTimeScaleLevel("level2", "level2", BigInteger.TEN, 2);
	
	@Test
	public void testBuilderWithValidNoSubPeriod()
	{
		String id = "id";
		String name = "name";
		String description = "description";

		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		BasicTimePeriod classUnderTest = Builder.buildTimePeriod(id, name, description)
												.from(start, true)
												.to(end, true)
												.atLevel(level1)
												.build();
		
		assertNotNull(classUnderTest);
		
		assertEquals(id, classUnderTest.getId());
		assertEquals(name, classUnderTest.getName());
		assertEquals(description, classUnderTest.getDescription());
		
		assertNotNull(classUnderTest.getRange());
		
		assertEquals(start, classUnderTest.getRange().getMinValue());
		assertEquals(true, classUnderTest.getRange().isInclusiveLeft());
		assertEquals(false, classUnderTest.getRange().isOpenLeft());
		
		assertEquals(end, classUnderTest.getRange().getMaxValue());
		assertEquals(true, classUnderTest.getRange().isInclusiveRight());
		assertEquals(false, classUnderTest.getRange().isOpenRight());
		
		assertNotNull(classUnderTest.getSubPeriods());
		assertTrue(classUnderTest.getSubPeriods().isEmpty());
		assertFalse(classUnderTest.hasSubPeriods());
		
		assertEquals(level1, classUnderTest.getLevel());
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithNoId()
	{
		String id = null;
		String name = "name";
		String description = "description";

		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		Builder.buildTimePeriod(id, name, description)
			   .from(start, true)
			   .to(end, true)
			   .atLevel(level1)
			   .build();
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithNoName()
	{
		String id = "id";
		String name = "";
		String description = "description";

		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		Builder.buildTimePeriod(id, name, description)
			   .from(start, true)
			   .to(end, true)
			   .atLevel(level1)
			   .build();
	}
	
	@Test
	public void testBuilderWithNoDescription()
	{
		String id = "id";
		String name = "name";
		String description = "";

		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		BasicTimePeriod classUnderTest = Builder.buildTimePeriod(id, name, description)
												.from(start, true)
												.to(end, true)
												.atLevel(level1)
												.build();
		
		assertNotNull(classUnderTest);
	}
	
	@Test
	public void testBuilderWithNoStart()
	{
		String id = "id";
		String name = "name";
		String description = "description";

		BigTime start = null;
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		BasicTimePeriod classUnderTest = Builder.buildTimePeriod(id, name, description)
												.from(start, true)
												.to(end, true)
												.atLevel(level1)
												.build();
		
		assertNotNull(classUnderTest);
		
		assertNotNull(classUnderTest.getRange());
		
		assertEquals(start, classUnderTest.getRange().getMinValue());
		assertEquals(false, classUnderTest.getRange().isInclusiveLeft());
		assertEquals(true, classUnderTest.getRange().isOpenLeft());
		
		assertEquals(end, classUnderTest.getRange().getMaxValue());
		assertEquals(true, classUnderTest.getRange().isInclusiveRight());
		assertEquals(false, classUnderTest.getRange().isOpenRight());
	}
	
	@Test
	public void testBuilderWithNoEnd()
	{
		String id = "id";
		String name = "name";
		String description = "description";

		BigTime start = new BigTime(BigInteger.valueOf(10000));
		BigTime end = null;
		
		BasicTimePeriod classUnderTest = Builder.buildTimePeriod(id, name, description)
												.from(start, true)
												.to(end, true)
												.atLevel(level1)
												.build();
		
		assertNotNull(classUnderTest);
		
		assertNotNull(classUnderTest.getRange());
		
		assertEquals(start, classUnderTest.getRange().getMinValue());
		assertEquals(true, classUnderTest.getRange().isInclusiveLeft());
		assertEquals(false, classUnderTest.getRange().isOpenLeft());
		
		assertEquals(end, classUnderTest.getRange().getMaxValue());
		assertEquals(false, classUnderTest.getRange().isInclusiveRight());
		assertEquals(true, classUnderTest.getRange().isOpenRight());
	}
	
	@Test
	public void testBuilderWithNoRange()
	{
		String id = "id";
		String name = "name";
		String description = "description";

		BasicTimePeriod classUnderTest = Builder.buildTimePeriod(id, name, description)
												.atLevel(level1)
												.build();
		
		assertNotNull(classUnderTest);
		
		assertNotNull(classUnderTest.getRange());
		
		assertEquals(null, classUnderTest.getRange().getMinValue());
		assertEquals(false, classUnderTest.getRange().isInclusiveLeft());
		assertEquals(true, classUnderTest.getRange().isOpenLeft());
		
		assertEquals(null, classUnderTest.getRange().getMaxValue());
		assertEquals(false, classUnderTest.getRange().isInclusiveRight());
		assertEquals(true, classUnderTest.getRange().isOpenRight());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBuilderWithNoLevel()
	{
		String id = "id";
		String name = "name";
		String description = "description";

		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		Builder.buildTimePeriod(id, name, description)
			   .from(start, true)
			   .to(end, true)
			   .atLevel(null)
			   .build();
	}
	
	@Test
	public void testBuilderWithValidSubPeriod()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description1")
										 .atLevel(level2)
										 .build();
		
		BasicTimePeriod period2 = Builder.buildTimePeriod("id2", "name2", "description2")
				 						 .atLevel(level1)
				 						 .withSubPeriod(period1)
				 						 .build();
		
		assertNotNull(period1);
		assertNotNull(period2);
		
		assertTrue(period2.getSubPeriods().contains(period1));
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithSubPeriodSameLevel()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description1")
										 .atLevel(level2)
										 .build();
		
		Builder.buildTimePeriod("id2", "name2", "description2")
			   .atLevel(level2)
			   .withSubPeriod(period1)
			   .build();
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithSubPeriodHigherLevel()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description1")
										 .atLevel(level1)
										 .build();
		
		Builder.buildTimePeriod("id2", "name2", "description2")
			   .atLevel(level2)
			   .withSubPeriod(period1)
			   .build();
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithSubPeriodSameId()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description1")
										 .atLevel(level2)
										 .build();
		
		Builder.buildTimePeriod("id1", "name2", "description2")
			   .atLevel(level1)
			   .withSubPeriod(period1)
			   .build();
	}
	
	@Test
	public void testBuilderWithSubPeriodSameName()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name", "description1")
										 .atLevel(level2)
										 .build();
		
		BasicTimePeriod period2 = Builder.buildTimePeriod("id2", "name", "description2")
				 						 .atLevel(level1)
				 						 .withSubPeriod(period1)
				 						 .build();

		assertNotNull(period1);
		assertNotNull(period2);
		
		assertTrue(period2.getSubPeriods().contains(period1));
	}
	
	@Test
	public void testBuilderWithSubPeriodSameDescription()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description")
										 .atLevel(level2)
										 .build();
		
		BasicTimePeriod period2 = Builder.buildTimePeriod("id2", "name2", "description")
				 						 .atLevel(level1)
				 						 .withSubPeriod(period1)
				 						 .build();

		assertNotNull(period1);
		assertNotNull(period2);
		
		assertTrue(period2.getSubPeriods().contains(period1));
	}
	
	@Test
	public void testCompareToWithEqualRange()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description")
										 .from(new BigTime(BigInteger.valueOf(1000)), true)
										 .to(new BigTime(BigInteger.valueOf(10000)), true)
										 .atLevel(level2)
										 .build();
		
		BasicTimePeriod period2 = Builder.buildTimePeriod("id2", "name2", "description")
				 						 .from(new BigTime(BigInteger.valueOf(1000)), true)
				 						 .to(new BigTime(BigInteger.valueOf(20000)), true)
				 						 .atLevel(level1)
				 						 .withSubPeriod(period1)
				 						 .build();

		assertTrue(period1.compareTo(period2) == 0);
	}
	
	@Test
	public void testCompareToWithEarlierStartRange()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description")
										 .from(new BigTime(BigInteger.valueOf(999)), true)
										 .to(new BigTime(BigInteger.valueOf(10000)), true)
										 .atLevel(level2)
										 .build();
		
		BasicTimePeriod period2 = Builder.buildTimePeriod("id2", "name2", "description")
				 						 .from(new BigTime(BigInteger.valueOf(1000)), true)
				 						 .to(new BigTime(BigInteger.valueOf(10000)), true)
				 						 .atLevel(level1)
				 						 .withSubPeriod(period1)
				 						 .build();

		assertTrue(period1.compareTo(period2) < 0);
	}
	
	@Test
	public void testCompareToWithLaterStartRange()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description")
										 .from(new BigTime(BigInteger.valueOf(1001)), true)
										 .to(new BigTime(BigInteger.valueOf(10000)), true)
										 .atLevel(level2)
										 .build();
		
		BasicTimePeriod period2 = Builder.buildTimePeriod("id2", "name2", "description")
				 						 .from(new BigTime(BigInteger.valueOf(1000)), true)
				 						 .to(new BigTime(BigInteger.valueOf(20000)), true)
				 						 .atLevel(level1)
				 						 .withSubPeriod(period1)
				 						 .build();

		assertTrue(period1.compareTo(period2) > 0);
	}
	

	@Test
	public void testContainsWithNull()
	{
		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		BasicTimePeriod period1 = Builder.buildTimePeriod("id", "name", "description")
										 .from(start, true)
										 .to(end, true)
										 .atLevel(level1)
										 .build();
		
		BigTime test = null;
		
		assertFalse(period1.contains(test));
	}
	
	@Test
	public void testContainsWithEarlier()
	{
		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		BasicTimePeriod period1 = Builder.buildTimePeriod("id", "name", "description")
										 .from(start, true)
										 .to(end, true)
										 .atLevel(level1)
										 .build();
		
		BigTime test = new BigTime(BigInteger.valueOf(999));
		
		assertFalse(period1.contains(test));
	}
	
	@Test
	public void testContainsWithIn()
	{
		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		BasicTimePeriod period1 = Builder.buildTimePeriod("id", "name", "description")
										 .from(start, true)
										 .to(end, true)
										 .atLevel(level1)
										 .build();
		
		BigTime test = new BigTime(BigInteger.valueOf(1000));
		
		assertTrue(period1.contains(test));
	}
	
	@Test
	public void testContainsWithLater()
	{
		BigTime start = new BigTime(BigInteger.valueOf(1000));
		BigTime end = new BigTime(BigInteger.valueOf(10000));
		
		BasicTimePeriod period1 = Builder.buildTimePeriod("id", "name", "description")
										 .from(start, true)
										 .to(end, true)
										 .atLevel(level1)
										 .build();
		
		BigTime test = new BigTime(BigInteger.valueOf(10001));
		
		assertFalse(period1.contains(test));
	}
	
	@Test
	public void testDefaultLabelProviderNonOverlappingSubPeriods()
	{
		BasicTimePeriod sub1 = Builder.buildTimePeriod("sub1", "sub1", "sub1")
									  .to(new BigTime(BigInteger.ZERO), true)
									  .atLevel(level2)
									  .build();
		
		BasicTimePeriod sub2 = Builder.buildTimePeriod("sub2", "sub2", "sub2")
				  					  .from(new BigTime(BigInteger.ONE), true)
				  					  .atLevel(level2)
				  					  .build();
		
		BasicTimePeriod period = Builder.buildTimePeriod("parent", "parent", "parent")
				 						.atLevel(level1)
				 						.withSubPeriods(sub1, sub2)
				 						.build();
		
		BigTime test = new BigTime(BigInteger.ZERO);
		
		assertEquals("sub1", sub1.getLabel(test));
		assertEquals(null, sub2.getLabel(test));
		assertEquals("sub1", period.getLabel(test));
	}
	
	@Test
	public void testDefaultLabelProviderOverlappingSubPeriods()
	{
		BasicTimePeriod sub1 = Builder.buildTimePeriod("sub1", "sub1", "sub1")
									  .to(new BigTime(BigInteger.ZERO), true)
									  .atLevel(level2)
									  .build();
		
		BasicTimePeriod sub2 = Builder.buildTimePeriod("sub2", "sub2", "sub2")
				  					  .from(new BigTime(BigInteger.valueOf(-10)), true)
				  					  .atLevel(level2)
				  					  .build();
		
		BasicTimePeriod period = Builder.buildTimePeriod("parent", "parent", "parent")
				 						.atLevel(level1)
				 						.withSubPeriods(sub1, sub2)
				 						.build();
		
		BigTime test = new BigTime(BigInteger.ZERO);
		
		assertEquals("sub1", sub1.getLabel(test));
		assertEquals("sub2", sub2.getLabel(test));
		assertEquals("sub1 / sub2", period.getLabel(test));
	}
	
	@Test
	public void testEqualsWithNull()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description")
				 						 .atLevel(level2)
				 						 .build();

		BasicTimePeriod period2 = null;
		
		assertFalse(period1.equals(period2));
	}
	
	@Test
	public void testEqualsWithSelf()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description")
				 						 .atLevel(level2)
				 						 .build();

		BasicTimePeriod period2 = period1;
		
		assertTrue(period1.equals(period2));
	}
	
	@Test
	public void testEqualsWithSameId()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description1")
				 						 .atLevel(level1)
				 						 .build();

		BasicTimePeriod period2 = Builder.buildTimePeriod("id1", "name2", "description2")
				 						 .atLevel(level2)
				 						 .build();
		
		assertTrue(period1.equals(period2));
	}
	
	@Test
	public void testEqualsWithDifferentId()
	{
		BasicTimePeriod period1 = Builder.buildTimePeriod("id1", "name1", "description1")
				 						 .atLevel(level1)
				 						 .build();

		BasicTimePeriod period2 = Builder.buildTimePeriod("id2", "name2", "description2")
				 						 .atLevel(level2)
				 						 .build();
		
		assertFalse(period1.equals(period2));
	}
}
