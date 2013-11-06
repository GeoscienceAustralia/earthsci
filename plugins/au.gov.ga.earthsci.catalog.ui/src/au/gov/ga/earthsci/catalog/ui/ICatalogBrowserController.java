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
package au.gov.ga.earthsci.catalog.ui;

import au.gov.ga.earthsci.catalog.ICatalogTreeNode;

/**
 * A controller class that coordinates interactions between the catalog browser
 * and the current layer model
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ICatalogBrowserController
{
	/**
	 * Add the provided catalog nodes (and their children, as appropriate) to
	 * the current layer model
	 * 
	 * @param nodes
	 *            The nodes to add to the layer model
	 */
	void addToLayerModel(ICatalogTreeNode... nodes);

	/**
	 * Determine whether all of the provided nodes are layer nodes (e.g. ones
	 * that can be added to the current layer model)
	 * 
	 * @param nodes
	 *            The list of nodes to inspect
	 * 
	 * @return <code>true</code> if all nodes are layer nodes;
	 *         <code>false</code> otherwise.
	 */
	boolean areAllLayerNodes(ICatalogTreeNode... nodes);
}
