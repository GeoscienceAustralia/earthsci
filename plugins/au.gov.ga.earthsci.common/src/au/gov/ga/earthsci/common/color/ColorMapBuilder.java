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
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;

/**
 * A builder class for creating {@link ColorMap} instances
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class ColorMapBuilder
{

	/**
	 * Begin creating a new ColorMap using a builder
	 */
	public static ColorMapBuilder createColorMap()
	{
		return new ColorMapBuilder();
	}

	private String name;
	private String description;
	private Map<Double, Color> entries = new HashMap<Double, Color>();
	private InterpolationMode mode = InterpolationMode.INTERPOLATE_RGB;
	private boolean valuesArePercentages = false;
	private Color nodata;

	/**
	 * Create a new empty builder ready to be populated with values
	 */
	public ColorMapBuilder()
	{
	}

	public ColorMapBuilder named(String name)
	{
		this.name = name;
		return this;
	}

	public ColorMapBuilder describedAs(String description)
	{
		this.description = description;
		return this;
	}

	public ColorMapBuilder using(InterpolationMode mode)
	{
		this.mode = mode;
		return this;
	}

	public ColorMapBuilder withPercentageValues()
	{
		this.valuesArePercentages = true;
		return this;
	}

	public ColorMapBuilder withAbsoluteValues()
	{
		this.valuesArePercentages = false;
		return this;
	}

	public ColorMapBuilder withValuesAsPercentages(boolean enable)
	{
		this.valuesArePercentages = enable;
		return this;
	}

	public ColorMapBuilder withEntry(Double val, Color color)
	{
		this.entries.put(val, color);
		return this;
	}

	public ColorMapBuilder withNodata(Color color)
	{
		this.nodata = color;
		return this;
	}

	public ColorMap build()
	{
		return new ColorMap(name, description, entries, nodata, mode, valuesArePercentages);
	}

}
