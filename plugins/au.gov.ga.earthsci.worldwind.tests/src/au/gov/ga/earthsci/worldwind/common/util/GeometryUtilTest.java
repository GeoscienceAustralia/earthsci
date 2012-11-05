package au.gov.ga.earthsci.worldwind.common.util;

import static org.junit.Assert.*;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Vec4;

import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.util.GeometryUtil;

/**
 * Unit tests for the {@link GeometryUtil} class
 */
public class GeometryUtilTest
{

	@Test
	public void testCreatePlaneFromAxisLines()
	{
		Line line1 = Line.fromSegment(new Vec4(0, 0, 0), new Vec4(1, 0, 0));
		Line line2 = Line.fromSegment(new Vec4(0, 0, 0), new Vec4(0, 1, 0));
		
		Plane result = GeometryUtil.createPlaneContainingLines(line1, line2);
		
		assertNotNull(result);
		
		// Test some intersections on the plane
		testIntersection(result, new Vec4(0, 0, -1), new Vec4(0, 0, 1), new Vec4(0, 0, 0));
		testIntersection(result, new Vec4(0, 10, -1), new Vec4(0, 10, 1), new Vec4(0, 10, 0));
	}
	
	@Test
	public void testCreatePlaneFromParallelLines()
	{
		Line line1 = Line.fromSegment(new Vec4(10, 0, 0), new Vec4(10, 1, 0));
		Line line2 = Line.fromSegment(new Vec4(0, 0, 0), new Vec4(0, 1, 0));
		
		Plane result = GeometryUtil.createPlaneContainingLines(line1, line2);
		
		assertNotNull(result);
		
		// Test some intersections on the plane
		testIntersection(result, new Vec4(0, 0, -1), new Vec4(0, 0, 1), new Vec4(0, 0, 0));
		testIntersection(result, new Vec4(0, 10, -1), new Vec4(0, 10, 1), new Vec4(0, 10, 0));
	}
	
	@Test
	public void testCreatePlaneFromOffAxisLines()
	{
		Line line1 = Line.fromSegment(new Vec4(0, 0, 10), new Vec4(1, 0, 10));
		Line line2 = Line.fromSegment(new Vec4(0, 0, 10), new Vec4(0, 1, 10));
		
		Plane result = GeometryUtil.createPlaneContainingLines(line1, line2);
		
		assertNotNull(result);
		
		// Test some intersections on the plane
		testIntersection(result, new Vec4(0, 0, -1), new Vec4(0, 0, 1), new Vec4(0, 0, 10));
		testIntersection(result, new Vec4(0, 10, -1), new Vec4(0, 10, 1), new Vec4(0, 10, 10));
	}

	private void testIntersection(Plane plane, Vec4 lineStart, Vec4 lineEnd, Vec4 expectedIntersection)
	{
		Line line = Line.fromSegment(lineStart, lineEnd);
		Vec4 intersection = plane.intersect(line);
		assertEquals(expectedIntersection, intersection);
	}

}
