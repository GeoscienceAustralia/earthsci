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
package au.gov.ga.earthsci.layer;

import gov.nasa.worldwind.layers.Layer;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.util.ILoader;
import au.gov.ga.earthsci.common.util.INameable;
import au.gov.ga.earthsci.common.util.IPropertyChangeBean;

/**
 * EarthSci layer. Contains extensions of World Wind's {@link Layer} interface
 * that are required for EarthSci.
 * <p/>
 * All implementations must have an empty constructor (can be private), which is
 * called via reflection before loading the layer from a saved state.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayer extends Layer, IPropertyChangeBean, ILoader, INameable
{
	/**
	 * Save any properties/state required to recreate this layer from XML.
	 * 
	 * @param parent
	 *            XML parent element to save into
	 */
	void save(Element parent);

	/**
	 * Load the properties/state for this layer from the given XML element.
	 * 
	 * @param parent
	 *            XML parent to load from (same as element passed to
	 *            {@link #save} method)
	 */
	void load(Element parent);
}
