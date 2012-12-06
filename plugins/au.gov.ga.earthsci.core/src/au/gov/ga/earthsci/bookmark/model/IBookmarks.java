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
package au.gov.ga.earthsci.bookmark.model;

/**
 * The core entry point to the bookmarks model. Gives access to named {@link IBookmarkList}s
 * and a default list to use in the absence of any other.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarks
{

	/**
	 * Retrieve a bookmark list by name. In the case of two bookmarks lists
	 * having the same name, the result is implementation specific but may be
	 * undefined.
	 * 
	 * @param name The name of the bookmark list to return.
	 * 
	 * @return The named bookmark list, or <code>null</code> if one does not exist.
	 */
	IBookmarkList getListByName(String name);
	
	/**
	 * Retrieve a bookmark list by ID.
	 * 
	 * @param id The ID of the bookmark list to return.
	 * 
	 * @return The bookmark list, or <code>null</code> if one does not exist.
	 */
	IBookmarkList getListById(String id);
	
	/**
	 * Return the default bookmark list which is present in every model.
	 *  
	 * @return The default bookmark list
	 */
	IBookmarkList getDefaultList();
	
	/**
	 * Return all of the bookmarks lists currently present.
	 * 
	 * @return All of the bookmark lists currently present.
	 */
	IBookmarkList[] getLists();
	
	/**
	 * Add the given bookmark list to this model. If one already exists with the same ID it will be replaced.
	 * 
	 * @param list The new list to add.
	 */
	void addList(IBookmarkList list);

	/**
	 * Set the given bookmarks liss on this model.
	 * 
	 * @param lists The lists to add.
	 */
	void setLists(IBookmarkList[] lists);
	
}
