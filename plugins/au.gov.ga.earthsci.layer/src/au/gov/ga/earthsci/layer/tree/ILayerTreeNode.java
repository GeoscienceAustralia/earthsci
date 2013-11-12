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
package au.gov.ga.earthsci.layer.tree;

import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.net.URI;
import java.net.URL;
import java.util.List;

import au.gov.ga.earthsci.common.util.IEnableable;
import au.gov.ga.earthsci.common.util.IInformationed;
import au.gov.ga.earthsci.common.util.ILabelable;
import au.gov.ga.earthsci.common.util.INameable;
import au.gov.ga.earthsci.common.util.IPropertyChangeBean;
import au.gov.ga.earthsci.core.model.IStatused;
import au.gov.ga.earthsci.core.tree.ITreeNode;

/**
 * Represents a tree node value in the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerTreeNode extends ITreeNode<ILayerTreeNode>, IPropertyChangeBean, ILabelable, INameable,
		IStatused, IInformationed
{
	/**
	 * This layer node's unique id. This should be randomly generated upon
	 * creation (eg from a UUID), and persisted during layer persistence. This
	 * can be used to quickly find layers matches by other systems, such as the
	 * bookmark system which saves layer state.
	 * 
	 * @return This node's unique id.
	 */
	String getId();

	/**
	 * Set this node's unique id from the given node's id. This shouldn't be
	 * called on any of the layers in the layer model; rather it can be used to
	 * match up a layer node in a separate system to a layer in the layer model.
	 * 
	 * @param node
	 *            Node whose id value will set as this node's id
	 */
	void setIdFrom(ILayerNode node);

	/**
	 * @return A {@link LayerList} that contains all layers in the tree at and
	 *         below this node.
	 */
	LayerList getLayers();

	/**
	 * @return A {@link CompoundElevationModel} that contains all elevation
	 *         models in the tree at and below this node.
	 */
	CompoundElevationModel getElevationModels();

	/**
	 * Return the {@link ILayerTreeNode} that is a descendant of this node which
	 * has a catalog URI that matches the given URI. If there are no descendants
	 * with the given catalog URI, null is returned. If there are multiple, the
	 * first is returned. The returned node could possibly be this node if this
	 * node's URI matches.
	 * <p/>
	 * The results from this method are cached, and updated when the node's
	 * descendants change.
	 * 
	 * @param catalogURI
	 *            Catalog URI to match
	 * @return First descendant node that has a matching catalog URI
	 */
	ILayerTreeNode getNodeForCatalogURI(URI catalogURI);

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
	void childrenChanged(List<ILayerTreeNode> oldChildren, List<ILayerTreeNode> newChildren);

	/**
	 * @return The URL pointing to this node's legend.
	 */
	URL getLegendURL();

	/**
	 * @return The URL pointing to this node's icon.
	 */
	URL getIconURL();

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
	 * @return The URI from the catalog tree node associated with this layer
	 *         node. Returns <code>null</code> if this node was not created from
	 *         a catalog.
	 */
	URI getCatalogURI();

	/**
	 * Set this node's catalog URI, which associates this node with a catalog
	 * tree node.
	 * 
	 * @param catalogUri
	 */
	void setCatalogURI(URI catalogURI);
}
