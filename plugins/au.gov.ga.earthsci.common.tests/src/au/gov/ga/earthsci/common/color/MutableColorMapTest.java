package au.gov.ga.earthsci.common.color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;

/**
 * Unit tests for the {@link MutableColorMap} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class MutableColorMapTest
{

	private Set<String> listenerHits;
	private final PropertyChangeListener testListener = new PropertyChangeListener()
	{

		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			listenerHits.add(evt.getPropertyName());
		}
	};

	@Before
	public void setup()
	{
		listenerHits = new HashSet<String>();
	}

	@Test
	public void testCopyConstructorWithNull()
	{
		ColorMap seed = null;

		MutableColorMap classUnderTest = new MutableColorMap(seed);

		assertNotNull(classUnderTest.getName());
		assertNull(classUnderTest.getDescription());
		assertTrue(classUnderTest.getEntries().isEmpty());
		assertNull(classUnderTest.getNodataColour());
		assertEquals(InterpolationMode.INTERPOLATE_RGB, classUnderTest.getMode());
	}

	@Test
	public void testCopyContructorNonNull()
	{
		ColorMap seed = ColorMaps.getRGBRainbowMap();

		MutableColorMap classUnderTest = new MutableColorMap(seed);

		assertEquals(seed.getName(), classUnderTest.getName());
		assertEquals(seed.getDescription(), classUnderTest.getDescription());
		assertEquals(seed.getNodataColour(), classUnderTest.getNodataColour());
		assertEquals(seed.getMode(), classUnderTest.getMode());
		assertEquals(seed.getEntries(), classUnderTest.getEntries());
	}

	@Test
	public void testChangeToPercentages()
	{
		Map<Double, Color> entries = new HashMap<Double, Color>();
		entries.put(10.0, Color.RED);
		entries.put(15.0, Color.GREEN);
		entries.put(20.0, Color.BLUE);

		MutableColorMap classUnderTest = new MutableColorMap(null, null, entries, null, null, false);
		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.setValuesArePercentages(true, 10, 20);
		assertEquals(Color.RED, classUnderTest.getEntries().get(0.0));
		assertEquals(Color.GREEN, classUnderTest.getEntries().get(0.5));
		assertEquals(Color.BLUE, classUnderTest.getEntries().get(1.0));

		assertListenersHit(MutableColorMap.VALUE_TYPE_CHANGE_EVENT);
	}

	@Test
	public void testChangeToPercentagesNoChange()
	{
		Map<Double, Color> entries = new HashMap<Double, Color>();
		entries.put(0.0, Color.RED);
		entries.put(0.5, Color.GREEN);
		entries.put(1.0, Color.BLUE);

		MutableColorMap classUnderTest = new MutableColorMap(null, null, entries, null, null, true);
		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.setValuesArePercentages(true, 10, 20);
		assertEquals(Color.RED, classUnderTest.getEntries().get(0.0));
		assertEquals(Color.GREEN, classUnderTest.getEntries().get(0.5));
		assertEquals(Color.BLUE, classUnderTest.getEntries().get(1.0));

		assertListenersNotHit(MutableColorMap.VALUE_TYPE_CHANGE_EVENT);
	}

	@Test
	public void testChangeFromPercentages()
	{
		Map<Double, Color> entries = new HashMap<Double, Color>();
		entries.put(0.0, Color.RED);
		entries.put(0.5, Color.GREEN);
		entries.put(1.0, Color.BLUE);

		MutableColorMap classUnderTest = new MutableColorMap(null, null, entries, null, null, true);
		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.setValuesArePercentages(false, 10, 20);
		assertEquals(Color.RED, classUnderTest.getEntries().get(10.0));
		assertEquals(Color.GREEN, classUnderTest.getEntries().get(15.0));
		assertEquals(Color.BLUE, classUnderTest.getEntries().get(20.0));

		assertListenersHit(MutableColorMap.VALUE_TYPE_CHANGE_EVENT);
	}

	@Test
	public void testChangeFromPercentagesNoChange()
	{
		Map<Double, Color> entries = new HashMap<Double, Color>();
		entries.put(0.0, Color.RED);
		entries.put(0.5, Color.GREEN);
		entries.put(1.0, Color.BLUE);

		MutableColorMap classUnderTest = new MutableColorMap(null, null, entries, null, null, false);
		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.setValuesArePercentages(false, 10, 20);
		assertEquals(Color.RED, classUnderTest.getEntries().get(0.0));
		assertEquals(Color.GREEN, classUnderTest.getEntries().get(0.5));
		assertEquals(Color.BLUE, classUnderTest.getEntries().get(1.0));

		assertListenersNotHit(MutableColorMap.VALUE_TYPE_CHANGE_EVENT);
	}

	@Test
	public void testSnapshot()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addEntry(0.0, Color.RED);
		classUnderTest.addEntry(0.5, Color.GREEN);
		classUnderTest.addEntry(0.7, Color.ORANGE);
		classUnderTest.addEntry(1.0, Color.GRAY);
		classUnderTest.setName("testMap");
		classUnderTest.setDescription("testDescription");
		classUnderTest.setMode(InterpolationMode.EXACT_MATCH);
		classUnderTest.setNodataColour(Color.BLUE);

		ColorMap snapshot = classUnderTest.snapshot();

		assertEquals(classUnderTest.getName(), snapshot.getName());
		assertEquals(classUnderTest.getDescription(), snapshot.getDescription());
		assertEquals(classUnderTest.getMode(), snapshot.getMode());
		assertEquals(classUnderTest.getNodataColour(), snapshot.getNodataColour());
		assertEquals(classUnderTest.isPercentageBased(), snapshot.isPercentageBased());
		assertEquals(classUnderTest.getEntries(), snapshot.getEntries());
		assertEquals(classUnderTest, snapshot);
	}

	@Test
	public void testMoveWithValidValue()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addEntry(0.0, Color.RED);
		classUnderTest.addEntry(0.5, Color.GREEN);
		classUnderTest.addEntry(0.7, Color.ORANGE);
		classUnderTest.addEntry(1.0, Color.GRAY);

		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.moveEntry(0.5, 0.8);

		assertEquals(Color.GREEN, classUnderTest.getEntries().get(0.8));
		assertEquals(null, classUnderTest.getEntries().get(0.5));

		assertListenersHit(MutableColorMap.COLOR_MAP_ENTRY_CHANGE_EVENT);
	}

	@Test
	public void testMoveWithInvalidValue()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addEntry(0.0, Color.RED);
		classUnderTest.addEntry(0.5, Color.GREEN);
		classUnderTest.addEntry(0.7, Color.ORANGE);
		classUnderTest.addEntry(1.0, Color.GRAY);

		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.moveEntry(0.3, 0.5);

		assertEquals(null, classUnderTest.getEntries().get(0.3));
		assertEquals(Color.GREEN, classUnderTest.getEntries().get(0.5));

		assertListenersNotHit(MutableColorMap.COLOR_MAP_ENTRY_CHANGE_EVENT);
	}

	@Test
	public void testRemoveWithValidValue()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addEntry(0.0, Color.RED);
		classUnderTest.addEntry(0.5, Color.GREEN);
		classUnderTest.addEntry(0.7, Color.ORANGE);
		classUnderTest.addEntry(1.0, Color.GRAY);

		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.removeEntry(0.5);

		assertEquals(Color.RED, classUnderTest.getEntries().get(0.0));
		assertEquals(null, classUnderTest.getEntries().get(0.5));
		assertEquals(Color.ORANGE, classUnderTest.getEntries().get(0.7));
		assertEquals(Color.GRAY, classUnderTest.getEntries().get(1.0));

		assertListenersHit(MutableColorMap.COLOR_MAP_ENTRY_CHANGE_EVENT);
	}

	@Test
	public void testSetModeWithSame()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.setMode(classUnderTest.getMode());

		assertListenersNotHit(MutableColorMap.MODE_CHANGE_EVENT);
	}

	@Test
	public void testSetModeWithNull()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.setMode(null);

		assertEquals(InterpolationMode.INTERPOLATE_RGB, classUnderTest.getMode());
		assertListenersNotHit(MutableColorMap.MODE_CHANGE_EVENT);

	}

	@Test
	public void testSetModeWithDifferent()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.setMode(InterpolationMode.NEAREST_MATCH);

		assertEquals(InterpolationMode.NEAREST_MATCH, classUnderTest.getMode());
		assertListenersHit(MutableColorMap.MODE_CHANGE_EVENT);
	}

	@Test
	public void testChangeColorWithValidValue()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addEntry(0.0, Color.RED);
		classUnderTest.addEntry(0.5, Color.GREEN);
		classUnderTest.addEntry(0.7, Color.ORANGE);
		classUnderTest.addEntry(1.0, Color.GRAY);

		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.changeColor(0.5, Color.YELLOW);

		assertEquals(Color.YELLOW, classUnderTest.getEntries().get(0.5));
		assertListenersHit(MutableColorMap.COLOR_MAP_ENTRY_CHANGE_EVENT);
	}

	@Test
	public void testChangeColorWithInvalidValue()
	{
		MutableColorMap classUnderTest = new MutableColorMap();
		classUnderTest.addEntry(0.0, Color.RED);
		classUnderTest.addEntry(0.5, Color.GREEN);
		classUnderTest.addEntry(0.7, Color.ORANGE);
		classUnderTest.addEntry(1.0, Color.GRAY);

		classUnderTest.addPropertyChangeListener(testListener);

		classUnderTest.changeColor(0.4, Color.YELLOW);

		assertEquals(null, classUnderTest.getEntries().get(0.4));
		assertListenersNotHit(MutableColorMap.COLOR_MAP_ENTRY_CHANGE_EVENT);
	}

	private void assertListenersHit(String... events)
	{
		for (String s : events)
		{
			assertTrue(listenerHits.contains(s));
		}
	}

	private void assertListenersNotHit(String... events)
	{
		for (String s : events)
		{
			assertFalse(listenerHits.contains(s));
		}
	}
}
