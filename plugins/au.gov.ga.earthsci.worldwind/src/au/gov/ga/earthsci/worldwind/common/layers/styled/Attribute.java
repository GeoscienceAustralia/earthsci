/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.styled;

import gov.nasa.worldwind.avlist.AVList;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * An attribute represents a column in tabular data. It could be an attribute
 * within a shapefile DBF file, a column in a CSV file, etc. It allows matching
 * of attribute values to an associated {@link Style} name, using exact
 * matching, regex matching, and in-range matching.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Attribute
{
	protected String name;
	protected Map<String, String> switches = new HashMap<String, String>();
	protected Map<String, String> regexes = new HashMap<String, String>();
	protected Map<Range, String> ranges = new HashMap<Range, String>();
	protected StringWithPlaceholder textString;
	protected StringWithPlaceholder linkString;

	/**
	 * Create a new attribute.
	 * 
	 * @param name
	 *            Name of the attribute
	 */
	public Attribute(String name)
	{
		setName(name);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Add an exact match case.
	 * 
	 * @param value
	 *            Attribute value to match
	 * @param style
	 *            Style to return if match is found
	 */
	public void addCase(String value, String style)
	{
		switches.put(value, style);
	}

	/**
	 * Add a regular expression match case.
	 * 
	 * @param regex
	 *            Regular expression pattern
	 * @param style
	 *            Style to return if the attribute value matches the regex
	 */
	public void addRegex(String regex, String style)
	{
		regexes.put(regex, style);
	}

	/**
	 * Add a in-range match case.
	 * 
	 * @param min
	 *            Minimum for the attribute value (inclusive)
	 * @param max
	 *            Maximum for the attribute value (inclusive)
	 * @param style
	 *            Style to return if the value is in range
	 */
	public void addRange(double min, double max, String style)
	{
		Range range = new Range();
		range.min = min;
		range.max = max;
		ranges.put(range, style);
	}

	/**
	 * Make this attribute contribute to the text of the object.
	 * 
	 * @param value
	 *            Text to append
	 * @param placeholder
	 *            Text in <code>value</code> to replace with the attribute value
	 */
	public void addText(String value, String placeholder)
	{
		textString = new StringWithPlaceholder(value, placeholder);
	}

	/**
	 * This attribute will be used to create the object's link.
	 * 
	 * @param url
	 *            URL link
	 * @param placeholder
	 *            Text in <code>url</code> to replace with the attribute value
	 */
	public void addLink(String url, String placeholder)
	{
		linkString = new StringWithPlaceholder(url, placeholder);
	}

	/**
	 * Create the text contributed by this attribute, using the attribute value
	 * in the provided {@link AVList}.
	 * 
	 * @param attributeValues
	 *            Attribute values to replace any text placeholders with
	 * @return
	 */
	public String getText(AVList attributeValues)
	{
		return getPlaceholderString(textString, attributeValues);
	}

	/**
	 * Create the link for this attribute, using the attribute value in the
	 * provided {@link AVList}.
	 * 
	 * @param attributeValues
	 *            Attribute values to replace any link placeholders with
	 * @return
	 */
	public String getLink(AVList attributeValues)
	{
		return getPlaceholderString(linkString, attributeValues);
	}

	protected String getPlaceholderString(StringWithPlaceholder string, AVList attributeValues)
	{
		if (string == null || attributeValues.getValue(name) == null)
			return null;

		String stringValue = attributeValues.getValue(name).toString();
		return string.replacePlaceholder(stringValue);
	}

	/**
	 * Find a matching {@link Style} string for the provided attributes.
	 * 
	 * @param attributeValues
	 *            Attribute values to use when searching for matching cases
	 * @return
	 */
	public String getMatchingStyle(AVList attributeValues)
	{
		if (attributeValues.getValue(name) == null)
			return null;

		String stringValue = attributeValues.getValue(name).toString();
		if (switches.containsKey(stringValue))
			return switches.get(stringValue);

		for (Entry<String, String> regex : regexes.entrySet())
		{
			if (Pattern.matches(regex.getKey(), stringValue))
				return regex.getValue();
		}

		Double doubleValue = null;
		try
		{
			doubleValue = Double.valueOf(stringValue);
		}
		catch (Exception e)
		{
		}
		if (doubleValue != null)
		{
			for (Entry<Range, String> range : ranges.entrySet())
			{
				if (range.getKey().contains(doubleValue))
					return range.getValue();
			}
		}

		return null;
	}

	/**
	 * Helper class for storing and testing a double range.
	 */
	protected class Range
	{
		public double min;
		public double max;

		public boolean contains(double value)
		{
			return min <= value && value <= max;
		}
	}

	/**
	 * Helper class for storing a string/placeholder pair.
	 */
	protected class StringWithPlaceholder
	{
		public final String string;
		public final String placeholder;
		
		public StringWithPlaceholder(String string, String placeholder)
		{
			this.string = string;
			this.placeholder = placeholder;
		}

		public String replacePlaceholder(String with)
		{
			return string.replaceAll(placeholder, with);
		}
	}
}
