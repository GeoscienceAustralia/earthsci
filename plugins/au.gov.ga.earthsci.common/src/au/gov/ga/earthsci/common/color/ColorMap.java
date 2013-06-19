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
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.INamed;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * An immutable colour map used for mapping values to colours according to a
 * colour table
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 * 
 * @see au.gov.ga.earthsci.worldwind.common.util.ColorMap
 */
public class ColorMap implements INamed, IDescribed
{

	public static enum InterpolationMode implements INamed, IDescribed
	{
		/**
		 * Return the colour for the nearest matching value in the colour table
		 * to the value provided
		 * <p/>
		 * Useful for discretising continuous data.
		 */
		NEAREST_MATCH
		{
			@Override
			protected Color getColor(double value, TreeMap<Double, Color> entries, Color nodata)
			{
				Entry<Double, Color> ceiling = entries.ceilingEntry(value);
				Entry<Double, Color> floor = entries.floorEntry(value);

				if (ceiling == null && floor == null)
				{
					return nodata;
				}
				if (ceiling == null && floor != null)
				{
					return floor.getValue();
				}
				if (ceiling != null && floor == null)
				{
					return ceiling.getValue();
				}
				if (Math.abs(ceiling.getKey() - value) > Math.abs(floor.getKey() - value))
				{
					return floor.getValue();
				}
				return ceiling.getValue();
			}

			@Override
			public String getName()
			{
				return Messages.ColorMap_NearestMatchName;
			}

			@Override
			public String getDescription()
			{
				return Messages.ColorMap_NearestMatchDescription;
			}
		},

		/**
		 * Return the colour for the value in the colour table that exactly
		 * matches the value provided, or NODATA if none is found.
		 * <p/>
		 * Useful for classification colouring of discrete data.
		 */
		EXACT_MATCH
		{
			@Override
			protected Color getColor(double value, TreeMap<Double, Color> entries, Color nodata)
			{
				Color result = entries.get(value);
				if (result == null)
				{
					return nodata;
				}
				return result;
			}

			@Override
			public String getName()
			{
				return Messages.ColorMap_ExactMatchName;
			}

			@Override
			public String getDescription()
			{
				return Messages.ColorMap_ExactMatchDescription;
			}
		},

		/**
		 * Interpolate between the two closest colour entries by separately
		 * interpolating the RGB components of each colour.
		 */
		INTERPOLATE_RGB
		{
			@Override
			protected Color getColor(double value, TreeMap<Double, Color> entries, Color nodata)
			{
				return doInterpolate(value, entries, nodata, false);
			}

			@Override
			public String getName()
			{
				return Messages.ColorMap_RGBInterpolateName;
			}

			@Override
			public String getDescription()
			{
				return Messages.ColorMap_RGBInterpolateDescription;
			}
		},

		/**
		 * Interpolate between the two closest colour entries by interpolating
		 * along in HSB space, wrapping around the Hue axis where necessary, and
		 * converting back to RGB.
		 */
		INTERPOLATE_HUE
		{
			@Override
			protected Color getColor(double value, TreeMap<Double, Color> entries, Color nodata)
			{
				return doInterpolate(value, entries, nodata, true);
			}

			@Override
			public String getName()
			{
				return Messages.ColorMap_HueInterpolateName;
			}

			@Override
			public String getDescription()
			{
				return Messages.ColorMap_HueInterpolateDescription;
			}
		};

		protected abstract Color getColor(double value, TreeMap<Double, Color> entries, Color nodata);

		private static Color doInterpolate(double value, TreeMap<Double, Color> entries, Color nodata, boolean hue)
		{
			Entry<Double, Color> floor = entries.floorEntry(value);
			Entry<Double, Color> ceiling = entries.ceilingEntry(value);

			if (floor == null && ceiling == null)
			{
				return nodata;
			}

			double mixer = 0;
			if (floor != null && ceiling != null)
			{
				double window = ceiling.getKey() - floor.getKey();
				if (window > 0)
				{
					mixer = (value - floor.getKey()) / window;
				}
			}
			Color floorColor = floor == null ? null : floor.getValue();
			Color ceilingColor = ceiling == null ? null : ceiling.getValue();
			return Util.interpolateColor(floorColor, ceilingColor, mixer, hue);
		}
	}

	private static final Color DEFAULT_NODATA = new Color(0, 0, 0, 0);

	// Note that package-private is used to restrict subclassing to the MutableColorMap implementation

