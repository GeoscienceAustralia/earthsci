package au.gov.ga.earthsci.common.math.bezier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import au.gov.ga.earthsci.common.math.bezier.Bezier;
import au.gov.ga.earthsci.common.math.interpolation.BezierInterpolator;
import au.gov.ga.earthsci.common.math.vector.Vector2;

/**
 * Tests for the {@link Bezier} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class BezierTest
{

	private static final double allowableError = 0.01;

	/**
	 * Test the {@link BezierInterpolator} with a 2D vector
	 */
	@Test
	public void testInterpolationV2()
	{
		Vector2 begin = new Vector2(0, 0);
		Vector2 out = new Vector2(0, 1);
		Vector2 in = new Vector2(10, 1);
		Vector2 end = new Vector2(10, 0);

		Bezier<Vector2> classToBeTested = new Bezier<Vector2>(begin, out, in, end);

		// Bezier should pass through start and end points
		assertEquals(0.0, classToBeTested.pointAt(0).x, allowableError);
		assertEquals(0.0, classToBeTested.pointAt(0).y, allowableError);

		assertEquals(10.0, classToBeTested.pointAt(1).x, allowableError);
		assertEquals(0.0, classToBeTested.pointAt(1).y, allowableError);

		// Make sure nothing odd happens in between
		for (double percent = 0; percent <= 1.0; percent += 0.01)
		{
			Vector2 computedValue = classToBeTested.pointAt(percent);
			//System.out.println(percent*10 + ", " + computedValue.y);
		}
	}

}
