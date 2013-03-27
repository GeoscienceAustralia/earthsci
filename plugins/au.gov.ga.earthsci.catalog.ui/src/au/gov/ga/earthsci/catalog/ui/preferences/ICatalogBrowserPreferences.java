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
package au.gov.ga.earthsci.catalog.ui.preferences;

import au.gov.ga.earthsci.application.util.UserActionPreference;

/**
 * An interface for objects that can provide user preferences to the catalog
 * browser and its components.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ICatalogBrowserPreferences
{
	String QUALIFIER_ID = "au.gov.ga.earthsci.catalog"; //$NON-NLS-1$
	String ADD_NODE_STRUCTURE_MODE = "au.gov.ga.earthsci.catalog.preferences.addNodeStructure"; //$NON-NLS-1$
	String DELETE_EMPTY_FOLDERS_MODE = "au.gov.ga.earthsci.catalog.preferences.deleteEmptyFolders"; //$NON-NLS-1$

	/**
	 * Return the user's preference for adding the complete tree structure when
	 * a leaf (layer) node is added.
	 */
	UserActionPreference getAddNodeStructureMode();

	/**
	 * Set the user's preference for adding the complete tree structure when a
	 * leaf node is added.
	 */
	void setAddNodeStructureMode(UserActionPreference mode);

	/**
	 * Return the user's preference for removing empty folder nodes from the
	 * layer tree when all children are removed using the catalog browser.
	 */
	UserActionPreference getDeleteEmptyFoldersMode();

	/**
	 * Set the user's preference for removing empty folder nodes from the layer
	 * tree when all children are removed using the catalog browser
	 */
	void setDeleteEmptyFoldersMode(UserActionPreference mode);
}
