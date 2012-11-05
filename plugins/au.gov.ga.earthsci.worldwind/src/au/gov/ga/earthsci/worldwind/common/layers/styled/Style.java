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

/**
 * Generalised selectable style. A style is selected using the
 * {@link StyleProvider} for a particular set of attribute values, and then the
 * Style is used to set an object's properties.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Style extends PropertySetter
{
	protected String name;
	protected boolean defalt;

	/**
	 * Create a new style.
	 * 
	 * @param name
	 * @param defalt
	 *            Is this style the default style?
	 */
	public Style(String name, boolean defalt)
	{
		setName(name);
		setDefault(defalt);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isDefault()
	{
		return defalt;
	}

	public void setDefault(boolean defalt)
	{
		this.defalt = defalt;
	}
}
