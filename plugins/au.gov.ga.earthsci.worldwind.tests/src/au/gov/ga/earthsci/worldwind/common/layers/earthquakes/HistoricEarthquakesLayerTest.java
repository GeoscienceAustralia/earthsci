package au.gov.ga.earthsci.worldwind.common.layers.earthquakes;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.layers.earthquakes.HistoricEarthquakesLayer.Earthquake;
import au.gov.ga.earthsci.worldwind.common.util.HSLColor;

/**
 * Unit tests for the {@link HistoricEarthquakesLayer} class
 */
public class HistoricEarthquakesLayerTest
{
	
	private static final int RED_HUE = 0;
	private static final int GREEN_HUE = 120;
	private static final int BLUE_HUE = 240;
	
	private HistoricEarthquakesLayer classUnderTest;
	
	@Before
	public void setup()
	{
		AVList params = new AVListImpl();
		params.setValue(AVKey.URL, "http://dummy/url");
		
		classUnderTest = new HistoricEarthquakesLayer(params );
	}

	@Test
	public void testDateColoring() throws Exception
	{
		List<Earthquake> quakes = new ArrayList<Earthquake>();
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, -100), 5, getMillisForDate("1950-01-01")));
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, -100), 5, getMillisForDate("1970-01-01")));
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, -100), 5, getMillisForDate("1990-01-01")));
		
		FloatBuffer colorBuffer = FloatBuffer.allocate(quakes.size() * 3);
		classUnderTest.generateDateColoring(colorBuffer, quakes);
		colorBuffer.rewind();
		
		List<HSLColor> colors = getColors(colorBuffer);
		
		assertEquals(3, colors.size());
		
		assertEquals(BLUE_HUE, colors.get(0).getHue(), 1); // Old = Blue
		assertEquals(GREEN_HUE, colors.get(1).getHue(), 1); // Middle = Green
		assertEquals(RED_HUE, colors.get(2).getHue(), 1); // Recent = Red
	}
	
	@Test
	public void testDepthColoring() throws Exception
	{
		List<Earthquake> quakes = new ArrayList<Earthquake>();
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, 0), 5, getMillisForDate("1990-01-01")));
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, -100), 5, getMillisForDate("1990-01-01")));
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, -200), 5, getMillisForDate("1990-01-01")));
		
		FloatBuffer colorBuffer = FloatBuffer.allocate(quakes.size() * 3);
		classUnderTest.generateDepthColoring(colorBuffer, quakes);
		colorBuffer.rewind();
		
		List<HSLColor> colors = getColors(colorBuffer);
		
		assertEquals(3, colors.size());
		
		assertEquals(BLUE_HUE, colors.get(0).getHue(), 1); // Shallow = Blue
		assertEquals(GREEN_HUE, colors.get(1).getHue(), 1); // Middle = Green
		assertEquals(RED_HUE, colors.get(2).getHue(), 1); // Deep = Red
	}
	
	@Test
	public void testMagnitudeColoring() throws Exception
	{
		List<Earthquake> quakes = new ArrayList<Earthquake>();
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, 0), 0, getMillisForDate("1990-01-01")));
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, -100), 5, getMillisForDate("1990-01-01")));
		quakes.add(new Earthquake(Position.fromDegrees(100, 100, -200), 10, getMillisForDate("1990-01-01")));
		
		FloatBuffer colorBuffer = FloatBuffer.allocate(quakes.size() * 3);
		classUnderTest.generateMagnitudeColoring(colorBuffer, quakes);
		colorBuffer.rewind();
		
		List<HSLColor> colors = getColors(colorBuffer);
		
		assertEquals(3, colors.size());
		
		assertEquals(BLUE_HUE, colors.get(0).getHue(), 1); // Low magnitude = Blue
		assertEquals(RED_HUE, colors.get(2).getHue(), 1); // High magnitude = Red
	}

	private long getMillisForDate(String string) throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = sdf.parse(string);
		return parsedDate.getTime();
	}
	
	private List<HSLColor> getColors(FloatBuffer colorBuffer)
	{
		ArrayList<HSLColor> result = new ArrayList<HSLColor>();
		while (colorBuffer.hasRemaining())
		{
			Color rgbColor = new Color(colorBuffer.get(), colorBuffer.get(), colorBuffer.get());
			result.add(new HSLColor(rgbColor));
		}
		return result;
	}
	
}
