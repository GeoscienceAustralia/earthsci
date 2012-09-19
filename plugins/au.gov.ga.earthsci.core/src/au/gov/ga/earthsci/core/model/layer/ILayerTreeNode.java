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
package au.gov.ga.earthsci.core.model.layer;

import gov.nasa.worldwind.layers.LayerList;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.util.IEnableable;
import au.gov.ga.earthsci.core.util.INameable;
import au.gov.ga.earthsci.core.util.IPropertyChangeBean;

/**
 * Represents a tree node value in the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerTreeNode<E extends ILayerTreeNode<E>> extends ITreeNode<E>, IPropertyChangeBean, INameable
{
	/**
	 * @return A {@link LayerList} that contains all layers in the tree at and
	 *         below this node.
	 */
	LayerList getLayerList();

	/**
	 * @return Are any of this node's children enabled?
	 */
	boolean isAnyChildrenEnabled();

	/**
	 * @return Are all of this node's children enabled?
	 */
	boolean isAllChildrenEnabled();

	/**
	 * Enable/disable this node (if {@link IEnableable}), and enable/disable all
	 * {@link IEnableable} children.
	 * 
	 * @param enabled
	 */
	void enableChildren(boolean enabled);
}
