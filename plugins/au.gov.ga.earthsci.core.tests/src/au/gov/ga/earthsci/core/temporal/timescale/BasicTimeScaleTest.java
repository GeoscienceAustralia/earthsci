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

import au.gov.ga.earthsci.core.temporal.timescale.BasicTimeScale.Builder;

/**
 * Unit tests for the {@link BasicTimeScale} and {@link Builder} classes
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicTimeScaleTest
{

	@Test
	public void testBuilderWithValid()
	{
		String id = "id";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
													.atLevel(levels[0])
													.build();
		
		BasicTimeScale classUnderTest = Builder.buildTimeScale(id, name, description)
											   .withTopLevelPeriod(period)
											   .withLevels(levels)
											   .build();
		
		assertNotNull(classUnderTest);
		assertEquals(id, classUnderTest.getId());
		assertEquals(name, classUnderTest.getName());
		assertEquals(description, classUnderTest.getDescription());
		
		assertEquals(2, classUnderTest.getLevels().size());
		assertEquals(levels[1], classUnderTest.getLevels().get(0));
		assertEquals(levels[0], classUnderTest.getLevels().get(1));
		
		assertEquals(1, classUnderTest.getPeriods().size());
		assertEquals(period, classUnderTest.getPeriods().get(0));
	}
	
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithNoId()
	{
		String id = "";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
													.atLevel(levels[0])
													.build();
		
		Builder.buildTimeScale(id, name, description)
			   .withTopLevelPeriod(period)
			   .withLevels(levels)
			   .build();
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithNoName()
	{
		String id = "id";
		String name = "";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
													.atLevel(levels[0])
													.build();
		
		Builder.buildTimeScale(id, name, description)
			   .withTopLevelPeriod(period)
			   .withLevels(levels)
			   .build();
	}
	
	@Test
	public void testBuilderWithNoDescription()
	{
		String id = "id";
		String name = "name";
		String description = "";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
													.atLevel(levels[0])
													.build();
		
		BasicTimeScale classUnderTest = Builder.buildTimeScale(id, name, description)
											   .withTopLevelPeriod(period)
											   .withLevels(levels)
											   .build();
		assertNotNull(classUnderTest);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithNoLevels()
	{
		String id = "id";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
													.atLevel(levels[0])
													.build();
		
		Builder.buildTimeScale(id, name, description)
			   .withTopLevelPeriod(period)
			   .build();
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithNoPeriods()
	{
		String id = "id";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		Builder.buildTimeScale(id, name, description)
			   .withLevels(levels)
			   .build();
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBuilderWithPeriodContainingInvalidLevel()
	{
		String id = "id";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimeScaleLevel invalidLevel = new BasicTimeScaleLevel("bad", "bad", BigInteger.TEN, 2);
		
		ITimePeriod subPeriod = BasicTimePeriod.Builder.buildTimePeriod("sub1", "sub1", "sub1")
													   .atLevel(invalidLevel)
													   .build();
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
												    .atLevel(levels[0])
												    .withSubPeriod(subPeriod)
												    .build();
		
		Builder.buildTimeScale(id, name, description)
			   .withLevels(levels)
			   .withTopLevelPeriod(period)
			   .build();
	}
	
	@Test
	public void testHasPeriodWithNull()
	{
		String id = "id";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod subPeriod = BasicTimePeriod.Builder.buildTimePeriod("sub1", "sub1", "sub1")
													   .atLevel(levels[0])
													   .build();
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
												    .atLevel(levels[1])
												    .withSubPeriod(subPeriod)
												    .build();
		
		BasicTimeScale classUnderTest = Builder.buildTimeScale(id, name, description)
											   .withLevels(levels)
											   .withTopLevelPeriod(period)
											   .build();
		
		assertFalse(classUnderTest.hasPeriod(null));
	}
	
	@Test
	public void testHasPeriodWithTopLevelPeriod()
	{
		String id = "id";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod subPeriod = BasicTimePeriod.Builder.buildTimePeriod("sub1", "sub1", "sub1")
													   .atLevel(levels[0])
													   .build();
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
												    .atLevel(levels[1])
												    .withSubPeriod(subPeriod)
												    .build();
		
		BasicTimeScale classUnderTest = Builder.buildTimeScale(id, name, description)
											   .withLevels(levels)
											   .withTopLevelPeriod(period)
											   .build();
		
		assertTrue(classUnderTest.hasPeriod(period));
	}
	
	@Test
	public void testHasPeriodWithSubPeriod()
	{
		String id = "id";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod subPeriod = BasicTimePeriod.Builder.buildTimePeriod("sub1", "sub1", "sub1")
													   .atLevel(levels[0])
													   .build();
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
												    .atLevel(levels[1])
												    .withSubPeriod(subPeriod)
												    .build();
		
		BasicTimeScale classUnderTest = Builder.buildTimeScale(id, name, description)
											   .withLevels(levels)
											   .withTopLevelPeriod(period)
											   .build();
		
		assertTrue(classUnderTest.hasPeriod(subPeriod));
	}
	
	@Test
	public void testHasPeriodWithNonChildPeriod()
	{
		String id = "id";
		String name = "name";
		String description = "description";
		
		ITimeScaleLevel[] levels = new ITimeScaleLevel[] {
			new BasicTimeScaleLevel("level2", "level2", BigInteger.ONE, 1),	
			new BasicTimeScaleLevel("level1", "level1", BigInteger.TEN, 0),	
		};
		
		ITimePeriod subPeriod = BasicTimePeriod.Builder.buildTimePeriod("sub1", "sub1", "sub1")
													   .atLevel(levels[0])
													   .build();
		
		ITimePeriod period = BasicTimePeriod.Builder.buildTimePeriod("period1", "period1", "period1")
												    .atLevel(levels[1])
												    .withSubPeriod(subPeriod)
												    .build();
		
		ITimePeriod nonChildPeriod = BasicTimePeriod.Builder.buildTimePeriod("odd", "odd", "odd")
				   											.atLevel(levels[0])
				   											.build();
		
		BasicTimeScale classUnderTest = Builder.buildTimeScale(id, name, description)
											   .withLevels(levels)
											   .withTopLevelPeriod(period)
											   .build();
		
		assertFalse(classUnderTest.hasPeriod(nonChildPeriod));
	}
	
}
