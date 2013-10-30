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

/**
 * Wrapper for legacy layers. Delegates all {@link Layer} methods to the wrapped
 * {@link Layer} instance, and provides an implementation for any extra methods
 * defined by the {@link ILayer} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerWrapper extends ILayer, ILayerDelegate
{
	/**
	 * Does this wrapper support wrapping the given layer?
	 * 
	 * @param layer
	 *            Layer to be wrapped
	 * @return True if this wrapper can wrap the given layer
	 */
	boolean supports(Layer layer);
}
