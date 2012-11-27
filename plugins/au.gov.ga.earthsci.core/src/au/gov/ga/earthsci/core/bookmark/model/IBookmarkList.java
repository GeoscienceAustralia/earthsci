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
package au.gov.ga.earthsci.core.bookmark.model;

import java.util.List;

/**
 * An interface for an ordered list of bookmarks with a given name.
 * <p/>
 * Bookmark lists are used to group bookmarks into a logical collection. This can be used to tell a 'story'
 * about a set of data, to sort bookmarks, or to create a presentation, for example.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkList
{
	/**
	 * Return the human readable name of this bookmark list.
	 * 
	 * @return The name of this bookmark list.
	 */
	String getName();
	
	/**
	 * Set the name for this list
	 * 
	 * @param name The name to set on this list
	 */
	void setName(String name);
	
	/**
	 * Return the list of bookmarks in this bookmark list.
	 * 
	 * @return The ordered list of bookmarks in this bookmark list.
	 */
	List<IBookmark> getBookmarks();
	
	/**
	 * Set the bookmarks on this list
	 * 
	 * @param bookmarks The bookmarks to set on this list
	 */
	void setBookmarks(List<IBookmark> bookmarks);
}
