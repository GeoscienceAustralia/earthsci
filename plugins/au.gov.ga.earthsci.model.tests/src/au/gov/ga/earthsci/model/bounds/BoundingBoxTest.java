package au.gov.ga.earthsci.model.bounds;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import au.gov.ga.earthsci.common.math.vector.Vector3;

/**
 * Unit tests for the {@link BoundingBox} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BoundingBoxTest
{

	private final Vector3[] allIn = new Vector3[] {
			new Vector3(0, 0, 0),
			new Vector3(1, 1, 1),
			new Vector3(5, 5, 5),
			new Vector3(10, 10, 10),
	};

	private final Vector3[] oneOut = new Vector3[] {
			new Vector3(0, 0, 0),
			new Vector3(1, 1, 1),
			new Vector3(5, 5, 5),
			new Vector3(11, 10, 10),
	};

	private final Vector3[] allOut = new Vector3[] {
			new Vector3(-10, 0, 0),
			new Vector3(-1, 1, 1),
			new Vector3(-5, 5, 5),
			new Vector3(-10, 10, 10),
	};

	private BoundingBox classUnderTest = new BoundingBox(0, 10, 0, 10, 0, 10, true);

	@Test
	public void testContainsWithNull()
	{
		assertFalse(classUnderTest.contains(null));
	}

	@Test
	public void testContainsWithIn()
	{
		assertTrue(classUnderTest.contains(new Vector3(5, 5, 5)));
	}

	@Test
	public void testContainsWithOut()
	{
		assertFalse(classUnderTest.contains(new Vector3(-5, 5, 5)));
	}

	@Test
	public void testContainsWithBoundary()
	{
		assertTrue(classUnderTest.contains(new Vector3(0, 0, 0)));
	}

	@Test
	public void testContainsAllWithAllNull()
	{
		assertFalse(classUnderTest.containsAll((Vector3) null));
	}

	@Test
	public void testContainsAllWithAllEmpty()
	{
		assertFalse(classUnderTest.containsAll(new Vector3[0]));
	}

	@Test
	public void testContainsAllWithAllIn()
	{
		assertTrue(classUnderTest.containsAll(allIn));
	}

	@Test
	public void testContainsAllWithOneOut()
	{
		assertFalse(classUnderTest.containsAll(oneOut));
	}

	@Test
	public void testContainsAllWithAllOut()
	{
		assertFalse(classUnderTest.containsAll(allOut));
	}

	@Test
	public void testContainsAnyWithAllNull()
	{
		assertFalse(classUnderTest.containsAny((Vector3) null));
	}

	@Test
	public void testContainsAnyWithAllEmpty()
	{
		assertFalse(classUnderTest.containsAny(new Vector3[0]));
	}

	@Test
	public void testContainsAnyWithAllIn()
	{
		assertTrue(classUnderTest.containsAny(allIn));
	}

	@Test
	public void testContainsAnyWithOneOut()
	{
		assertTrue(classUnderTest.containsAny(oneOut));
	}

	@Test
	public void testContainsAnyWithAllOut()
	{
		assertFalse(classUnderTest.containsAny(allOut));
	}

}