	Color nodataColour;

	boolean valuesArePercentages;

	InterpolationMode mode;

	final TreeMap<Double, Color> entries = new TreeMap<Double, Color>();

	String name;

	String description;

	/**
	 * Create a new colour map using the provided entries. The instance will use
	 * RGB interpolation, will return {@code RGB(0,0,0,0)} for NODATA values and
	 * use absolute values as map entries.
	 * 
	 * @param entries
	 *            The colour map entries to use
	 */
	public ColorMap(Map<Double, Color> entries)
	{
		this(null, null, entries, DEFAULT_NODATA, InterpolationMode.INTERPOLATE_RGB, false);
	}

	/**
	 * Create a new fully configured colour map.
	 * 
	 * 
	 * @param name
	 *            The (localised) human-readable name for the colour map.
	 *            (Optional - if missing will use an auto-generated name).
	 * @param description
	 *            The (localised) human-readable description for the colour map
	 *            (Optional - if missing will have no description).
	 * @param entries
	 *            The colour map entries to use (Required).
	 * @param nodataColour
	 *            The nodata colour to associate with this map (Optional - if
	 *            missing will return <code>null</code> for nodata).
	 * @param mode
	 *            The interpolation mode for this map (Optional - if missing
	 *            will default to {@link InterpolationMode#INTERPOLATE_RGB})
	 * @param valuesArePercentages
	 *            Whether the map uses percentages (<code>true</code>) or
	 *            absolute values (<code>true</code>).
	 */
	public ColorMap(String name, String description,
			Map<Double, Color> entries, Color nodataColour,
			InterpolationMode mode, boolean valuesArePercentages)
	{
		if (entries != null)
		{
			this.entries.putAll(entries);
		}
		this.nodataColour = nodataColour;
		this.mode = mode == null ? InterpolationMode.INTERPOLATE_RGB : mode;
		this.valuesArePercentages = valuesArePercentages;
		this.name = name == null ? createDefaultName() : name;
		this.description = description;
	}

	final static String createDefaultName()
	{
		return Messages.ColorMap_DefaultColorMapName + UUID.randomUUID().toString();
	}

	/**
	 * Return the colour for the given value, using the appropriate
	 * interpolation mode.
	 * <p/>
	 * If {@link #isPercentageBased()}, expects a percentage value in the range
	 * {@code [0,1]} as input. Otherwise expects an absolute value.
	 * 
	 * @param value
	 *            The value to lookup in the map
	 * 
	 * @return The appropriate colour for the given value
	 */
	public Color getColor(double value)
	{
		return mode.getColor(value, entries, nodataColour);
	}

	/**
	 * Return the colour for the given absolute value, using the appropriate
	 * interpolation mode.
	 * <p/>
	 * If {@link #isPercentageBased()}, will calculate a percentage to use based
	 * on the {@code min} and {@code max} values. Otherwise will use the
	 * absolute value directly.
	 * 
	 * @param absoluteValue
	 *            The absolute data value to look up
	 * @param min
	 *            The minimum absolute value in the source data
	 * @param max
	 *            The maximum absolute value in the source data
	 * 
	 * @return The appropriate colour to use
	 */
	public Color getColor(double absoluteValue, double min, double max)
	{
		if (valuesArePercentages)
		{
			double percentage = (absoluteValue - Math.min(min, max)) / (Math.max(min, max) - Math.min(min, max));
			return getColor(percentage);
		}

		return getColor(absoluteValue);
	}

	/**
	 * @return the NODATA colour for this colour map
	 */
	public Color getNodataColour()
	{
		return nodataColour;
	}

	/**
	 * Return whether this colour map uses percentages in the range
	 * {@code [0,1]} rather than absolute values as keys in the map.
	 * 
	 * @return <code>true</code> if values are interpreted as percentages;
	 *         <code>false</code> otherwise.
	 */
	public boolean isPercentageBased()
	{
		return valuesArePercentages;
	}

	/**
	 * Return the interpolation mode being used by this colour map
	 * 
	 * @return the mode
	 */
	public InterpolationMode getMode()
	{
		return mode;
	}

	/**
	 * Return the entries in this colour map.
	 * 
	 * @return a read-only view of the entries in this colour map
	 */
	public Map<Double, Color> getEntries()
	{
		return Collections.unmodifiableMap(entries);
	}

