package au.gov.ga.earthsci.worldwind.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.util.GridHelper.GridProperties;

/**
 *	Unit tests for the {@link GridHelper} class
 */
public class GridHelperTest
{
	private static final double ALLOWABLE_ERROR = 0.0001;

	@Test
	public void testSimpleCase()
	{
		GridProperties grid = GridHelper.createGrid().ofSize(10)
													 .toFitIn(100)
													 .forValueRange(new Range<Double>(0d, 100d))
													 .build();
		
		assertNotNull(grid);
		assertEquals(10, grid.getValueChangePerGridLine(), ALLOWABLE_ERROR);
	}
	
	@Test
	public void testSmallRange()
	{
		GridProperties grid = GridHelper.createGrid().ofSize(10)
													 .toFitIn(100)
													 .forValueRange(new Range<Double>(0d, 1.2d))
													 .build();
		
		assertNotNull(grid);
		assertEquals(0.1, grid.getValueChangePerGridLine(), ALLOWABLE_ERROR);
	}
	
	@Test
	public void testSmallRangeWithOffset()
	{
		GridProperties grid = GridHelper.createGrid().ofSize(10)
													 .toFitIn(100)
													 .forValueRange(new Range<Double>(0.05d, 1.2d))
													 .build();
		
		assertNotNull(grid);
		assertEquals(0.1, grid.getValueChangePerGridLine(), ALLOWABLE_ERROR);
	}
	
	@Test
	public void testLargeRange()
	{
		GridProperties grid = GridHelper.createGrid().ofSize(40)
													 .toFitIn(100)
													 .forValueRange(new Range<Double>(100d, 12000d))
													 .build();
		
		assertNotNull(grid);
		assertEquals(2000, grid.getValueChangePerGridLine(), ALLOWABLE_ERROR);
	}
	
}
