package au.gov.ga.earthsci.worldwind.common.util.exaggeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.render.DrawContext;

import java.util.Random;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link DefaultVerticalExaggerationServiceImpl} class
 */
public class DefaultVerticalExaggerationServiceImplTest
{
	
	private static final double DELTA = 0.0001;
	private Mockery mockContext;
	private DrawContext dc;
	private DefaultVerticalExaggerationServiceImpl classUnderTest;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		dc = mockContext.mock(DrawContext.class);
		classUnderTest = new DefaultVerticalExaggerationServiceImpl();
	}
	
	@Test
	public void testApplyWithZeroExaggeration()
	{
		setDrawContextExaggeration(0);
		assertEquals(0d, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testApplyWithPositiveExaggeration()
	{
		setDrawContextExaggeration(10);
		assertEquals(1000d, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testApplyWithNegativeExaggeration()
	{
		setDrawContextExaggeration(-10);
		assertEquals(-1000d, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testApplyWithFractionalExaggeration()
	{
		setDrawContextExaggeration(0.2);
		assertEquals(20, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testGetGlobalExaggeration()
	{
		setDrawContextExaggeration(10);
		assertEquals(10, classUnderTest.getGlobalVerticalExaggeration(dc), DELTA);
	}
	
	@Test
	public void testIsVEChangedWithoutMark()
	{
		setDrawContextExaggeration(0);
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedWithMarkNoChange()
	{
		setDrawContextExaggeration(0);
		classUnderTest.markVerticalExaggeration(this, dc);
		
		setDrawContextExaggeration(0);
		assertFalse(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedWithMarkWithChange()
	{
		setDrawContextExaggeration(0.0);
		classUnderTest.markVerticalExaggeration(this, dc);
		
		changeDrawContextExaggeration(0.1);
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testIsVEChangedAfterClear()
	{
		setDrawContextExaggeration(0.0);
		classUnderTest.markVerticalExaggeration(this, dc);
		classUnderTest.clearMark(this);
		
		assertTrue(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	@Test
	public void testCheckAndMarkWithNoChange()
	{
		setDrawContextExaggeration(0.0);
		classUnderTest.markVerticalExaggeration(this, dc);
		
		changeDrawContextExaggeration(0.1);
		assertTrue(classUnderTest.checkAndMarkVerticalExaggeration(this, dc));
		
		assertFalse(classUnderTest.isVerticalExaggerationChanged(this, dc));
	}
	
	private void setDrawContextExaggeration(final double exaggeration)
	{
		mockContext.checking(new Expectations(){{
			allowing(dc).getVerticalExaggeration(); will(returnValue(exaggeration));
		}});
	}
	
	private void changeDrawContextExaggeration(final double exaggeration)
	{
		Random rand = new Random();
		dc = mockContext.mock(DrawContext.class, "newContext" + rand.nextInt());
		setDrawContextExaggeration(exaggeration);
	}
	
}