	/**
	 * Return the number of entries in this colour map
	 * 
	 * @return The number of entries in this colour map
	 */
	public int getSize()
	{
		return entries.size();
	}

	/**
	 * Return whether this map is empty
	 * 
	 * @return <code>true</code> if this map contains no entries;
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmpty()
	{
		return entries.isEmpty();
	}

	/**
	 * Return the first entry in this colour map, if one exists
	 * 
	 * @return The first entry in this colour map
	 */
	public Entry<Double, Color> getFirstEntry()
	{
		return entries.firstEntry();
	}

	/**
	 * Return the last entry in this colour map, if one exists
	 * 
	 * @return The last entry in this colour map
	 */
	public Entry<Double, Color> getLastEntry()
	{
		return entries.lastEntry();
	}

	/**
	 * Return the entry in the colour map for the next lowest value to the one
	 * given.
	 * 
	 * @param value
	 *            The value to retrieve the previous entry from
	 * 
	 * @return The previous entry, or <code>null</code> if none exists in the
	 *         map.
	 */
	public Entry<Double, Color> getPreviousEntry(Double value)
	{
		if (value == null)
		{
			return null;
		}
		return entries.lowerEntry(value);
	}

	/**
	 * Return the entry in the colour map for the next highest value to the one
	 * given.
	 * 
	 * @param value
	 *            The value to retrieve the next entry from
	 * 
	 * @return The next entry, or <code>null</code> if none exists in the map.
	 */
	public Entry<Double, Color> getNextEntry(Double value)
	{
		if (value == null)
		{
			return null;
		}
		return entries.higherEntry(value);
	}

	/**
	 * Return the nearest entry in the map to the provided value, or
	 * <code>null</code> if there are no entries in the map.
	 * 
	 * @param value
	 *            The value to find the nearest entry for
	 * 
	 * @return The nearest entry to the provided value, or <code>null</code> if
	 *         none exists
	 */
	public Entry<Double, Color> getNearestEntry(Double value)
	{
		if (value == null)
		{
			return null;
		}
		Entry<Double, Color> ceiling = entries.ceilingEntry(value);
		Entry<Double, Color> floor = entries.floorEntry(value);

		if (ceiling == null && floor == null)
		{
			return null;
		}
		if (ceiling == null && floor != null)
		{
			return floor;
		}
		if (ceiling != null && floor == null)
		{
			return ceiling;
		}
		if (Math.abs(ceiling.getKey() - value) > Math.abs(floor.getKey() - value))
		{
			return floor;
		}
		return ceiling;
	}

	/**
	 * Return the entry with the given value, if one exists.
	 * 
	 * @param value
	 *            The value to retrieve the entry for
	 * 
	 * @return The entry for the given value, or <code>null</code> if one cannot
	 *         be found
	 */
	public Entry<Double, Color> getEntry(Double value)
	{
		if (value == null)
		{
			return null;
		}

		if (!entries.containsKey(value))
		{
			return null;
		}

		return new AbstractMap.SimpleEntry<Double, Color>(value, entries.get(value));
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof ColorMap))
		{
			return false;
		}

		ColorMap other = (ColorMap) obj;

		// Equality based on all fields (use short-circuiting to avoid unnecessary tests)
		boolean equals = au.gov.ga.earthsci.common.util.Util.nullSafeEquals(name, other.name);
		equals = equals && au.gov.ga.earthsci.common.util.Util.nullSafeEquals(description, other.description);
		equals = equals && (mode == other.mode);
		equals = equals && (valuesArePercentages == other.valuesArePercentages);
		equals = equals && au.gov.ga.earthsci.common.util.Util.nullSafeEquals(nodataColour, other.nodataColour);
		equals = equals && (entries.size() == other.entries.size());

		for (Entry<Double, Color> thisEntry : entries.entrySet())
		{
			equals = equals && other.entries.containsKey(thisEntry.getKey());
			equals = equals && thisEntry.getValue().equals(other.entries.get(thisEntry.getKey()));
		}

		return equals;
	}

	@Override
	public int hashCode()
	{
		int hash = 31;
		if (name != null)
		{
			hash += name.hashCode();
		}
		if (description != null)
		{
			hash += description.hashCode();
		}
		if (nodataColour != null)
		{
			hash += nodataColour.hashCode();
		}
		if (mode != null)
		{
			hash += mode.hashCode();
		}
		if (valuesArePercentages)
		{
			hash += 1;
		}
		hash += entries.hashCode();
		return hash;
	}
}
