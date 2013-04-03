/**
 * 
 */
package au.gov.ga.earthsci.core.math.interpolation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import au.gov.ga.earthsci.core.math.vector.TimeVector;
import au.gov.ga.earthsci.core.math.vector.Vector1;
import au.gov.ga.earthsci.core.math.vector.Vector2;
import au.gov.ga.earthsci.core.math.vector.Vector3;


/**
 * Unit tests for the {@link BezierInterpolator} class
 * 
 * TODO: Record sampled values from external and compare with calculated vectors
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class BezierInterpolatorTest
{
	/** The error allowed in calculations to consider them correct */
	private static final double allowableError = 0.01;

	/**
	 * Test the {@link BezierInterpolator} with a 1D vector
	 */
	@Test
	public void testInterpolationV1()
	{
		Vector1 begin = new Vector1(0);
		Vector1 out = new Vector1(1);
		Vector1 in = new Vector1(9);
		Vector1 end = new Vector1(10);

		BezierInterpolator<Vector1> classToBeTested = new BezierInterpolator<Vector1>(begin, out, in, end);

		// Bezier should pass through start and end points
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(0).x);
		assertEqualsWithError(10.0, allowableError, classToBeTested.computeValue(1).x);

		// Make sure nothing odd happens in between
		for (double percent = 0; percent <= 1.0; percent += 0.1)
		{
			classToBeTested.computeValue(percent);
		}
	}


	/**
	 * Test the {@link BezierInterpolator} with a 2D vector
	 */
	@Test
	public void testInterpolationV2()
	{
		Vector2 begin = new Vector2(0, 0);
		Vector2 out = new Vector2(0, 100);
		Vector2 in = new Vector2(10, 100);
		Vector2 end = new Vector2(10, 0);

		BezierInterpolator<Vector2> classToBeTested = new BezierInterpolator<Vector2>(begin, out, in, end);

		// Bezier should pass through start and end points
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(0).x);
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(0).y);

		assertEqualsWithError(10.0, allowableError, classToBeTested.computeValue(1).x);
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(1).y);

		// Make sure nothing odd happens in between
		for (double percent = 0; percent <= 1.0; percent += 0.01)
		{
			Vector2 computedValue = classToBeTested.computeValue(percent);
			//System.out.println(percent*10 + ", " + computedValue.y);
		}
	}

	/**
	 * Test the {@link BezierInterpolator} with a 2D time vector
	 */
	@Test
	public void testInterpolationTimeVector()
	{
		TimeVector begin = new TimeVector(0, 0);
		TimeVector out = new TimeVector(0, 100);
		TimeVector in = new TimeVector(10, 100);
		TimeVector end = new TimeVector(10, 0);

		BezierInterpolator<Vector2> classToBeTested = new BezierInterpolator<Vector2>(begin, out, in, end);

		// Bezier should pass through start and end points
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(0).x);
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(0).y);

		assertEqualsWithError(10.0, allowableError, classToBeTested.computeValue(1).x);
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(1).y);

		// Make sure nothing odd happens in between
		for (double percent = 0; percent <= 1.0; percent += 0.01)
		{
			Vector2 computedValue = classToBeTested.computeValue(percent);
			//System.out.println(percent*10 + ", " + computedValue.y);
		}
	}

	/**
	 * Test the {@link BezierInterpolator} with a 3D vector
	 */
	@Test
	public void testInterpolationV3()
	{
		Vector3 begin = new Vector3(0, 0, 0);
		Vector3 out = new Vector3(0, 1, 1);
		Vector3 in = new Vector3(10, 1, 2);
		Vector3 end = new Vector3(10, 0, 3);

		BezierInterpolator<Vector3> classToBeTested = new BezierInterpolator<Vector3>(begin, out, in, end);

		// Bezier should pass through start and end points
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(0).x);
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(0).y);
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(0).z);

		assertEqualsWithError(10.0, allowableError, classToBeTested.computeValue(1).x);
		assertEqualsWithError(0.0, allowableError, classToBeTested.computeValue(1).y);
		assertEqualsWithError(3.0, allowableError, classToBeTested.computeValue(1).z);

		// Make sure nothing odd happens in between
		for (double percent = 0; percent <= 1.0; percent += 0.1)
		{
			classToBeTested.computeValue(percent);
		}
	}

	/**
	 * Asset that the provided double value is within an allowable error margin
	 * of the expected value
	 */
	private static void assertEqualsWithError(double expected, double allowableError, double actual)
	{
		assertEquals(expected, actual, allowableError);
	}

}
