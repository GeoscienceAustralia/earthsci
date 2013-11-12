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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.util.IEnableable;
import au.gov.ga.earthsci.common.util.ILoader;
import au.gov.ga.earthsci.common.util.INameable;
import au.gov.ga.earthsci.common.util.IPropertyChangeBean;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.layer.tree.ILayerNode;

/**
 * Persistent layer. Contains extensions of World Wind's {@link Layer} interface
 * that are required for EarthSci, such as layer persistence.
 * <p/>
 * All implementations must have an empty constructor (can be private), which is
 * called via reflection before loading the layer from a saved state.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IPersistentLayer extends Layer, IPropertyChangeBean, ILoader, INameable, IEnableable
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

	/**
	 * Initialize the layer. Only called during startup, after
	 * {@link #load(Element)} has been called. Layers that implement delayed
	 * loading can load now, which can include starting an {@link Intent} to
	 * perform the layer load.
	 * <p/>
	 * Note that this is not called on new instances created during normal
	 * execution of the application (from catalogs, intents, etc). It is only
	 * called on startup on layers loaded from a persisted state.
	 * 
	 * @param node
	 * @param context
	 */
	void initialize(ILayerNode node, IEclipseContext context);

	/**
	 * Generate a string that represents the class and state of this layer. The
	 * returned string must be portable across different JVMs and operating
	 * systems; ie layers of the same class and state must always generate the
	 * same hash. An example implementation would be to return the MD5 hash of
	 * the UTF-8 encoding of the resulting element from the
	 * {@link #save(Element)} method.
	 * 
	 * @return A short string that portably and uniquely represents this layer
	 */
	//String getStateKey();
}
