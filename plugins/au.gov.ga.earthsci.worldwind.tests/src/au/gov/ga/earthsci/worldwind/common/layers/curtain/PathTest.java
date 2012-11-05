package au.gov.ga.earthsci.worldwind.common.layers.curtain;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the {@link Path} class
 */
public class PathTest
{
	@Test
	public void testGetBoundingSectorStraightPath()
	{
		Path path = createPath(0d, -90d, 0d, 90d);
		
		Sector expected = new Sector(Angle.ZERO, Angle.ZERO, Angle.NEG90, Angle.POS90);
		Sector result = path.getBoundingSector();
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetBoundingSectorBentPath()
	{
		Path path = createPath(0d, -90d, 0d, 90d, 90d, 90d);
		
		Sector expected = new Sector(Angle.ZERO, Angle.POS90, Angle.NEG90, Angle.POS90);
		Sector result = path.getBoundingSector();
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testGetPercentLatLonStraightPath()
	{
		Path path = createPath(0d, -90d, 0d, 90d);
		
		double percent = 0.5;
		LatLon expected = new LatLon(Angle.ZERO, Angle.ZERO);
		LatLon result = path.getPercentLatLon(percent);
		
		assertLatLonsEqual(expected, result);
	}
	
	@Test
	public void testGetPercentLatLonBentPath()
	{
		Path path = createPath(0d, -90d, 0d, 90d, 180d, 90d);
		
		double percent = 0.25;
		LatLon expected = new LatLon(Angle.ZERO, Angle.ZERO);
		LatLon result = path.getPercentLatLon(percent);
		
		assertLatLonsEqual(expected, result);
	}
	
	@Test
	public void testGetPercentLatLonNegativePercent()
	{
		Path path = createPath(0d, -90d, 0d, 90d, 180d, 90d);
		
		double percent = -0.25;
		LatLon expected = new LatLon(Angle.ZERO, Angle.NEG90);
		LatLon result = path.getPercentLatLon(percent);
		
		assertLatLonsEqual(expected, result);
	}
	
	@Test
	public void testGetPercentLatLonLargerThan100Percent()
	{
		Path path = createPath(0d, -90d, 0d, 90d, 180d, 90d);
		
		double percent = 1.25;
		LatLon expected = new LatLon(Angle.POS180, Angle.POS90);
		LatLon result = path.getPercentLatLon(percent);
		
		assertLatLonsEqual(expected, result);
	}
	
	/**
	 * Creates a path from lat-lon locations specified by the provided angles in degrees in [lat,lon,lat,lon,...] format
	 */
	private Path createPath(Double... degrees)
	{
		List<LatLon> positions = new ArrayList<LatLon>();
		for (int i = 0; i < degrees.length; i+=2)
		{
			positions.add(new LatLon(Angle.fromDegrees(degrees[i]), Angle.fromDegrees(degrees[i+1])));
		}
		return new Path(positions);
	}
	
	private static void assertLatLonsEqual(LatLon expected, LatLon result)
	{
		if (expected == null)
		{
			assertEquals(expected, result);
			return;
		}
		
		assertEquals(expected.latitude.degrees, result.latitude.degrees, 0.0001);
		assertEquals(expected.longitude.degrees, result.longitude.degrees, 0.0001);
	}
}
