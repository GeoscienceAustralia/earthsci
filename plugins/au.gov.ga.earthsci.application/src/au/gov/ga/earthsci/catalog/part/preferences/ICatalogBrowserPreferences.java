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
package au.gov.ga.earthsci.catalog.part.preferences;

import au.gov.ga.earthsci.application.util.UserActionPreference;

/**
 * An interface for objects that can provide user preferences to the 
 * catalog browser and its components.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ICatalogBrowserPreferences
{
	String QUALIFIER_ID = "au.gov.ga.earthsci.catalog"; //$NON-NLS-1$
	String ADD_NODE_STRUCTURE_MODE = "au.gov.ga.earthsci.catalog.preferences.addNodeStructure"; //$NON-NLS-1$
	
	/**
	 * Return the user's preference for adding the complete tree structure
	 * when a leaf (layer) node is added.
	 * 
	 * @return <code>true</code> if the complete structure is to be created; <code>false</code> if the layer is to be added flat; 
	 * and <code>null</code> if no preference has been set and the user should be prompted.
	 */
	UserActionPreference getAddNodeStructureMode();

	/**
	 * Set the user's preference for adding the complete tree structure when a leaf node is added.
	 * 
	 * @param mode <code>true</code> = Always add; <code>false</code> = Never add; <code>null</code> = prompt;
	 */
	void setAddNodeStructureMode(UserActionPreference mode);
	
	
}
