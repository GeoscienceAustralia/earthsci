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
	 * Add the provided catalog nodes (and their children, as appropriate) to the current layer model
	 * 
	 * @param nodes The nodes to add to the layer model
	 */
	void addToLayerModel(ICatalogTreeNode[] nodes);
	
	/**
	 * Remove the provided layer nodes from the current layer model
	 * 
	 * @param nodes the nodes to remove from the layer model
	 */
	void removeFromLayerModel(ICatalogTreeNode[] nodes);
	
	/**
	 * Set the catalog browser part on this controller
	 * 
	 * @param part
	 */
	void setCatalogBrowserPart(CatalogBrowserPart part);
	
}
