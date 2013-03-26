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

import au.gov.ga.earthsci.bookmark.BookmarkFactory;
import au.gov.ga.earthsci.bookmark.model.IBookmark;

/**
 * An interface for object that can provide user preferences for the bookmarks
 * feature and its components.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarksPreferences
{
	String QUALIFIER_ID = "au.gov.ga.earthsci.bookmarks"; //$NON-NLS-1$
	String DEFAULT_TRANSITION_DURATION = "au.gov.ga.earthsci.bookmarks.preferences.defaultTransitionDuration"; //$NON-NLS-1$
	String PLAY_BOOKMARKS_WAIT_DURATION = "au.gov.ga.earthsci.bookmarks.preferences.playBookmarksWaitDuration"; //$NON-NLS-1$
	String ASK_LIST_DELETE_CONFIRM = "au.gov.ga.earthsci.bookmarks.preferences.askListDeleteConfirmation"; //$NON-NLS-1$
	String DEFAULT_PROPERTIES = "au.gov.ga.earthsci.bookmarks.preferences.defaultProperties"; //$NON-NLS-1$

	/**
	 * Returns the default duration to be used when transitioning from the
	 * current world state to the saved state of a bookmark.
	 * 
	 * @return The transition duration in milliseconds
	 * 
	 * @see IBookmark#getTransitionDuration()
	 */
	long getDefaultTransitionDuration();

	/**
	 * Returns the duration (in milliseconds) that bookmarks are paused at
	 * during playback of a bookmarks list
	 * 
	 * @return The duration (in milliseconds) to wait on a bookmark during
	 *         playback
	 */
	long getPlayBookmarksWaitDuration();

	/**
	 * Returns the default list of bookmark properties that are to be included
	 * when new bookmarks are created.
	 * 
	 * @return The list of default bookmark property types to include
	 * 
	 * @see BookmarkFactory#createBookmark(String...)
	 */
	String[] getDefaultPropertyTypes();

	/**
	 * Returns whether to ask for list deletion confirmation
	 * 
	 * @return <code>true</code> if the user wishes to be prompted;
	 *         <code>false</code> otherwise.
	 */
	boolean askForListDeleteConfirmation();

	/**
	 * Set whether or not to ask for list deletion confirmation
	 * 
	 * @param ask
	 *            whether or not to ask
	 */
	void setAskForListDeleteConfirmation(boolean ask);
}
