/*******************************************************************************
 * Copyright 2013 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.common.color;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A mutable version of the {@link ColorMap} class that allows entries to be
 * manipulated.
 * <p/>
 * In general, it is recommended that {@link ColorMap} be used where
 * appropriate, and that this class only be used where necessary (e.g. where
 * users manipulate color map values etc.)
 * <p/>
 * <b>Events:</b>
 * <dl>
 * <dt>{@value #COLOR_MAP_ENTRY_CHANGE_EVENT}</dt>
 * <dd>Fired when a colour map entry changes. The contents of the event will
 * include the value that has changed.</dt>
 * <dt>{@value #ENTRY_ADDED_EVENT}</dt>
 * <dd>Fired when a new entry is added to the colour map. Value will the be
 * added {@link Entry}.</dd>
 * <dt>{@value #ENTRY_REMOVED_EVENT}</dt>
 * <dd>Fired when a new entry is added to the colour map. Value will be the
 * removed {@link Entry}</dd>
 * <dt>{@value #ENTRY_MOVED_EVENT}</dt>
 * <dd>Fired when an entry is moved in the colour map. Value will be the moved
 * {@link Entry}</dd>
 * <dt>{@value #COLOR_CHANGED_EVENT}</dt>
 * <dd>Fired when a new entry is added to the colour map. Value will be the
 * changed {@link Entry}</dd>
 * <dt>{@value #MODE_CHANGE_EVENT}</dt>
 * <dd>Fired when the interpolation mode changes</dd>
 * <dt>{@value #VALUE_TYPE_CHANGE_EVENT}</dt>
 * <dd>Fired when the the value type is changed between absolute and percentage
 * based</dd>
 * <dt>{@value #NAME_CHANGE_EVENT}</dt>
 * <dd>Fired when the name for this map changes</dd>
 * <dt>{@value #DESCRIPTION_CHANGE_EVENT}</dt>
 * <dd>Fired when the description for this map changes</dd>
 * <dt>{@value #NODATA_CHANGE_EVENT}</dt>
 * <dd>Fired when the NODATA colour of this map changes</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class MutableColorMap extends ColorMap
{

	/** The event fired when the colour map changes */
	public static final String COLOR_MAP_ENTRY_CHANGE_EVENT = "colorMapEntry"; //$NON-NLS-1$

	/** The event fired when an entry is added to the map */
	public static final String ENTRY_ADDED_EVENT = "entryAdded"; //$NON-NLS-1$

	/** The event fired when an entry is removed from the map */
	public static final String ENTRY_REMOVED_EVENT = "entryRemoved"; //$NON-NLS-1$

	/** The event fired when an entry is moved in the map */
	public static final String ENTRY_MOVED_EVENT = "entryMoved"; //$NON-NLS-1$

	/** The event fired when an entry colour changes in the map */
	public static final String COLOR_CHANGED_EVENT = "colorChanged"; //$NON-NLS-1$

	/** The event fired when the mode changes */
	public static final String MODE_CHANGE_EVENT = "mode"; //$NON-NLS-1$

	/**
	 * The event fired when the value type is changed between absolute and
	 * percentage based
	 */
	public static final String VALUE_TYPE_CHANGE_EVENT = "valueType"; //$NON-NLS-1$

	/** The event fired when the map name changes */
	public static final String NAME_CHANGE_EVENT = "name"; //$NON-NLS-1$

	/** The event fired when the description changes */
	public static final String DESCRIPTION_CHANGE_EVENT = "description"; //$NON-NLS-1$

	/** The event fired when the nodata value changes */
	public static final String NODATA_CHANGE_EVENT = "nodata"; //$NON-NLS-1$

	final private PropertyChangeSupport propertyChange = new PropertyChangeSupport(this);

	private ReadWriteLock entriesLock = new ReentrantReadWriteLock();

	/**
	 * Create a new empty mutable colour map
	 */
	public MutableColorMap()
	{
		this((ColorMap) null);
	}

	/**
	 * Create a new mutable version of the given colour map
	 * 
	 * @param map
	 *            The map to create a mutable version of. If <code>null</code>,
	 *            creates an empty, unnamed color map with no entries.
	 */
	public MutableColorMap(ColorMap map)
	{
		this(map == null ? null : map.getName(),
				map == null ? null : map.getDescription(),
				map == null ? null : map.getEntries(),
				map == null ? null : map.getNodataColour(),
				map == null ? null : map.getMode(),
				map == null ? false : map.isPercentageBased());
	}

	/**
	 * @see ColorMap#ColorMap(Map)
	 */
	public MutableColorMap(Map<Double, Color> entries)
	{
		super(entries);
	}

	/**
	 * @see ColorMap#ColorMap(String, String, Map, Color, InterpolationMode,
	 *      boolean)
	 */
	public MutableColorMap(String name, String description, Map<Double, Color> entries, Color nodataColour,
			InterpolationMode mode, boolean valuesArePercentages)
	{
		super(name,
				description,
				entries == null ? new HashMap<Double, Color>() : entries,
				nodataColour,
				mode,
				valuesArePercentages);
	}

	@Override
	public Color getColor(double value)
	{
		entriesLock.readLock().lock();
		try
		{
			return super.getColor(value);
		}
		finally
		{
			entriesLock.readLock().unlock();
		}
	}

	@Override
	public Color getColor(double absoluteValue, double min, double max)
	{
		entriesLock.readLock().lock();
		try
		{
			return super.getColor(absoluteValue, min, max);
		}
		finally
		{
			entriesLock.readLock().unlock();
		}
	}

	/**
	 * Add a new entry to the colour map
	 * 
	 * @param value
	 *            The value to add a colour at
	 * @param color
	 *            The colour to add at that value
	 */
	public void addEntry(double value, Color color)
	{
		Entry<Double, Color> entry = null;
		entriesLock.writeLock().lock();
		try
		{
			entries.put(value, color);
			entry = getEntry(value);
		}
		finally
		{
			entriesLock.writeLock().unlock();
		}

		propertyChange.firePropertyChange(ENTRY_ADDED_EVENT, null, entry);
		propertyChange.firePropertyChange(COLOR_MAP_ENTRY_CHANGE_EVENT, null, value);
	}

	/**
	 * Remove an entry from the colour map
	 * 
	 * @param value
	 *            The value to remove
	 */
	public void removeEntry(double value)
	{
		Entry<Double, Color> entry = null;
		entriesLock.writeLock().lock();
		try
		{
			entry = getEntry(value);
			entries.remove(value);
		}
		finally
		{
			entriesLock.writeLock().unlock();
		}
		propertyChange.firePropertyChange(ENTRY_REMOVED_EVENT, entry, null);
		propertyChange.firePropertyChange(COLOR_MAP_ENTRY_CHANGE_EVENT, value, null);
	}

	/**
	 * Move an entry from its current value to a new value
	 * 
	 * @param oldValue
	 *            The old value of the entry
	 * @param newValue
	 *            The new value of the entry
	 */
	public void moveEntry(double oldValue, double newValue)
	{
		if (oldValue == newValue)
		{
			return;
		}

		entriesLock.readLock().lock();
		try
		{
			if (!entries.containsKey(oldValue))
			{
				return;
			}
		}
		finally
		{
			entriesLock.readLock().unlock();
		}

		Entry<Double, Color> oldEntry = null;
		Entry<Double, Color> newEntry = null;

		entriesLock.writeLock().lock();
		try
		{
			oldEntry = getEntry(oldValue);

			entries.put(newValue, oldEntry.getValue());
			entries.remove(oldValue);

			newEntry = getEntry(newValue);
		}
		finally
		{
			entriesLock.writeLock().unlock();
		}

		propertyChange.firePropertyChange(ENTRY_MOVED_EVENT, oldEntry, newEntry);
		propertyChange.firePropertyChange(COLOR_MAP_ENTRY_CHANGE_EVENT, oldValue, newValue);
	}

	/**
	 * Change the colour associated with the given value (if there is one)
	 * 
	 * @param value
	 *            The value to change the colour for
	 * @param newColor
	 *            The colour to change to
	 */
	public void changeColor(double value, Color newColor)
	{
		Entry<Double, Color> oldEntry = null;
		Entry<Double, Color> newEntry = null;
		entriesLock.writeLock().lock();
		try
		{
			if (!entries.containsKey(value))
			{
				return;
			}
			oldEntry = getEntry(value);
			entries.put(value, newColor);
			newEntry = getEntry(value);
		}
		finally
		{
			entriesLock.writeLock().unlock();
		}
		propertyChange.firePropertyChange(COLOR_CHANGED_EVENT, oldEntry, newEntry);
		propertyChange.firePropertyChange(COLOR_MAP_ENTRY_CHANGE_EVENT, value, null);
	}

	/**
	 * Set the interpolation mode on this colour map
	 */
	public void setMode(InterpolationMode mode)
	{
		propertyChange.firePropertyChange(MODE_CHANGE_EVENT, this.mode, this.mode = mode == null ? this.mode : mode);
	}

	/**
	 * Set whether values are to be interpreted as percentages
	 * 
	 * @param valuesArePercentages
	 *            Whether values are to be interpreted as percentages
	 */
	public void setValuesArePercentages(boolean valuesArePercentages, double minValue, double maxValue)
	{
		if (this.valuesArePercentages == valuesArePercentages)
		{
			return;
		}

		this.valuesArePercentages = valuesArePercentages;

		Map<Double, Color> newMap = new HashMap<Double, Color>();

		// Adjust the values in the map to change to/from percentage values as required
		for (Entry<Double, Color> e : entries.entrySet())
		{
			Double newValue = null;
			if (valuesArePercentages)
			{
				newValue = toPercentage(e.getKey(), minValue, maxValue);
			}
			else
			{
				newValue = fromPercentage(e.getKey(), minValue, maxValue);
			}
			newMap.put(newValue, e.getValue());
		}

		entriesLock.writeLock().lock();
		try
		{
			entries.clear();
			entries.putAll(newMap);
		}
		finally
		{
			entriesLock.writeLock().unlock();
		}

		propertyChange.firePropertyChange(VALUE_TYPE_CHANGE_EVENT, !valuesArePercentages, valuesArePercentages);
	}

	private static double toPercentage(double value, double minValue, double maxValue)
	{
		return (value - minValue) / (maxValue - minValue);
	}

	private static double fromPercentage(double percentage, double minValue, double maxValue)
	{
		return percentage * (maxValue - minValue) + minValue;
	}

	/**
	 * Set the name on this color map
	 */
	public void setName(String name)
	{
		propertyChange.firePropertyChange(NAME_CHANGE_EVENT, this.name,
				this.name = name == null ? createDefaultName() : name);
	}

	/**
	 * Set the description on this color map
	 */
	public void setDescription(String description)
	{
		propertyChange.firePropertyChange(DESCRIPTION_CHANGE_EVENT, this.description, this.description = description);
	}

	/**
	 * Set the nodata colour on this map
	 */
	public void setNodataColour(Color nodata)
	{
		propertyChange.firePropertyChange(NODATA_CHANGE_EVENT, this.nodataColour, this.nodataColour = nodata);
	}

	/**
	 * Create an immutable snapshot of this mutable map in its current state
	 * 
	 * @return a new immutable snapshot of this map in its current state
	 */
	public ColorMap snapshot()
	{
		entriesLock.readLock().lock();
		try
		{
			return new ColorMap(name, description, entries, nodataColour, mode, valuesArePercentages);
		}
		finally
		{
			entriesLock.readLock().unlock();
		}
	}

	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChange.addPropertyChangeListener(listener);
	}

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		propertyChange.removePropertyChangeListener(listener);
	}

	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(String,
	 *      PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		propertyChange.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(String,
	 *      PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		propertyChange.removePropertyChangeListener(propertyName, listener);
	}


}
