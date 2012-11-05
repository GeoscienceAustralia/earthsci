package au.gov.ga.earthsci.worldwind.common.util;

import static au.gov.ga.earthsci.worldwind.test.util.TestUtils.createDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

/**
 * Unit tests for the {@link Range} class
 */
public class RangeTest
{
	
	@Test
	public void testIntegerClosedRange()
	{
		Range<Integer> range = new Range<Integer>(0, 10);
		
		assertTrue(range.isInclusiveLeft());
		assertTrue(range.isInclusiveRight());
		assertFalse(range.isOpenLeft());
		assertFalse(range.isOpenRight());
		
		assertFalse(range.contains(-1));
		assertTrue(range.contains(0));
		assertTrue(range.contains(1));
		assertTrue(range.contains(9));
		assertTrue(range.contains(10));
		assertFalse(range.contains(11));
	}
	
	@Test
	public void testDoubleClosedRange()
	{
		Range<Double> range = new Range<Double>(0d, 10d);
		
		assertTrue(range.isInclusiveLeft());
		assertTrue(range.isInclusiveRight());
		assertFalse(range.isOpenLeft());
		assertFalse(range.isOpenRight());
		
		assertFalse(range.contains(-0.1d));
		assertTrue(range.contains(0d));
		assertTrue(range.contains(1d));
		assertTrue(range.contains(9d));
		assertTrue(range.contains(10d));
		assertFalse(range.contains(10.1d));
	}
	
	@Test
	public void testDateClosedRange()
	{
		Range<Date> range = new Range<Date>(createDate("2010-01-01"), createDate("2010-01-31"));
		
		assertTrue(range.isInclusiveLeft());
		assertTrue(range.isInclusiveRight());
		assertFalse(range.isOpenLeft());
		assertFalse(range.isOpenRight());
		
		assertFalse(range.contains(createDate("2009-12-31")));
		assertTrue(range.contains(createDate("2010-01-01")));
		assertTrue(range.contains(createDate("2010-01-02")));
		assertTrue(range.contains(createDate("2010-01-30")));
		assertTrue(range.contains(createDate("2010-01-31")));
		assertFalse(range.contains(createDate("2010-02-01")));
	}
	
	@Test
	public void testIntegerOpenRange()
	{
		Range<Integer> range = new Range<Integer>(null, 10);
		
		assertFalse(range.isInclusiveLeft());
		assertTrue(range.isInclusiveRight());
		assertTrue(range.isOpenLeft());
		assertFalse(range.isOpenRight());
		
		assertTrue(range.contains(-1));
		assertTrue(range.contains(0));
		assertTrue(range.contains(1));
		assertTrue(range.contains(9));
		assertTrue(range.contains(10));
		assertFalse(range.contains(11));
	}
	
	@Test
	public void testIntegerNonInclusiveRange()
	{
		Range<Integer> range = new Range<Integer>(0, false, 10, false);
		
		assertFalse(range.isInclusiveLeft());
		assertFalse(range.isInclusiveRight());
		assertFalse(range.isOpenLeft());
		assertFalse(range.isOpenRight());
		
		assertFalse(range.contains(-1));
		assertFalse(range.contains(0));
		assertTrue(range.contains(1));
		assertTrue(range.contains(9));
		assertFalse(range.contains(10));
		assertFalse(range.contains(11));
	}
	
	@Test
	public void testIntegerUnionClosedOverlappingRanges()
	{
		Range<Integer> range1 = new Range<Integer>(0, 10);
		Range<Integer> range2 = new Range<Integer>(5, 15);
		
		Range<Integer> union1 = range1.union(range2);
		Range<Integer> union2 = range2.union(range1);
		
		assertEquals(union1, union2);
		
		assertTrue(union1.isInclusiveLeft());
		assertTrue(union1.isInclusiveRight());
		assertFalse(union1.isOpenLeft());
		assertFalse(union1.isOpenRight());
		
		assertFalse(union1.contains(-1));
		assertTrue(union1.contains(0));
		assertTrue(union1.contains(1));
		assertTrue(union1.contains(14));
		assertTrue(union1.contains(15));
		assertFalse(union1.contains(16));
	}
	
	@Test
	public void testIntegerUnionClosedNonoverlappingRanges()
	{
		Range<Integer> range1 = new Range<Integer>(0, 10);
		Range<Integer> range2 = new Range<Integer>(15, 20);
		
		Range<Integer> union1 = range1.union(range2);
		Range<Integer> union2 = range2.union(range1);
		
		assertEquals(union1, union2);
		
		assertTrue(union1.isInclusiveLeft());
		assertTrue(union1.isInclusiveRight());
		assertFalse(union1.isOpenLeft());
		assertFalse(union1.isOpenRight());
		
		assertFalse(union1.contains(-1));
		assertTrue(union1.contains(0));
		assertTrue(union1.contains(1));
		
		assertTrue(union1.contains(13));
		
		assertTrue(union1.contains(19));
		assertTrue(union1.contains(20));
		assertFalse(union1.contains(21));
	}
	
	
	@Test
	public void testIntegerUnionOpenRanges()
	{
		Range<Integer> range1 = new Range<Integer>(null, 10);
		Range<Integer> range2 = new Range<Integer>(5, false, 15, false);
		
		Range<Integer> union1 = range1.union(range2);
		Range<Integer> union2 = range2.union(range1);
		
		assertEquals(union1, union2);
		
		assertFalse(union1.isInclusiveLeft());
		assertFalse(union1.isInclusiveRight());
		assertTrue(union1.isOpenLeft());
		assertFalse(union1.isOpenRight());
		
		assertTrue(union1.contains(-1));
		assertTrue(union1.contains(0));
		assertTrue(union1.contains(1));
		assertTrue(union1.contains(14));
		assertFalse(union1.contains(15));
		assertFalse(union1.contains(16));
	}
	
}
