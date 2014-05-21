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
package au.gov.ga.earthsci.layer.tree;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.earthsci.common.util.IEnableable;
import au.gov.ga.earthsci.common.util.ILoader;
import au.gov.ga.earthsci.layer.IPersistentLayer;
import au.gov.ga.earthsci.layer.delegator.ILayerDelegator;

/**
 * {@link ILayerTreeNode} that contains an {@link IPersistentLayer} that it
 * delegates {@link Layer} methods to.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerNode extends ILayerTreeNode, ILayerDelegator<IPersistentLayer>, ILoader, IEnableable
{
	/**
	 * @return If the layer associated with this node is an elevation model,
	 *         return the elevation model, otherwise <code>null</code>
	 */
	ElevationModel getElevationModel();

	/**
	 * Set the loading status of this node. Note that, if the layer associated
	 * with this node is loading, {@link #isLoading()} should return true
	 * regardless of the value set here.
	 * 
	 * @param loading
	 *            Is this node loading?
	 */
	void setLoading(boolean loading);

	/**
	 * @return The draw order for this layer
	 */
	int getDrawOrder();

	/**
	 * Set the draw order for this layer. Layers with the same draw order are
	 * drawn in the order they appear in the layer list.
	 * 
	 * @param drawOrder
	 */
	void setDrawOrder(int drawOrder);
}
