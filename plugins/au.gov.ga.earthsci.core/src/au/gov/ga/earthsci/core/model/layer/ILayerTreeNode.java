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

import java.net.URI;
import java.net.URL;

import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.util.IEnableable;
import au.gov.ga.earthsci.core.util.ILabelable;
import au.gov.ga.earthsci.core.util.INameable;
import au.gov.ga.earthsci.core.util.IPropertyChangeBean;

/**
 * Represents a tree node value in the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerTreeNode extends ITreeNode<ILayerTreeNode>, IPropertyChangeBean, ILabelable, INameable
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
	 * Does the enabled state of any children of this node equal the given
	 * value?
	 * 
	 * @param enabled
	 *            Value to test
	 * @return True if any children's enabled equals the given value.
	 */
	boolean anyChildrenEnabledEquals(boolean enabled);

	/**
	 * Enable/disable this node (if {@link IEnableable}), and enable/disable all
	 * {@link IEnableable} children.
	 * 
	 * @param enabled
	 */
	void enableChildren(boolean enabled);

	/**
	 * Notify property listeners that this node's enable state has changed.
	 * Should only be called internally.
	 */
	void enabledChanged();

	/**
	 * Notify property listeners that this node's children have changed. Should
	 * only be called internally.
	 * 
	 * @param oldChildren
	 * @param newChildren
	 */
	void childrenChanged(ITreeNode<ILayerTreeNode>[] oldChildren, ITreeNode<ILayerTreeNode>[] newChildren);

	/**
	 * @return The URL pointing to this layer's information page.
	 */
	URL getInfoUrl();

	/**
	 * @return The URL pointing to this layer's legend.
	 */
	URL getLegendUrl();

	/**
	 * @return The URL pointing to this layer's icon.
	 */
	URL getIconUrl();

	/**
	 * @return Is this tree node expanded?
	 */
	boolean isExpanded();

	/**
	 * Mark this tree node as expanded.
	 * 
	 * @param expanded
	 *            Expanded state
	 */
	void setExpanded(boolean expanded);

	/**
	 * @return URI that uniquely identifies this layer node, and optionally
	 *         locates a resource for layer creation.
	 */
	URI getUri();

	/**
	 * Set this node's URI, which uniquely identifies this node. This is also
	 * used by certain nodes for layer creation.
	 * 
	 * @param uri
	 */
	void setUri(URI uri);
}
