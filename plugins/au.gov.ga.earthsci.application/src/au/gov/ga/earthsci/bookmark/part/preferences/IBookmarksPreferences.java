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

import au.gov.ga.earthsci.bookmark.model.IBookmark;

/**
 * An interface for object that can provide user preferences for
 * the bookmarks feature and its components. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarksPreferences
{
	String QUALIFIER_ID = "au.gov.ga.earthsci.bookmarks"; //$NON-NLS-1$
	String DEFAULT_TRANSITION_DURATION = "au.gov.ga.earthsci.bookmarks.preferences.defaultTransitionDuration"; //$NON-NLS-1$
	
	/**
	 * Returns the default duration to be used when transitioning
	 * from the current world state to the saved state of a bookmark.
	 * 
	 * @return The transition duration in milliseconds
	 * 
	 * @see IBookmark#getTransitionDuration()
	 */
	long getDefaultTransitionDuration();
	
}
