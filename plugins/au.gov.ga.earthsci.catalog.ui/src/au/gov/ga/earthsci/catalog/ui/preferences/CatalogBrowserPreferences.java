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

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.application.util.UserActionPreference;

/**
 * Default implementation of the {@link ICatalogBrowserPreferences}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogBrowserPreferences implements ICatalogBrowserPreferences
{

	private static Logger logger = LoggerFactory.getLogger(CatalogBrowserPreferences.class);

	@Inject
	@Preference(nodePath = QUALIFIER_ID)
	private IEclipsePreferences preferenceStore;

	@Inject
	@Preference(nodePath = QUALIFIER_ID, value = ADD_NODE_STRUCTURE_MODE)
	private String addNodeStructureMode;

	@Inject
	@Preference(nodePath = QUALIFIER_ID, value = DELETE_EMPTY_FOLDERS_MODE)
	private String deleteEmptyFoldersMode;

	@Override
	public UserActionPreference getAddNodeStructureMode()
	{
		return UserActionPreference.valueOf(addNodeStructureMode);
	}

	@Override
	public void setAddNodeStructureMode(UserActionPreference mode)
	{
		preferenceStore.put(ADD_NODE_STRUCTURE_MODE, mode == null ? UserActionPreference.ASK.name() : mode.name());
		applyPreferences();
	}

	@Override
	public UserActionPreference getDeleteEmptyFoldersMode()
	{
		return UserActionPreference.valueOf(deleteEmptyFoldersMode);
	}

	@Override
	public void setDeleteEmptyFoldersMode(UserActionPreference mode)
	{
		preferenceStore.put(DELETE_EMPTY_FOLDERS_MODE, mode == null ? UserActionPreference.ASK.name() : mode.name());
		applyPreferences();
	}

	private void applyPreferences()
	{
		try
		{
			preferenceStore.flush();
		}
		catch (BackingStoreException e)
		{
			logger.error("An exception occurred while applying the catalog browser preference", e); //$NON-NLS-1$
		}
	}
}
