package au.gov.ga.earthsci.model.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the {@link ModelGeometryStatistics} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ModelGeometryStatisticsTest
{

	private ModelGeometryStatistics classUnderTest;

	@Test
	public void testEmptyConstructor()
	{
		classUnderTest = new ModelGeometryStatistics();

		assertStatsCorrect(null, null, null, null, null, null);
	}

	@Test
	public void testSeedingConstructor()
	{
		classUnderTest = new ModelGeometryStatistics(1.0, 2.0, 3.0);

		assertStatsCorrect(1.0, 1.0, 2.0, 2.0, 3.0, 3.0);
	}

	@Test
	public void testInitialisingConstructor()
	{
		classUnderTest = new ModelGeometryStatistics(1.0, 1.1, 2.0, 2.1, 3.0, 3.1);

		assertStatsCorrect(1.0, 1.1, 2.0, 2.1, 3.0, 3.1);
	}

	@Test
	public void testInitialisingConstructorWithSwitchedValues()
	{
		classUnderTest = new ModelGeometryStatistics(1.1, 1.0, 2.1, 2.0, 3.1, 3.0);

		assertStatsCorrect(1.0, 1.1, 2.0, 2.1, 3.0, 3.1);
	}

	@Test
	public void testInitialUpdateFromNulls()
	{
		classUnderTest = new ModelGeometryStatistics();
		classUnderTest.updateStats(1.0, 2.0, 3.0);

		assertStatsCorrect(1.0, 1.0, 2.0, 2.0, 3.0, 3.0);
	}

	@Test
	public void testFinalValueAfterChainedUpdates()
	{
		classUnderTest = new ModelGeometryStatistics(1.0, 2.0, 3.0);


		classUnderTest.updateStats(1.1, 1.5, 3.0);
		classUnderTest.updateStats(1.0, 2.0, 3.0);
		classUnderTest.updateStats(0.9, 2.9, 3.0);

		assertStatsCorrect(0.9, 1.1, 1.5, 2.9, 3.0, 3.0);
	}

	private void assertStatsCorrect(Double minLat, Double maxLat,
			Double minLon, Double maxLon,
			Double minElevation, Double maxElevation)
	{
		assertEquals(minLat, classUnderTest.getMinLat());
		assertEquals(maxLat, classUnderTest.getMaxLat());
		assertEquals(minLon, classUnderTest.getMinLon());
		assertEquals(maxLon, classUnderTest.getMaxLon());
		assertEquals(minElevation, classUnderTest.getMinElevation());
		assertEquals(maxElevation, classUnderTest.getMaxElevation());
	}
}
