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
import java.util.Map;

/**
 * A mutable version of the {@link ColorMap} class that allows entries to be
 * manipulated.
 * <p/>
 * In general, it is recommended that {@link ColorMap} be used where
 * appropriate, and that this class only be used where necessary (e.g. where
 * users manipulate color map values etc.)
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class MutableColorMap extends ColorMap
{

	/**
	 * Create a new mutable version of the given colour map
	 * 
	 * @param map
	 *            The map to create a mutable version of
	 */
	public MutableColorMap(ColorMap map)
	{
		this(map.getName(),
				map.getDescription(),
				map.getEntries(),
				map.getNodataColour(),
				map.getMode(),
				map.isPercentageBased());
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
		super(name, description, entries, nodataColour, mode, valuesArePercentages);
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
		entries.put(value, color);
	}

	/**
	 * Remove an entry from the colour map
	 * 
	 * @param value
	 *            The value to remove
	 */
	public void removeEntry(double value)
	{
		entries.remove(value);
	}

	/**
	 * Move an entry from its current value to a new value
	 * 
	 * @param oldValue
	 *            The old value of the entry
	 * @param newValue
	 *            The new value of the entry
	 */
	public void move(double oldValue, double newValue)
	{
		if (!entries.containsKey(oldValue))
		{
			return;
		}

		entries.put(newValue, entries.get(oldValue));
		entries.remove(oldValue);
	}

	/**
	 * Set the interpolation mode on this colour map
	 */
	public void setMode(InterpolationMode mode)
	{
		this.mode = mode == null ? this.mode : mode;
	}

}
