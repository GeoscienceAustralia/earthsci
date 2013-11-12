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
package au.gov.ga.earthsci.layer.delegator;

import gov.nasa.worldwind.layers.Layer;

/**
 * {@link Layer} that delegates all methods to another {@link Layer} instance,
 * set by the {@link #setLayer(Layer)} method.
 * <p/>
 * Implementations that use a subclass/interface of Layer as the generic
 * argument for <code>L</code> should also extend/implement that
 * class/interface, so following the delegate design pattern.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerDelegator<L extends Layer> extends Layer
{
	/**
	 * @return The layer that this object delegates to
	 */
	L getLayer();

	/**
	 * Set the layer to delegate to
	 * 
	 * @param layer
	 */
	void setLayer(L layer);

	/**
	 * @return Class of the layer that this can delegate to
	 */
	Class<L> getLayerClass();

	/**
	 * @return If {@link #getLayer()} returns another {@link ILayerDelegator},
	 *         calls recursively, otherwise returns the layer
	 */
	L getGrandLayer();

	/**
	 * @return Has the layer been set on this delegate? If false,
	 *         {@link #getLayer()} will return a dummy layer.
	 */
	boolean isLayerSet();

	/**
	 * @return If {@link #getLayer()} returns another {@link ILayerDelegator},
	 *         calls recursively, otherwise returns {@link #isLayerSet()}.
	 */
	boolean isGrandLayerSet();
}
