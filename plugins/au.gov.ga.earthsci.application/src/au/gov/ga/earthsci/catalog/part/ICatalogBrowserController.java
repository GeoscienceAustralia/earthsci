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
package au.gov.ga.earthsci.catalog.part;

import java.net.URI;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;


/**
 * A controller class that coordinates interactions between the catalog browser 
 * and the current layer model
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ICatalogBrowserController
{

	/**
	 * Determine whether the provided layer URI exists in the current layer model.
	 * 
	 * @param layerURI The layer URI to test for
	 * 
	 * @return <code>true</code> if the provided layer URI exists in the current layer model. <code>false</code> otherwise.
	 */
	boolean existsInLayerModel(URI layerURI);
	
	/**
	 * Determine whether <em>all</em> of the nodes in the provided list (and their children) that represent layer nodes
	 * represent layers that exist in the current layer model.
	 * 
	 * @param nodes The list of parent nodes to inspect.
	 * 
	 * @return <code>true</code> if all of the layer nodes in the provided trees exist in the the layer model; <code>false</code> otherwise.
	 */
	boolean allExistInLayerModel(ITreeNode<ICatalogTreeNode>... nodes);
	
	/**
	 * Determine whether <em>any</em> of the nodes in the provided list (and their children) that represent layer nodes
	 * represent layers that exist in the current layer model.
	 * 
	 * @param nodes The list of parent nodes to inspect.
	 * 
	 * @return <code>true</code> if any of the layer nodes in the provided trees exist in the the layer model; <code>false</code> otherwise.
	 */
	boolean anyExistInLayerModel(ITreeNode<ICatalogTreeNode>... nodes);
	
	/**
	 * Add the provided catalog nodes (and their children, as appropriate) to the current layer model
	 * 
	 * @param nodes The nodes to add to the layer model
	 */
	void addToLayerModel(ITreeNode<ICatalogTreeNode>... nodes);
	
	/**
	 * Remove the provided layer nodes from the current layer model
	 * 
	 * @param nodes the nodes to remove from the layer model
	 */
	void removeFromLayerModel(ITreeNode<ICatalogTreeNode>... nodes);
	
	/**
	 * Set the catalog browser part on this controller
	 * 
	 * @param part
	 */
	void setCatalogBrowserPart(CatalogBrowserPart part);

	/**
	 * Determine whether all of the provided nodes are layer nodes (e.g. ones that can be added to the current layer model)
	 * 
	 * @param nodes The list of nodes to inspect
	 * 
	 * @return <code>true</code> if all nodes are layer nodes; <code>false</code> otherwise.
	 */
	boolean areAllLayerNodes(ITreeNode<ICatalogTreeNode>... nodes);
	
}
