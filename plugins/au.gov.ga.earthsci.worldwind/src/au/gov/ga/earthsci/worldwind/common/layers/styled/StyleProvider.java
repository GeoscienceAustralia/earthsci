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

import java.util.List;

/**
 * A provider that yields the {@link Style} to use for a given set of
 * {@link Attribute} values
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface StyleProvider
{
	/**
	 * @return The {@link StyleAndText} to use for the given attribute values
	 */
	StyleAndText getStyle(AVList attributeValues);

	/**
	 * @return The collection of styles this provider supports
	 */
	List<Style> getStyles();

	/**
	 * Set the collection of styles this provider supports
	 */
	void setStyles(List<Style> styles);

	/**
	 * @return The collection of attributes associated with this provider
	 */
	List<Attribute> getAttributes();

	/**
	 * Set the collection of attributes associated with this provider.
	 * <p/>
	 * The attribute collection is used to map from attribute values to styles.
	 */
	void setAttributes(List<Attribute> attributes);
}
