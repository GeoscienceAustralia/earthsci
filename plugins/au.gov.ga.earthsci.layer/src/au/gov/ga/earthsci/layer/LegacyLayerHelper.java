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
import au.gov.ga.earthsci.layer.delegator.ILayerDelegator;

/**
 * Helper class for converting legacy World Wind {@link Layer}s to instances of
 * {@link IPersistentLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LegacyLayerHelper
{
	/**
	 * Wrap the given legacy World Wind {@link Layer} in a class that implements
	 * {@link IPersistentLayer}. Uses the {@link ExtensionManager} to wrap the
	 * layer.
	 * 
	 * @param layer
	 *            Layer to wrap
	 * @return Wrapped layer
	 */
	public static IPersistentLayer wrap(Layer layer)
	{
		//if the layer is delegating to another layer, find an IPersistentLayer within the delegate hierarchy if one exists
		if (layer instanceof ILayerDelegator)
		{
			IPersistentLayer lowestILayer = null;
			ILayerDelegator<?> delegator = (ILayerDelegator<?>) layer;
			while (true)
			{
				Layer delegate = delegator.getLayer();
				if (delegate instanceof IPersistentLayer)
				{
					lowestILayer = (IPersistentLayer) delegate;
				}
				if (!(delegate instanceof ILayerDelegator))
				{
					break;
				}
				delegator = (ILayerDelegator<?>) delegate;
			}
			if (lowestILayer != null)
			{
				return lowestILayer;
			}
		}

		//if the layer is already an IPersistentLayer, return it
		if (layer instanceof IPersistentLayer)
		{
			return (IPersistentLayer) layer;
		}

		//otherwise wrap it in the legacy wrapper
		return ExtensionManager.getInstance().wrapLayer(layer);
	}
}
