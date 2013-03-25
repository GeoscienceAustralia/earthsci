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
package au.gov.ga.earthsci.bookmark.part.preferences;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory;
import au.gov.ga.earthsci.common.util.Util;

/**
 * Default implementation of the {@link IBookmarksPreferences} interface that
 * uses the Eclipse preferences mechanism.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarksPreferences implements IBookmarksPreferences
{
	
	private static final Logger logger = LoggerFactory.getLogger(BookmarksPreferences.class);
	
	@Inject
	@Preference(nodePath=QUALIFIER_ID)
	private IEclipsePreferences preferenceStore;

	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=DEFAULT_TRANSITION_DURATION)
	private long defaultTransitionDuration;
	
	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=PLAY_BOOKMARKS_WAIT_DURATION)
	private long playBookmarksWaitDuration;
	
	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=DEFAULT_PROPERTIES)
	private String defaultProperties;
	
	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=ASK_LIST_DELETE_CONFIRM)
	private boolean confirmListDelete;
	
	@Override
	public long getDefaultTransitionDuration()
	{
		return defaultTransitionDuration;
	}
	
	@Override
	public long getPlayBookmarksWaitDuration()
	{
		return playBookmarksWaitDuration;
	}
	
	@Override
	public String[] getDefaultPropertyTypes()
	{
		if (defaultProperties == null)
		{
			String[] knownTypes = BookmarkPropertyFactory.getKnownPropertyTypes();
			defaultProperties = Util.concat(knownTypes, ",", ""); //$NON-NLS-1$ //$NON-NLS-2$
			return knownTypes;
		}
		return defaultProperties.split(","); //$NON-NLS-1$
	}
	
	@Override
	public boolean askForListDeleteConfirmation()
	{
		return confirmListDelete;
	}
	
	@Override
	public void setAskForListDeleteConfirmation(boolean ask)
	{
		preferenceStore.putBoolean(ASK_LIST_DELETE_CONFIRM, ask);
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
