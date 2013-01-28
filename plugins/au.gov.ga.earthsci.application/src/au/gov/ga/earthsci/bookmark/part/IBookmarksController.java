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
package au.gov.ga.earthsci.bookmark.part;

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyApplicator;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkList;

/**
 * A controller interface for the bookmarks feature
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarksController
{

	/**
	 * Create a new bookmark and append it to the given bookmark list.
	 * 
	 * @param list The list append the new bookmark to
	 * 
	 * @return The created bookmark
	 */
	IBookmark createNew(IBookmarkList list);
	
	/**
	 * Create a new bookmark and append it to the current bookmark list
	 * 
	 * @return The created bookmark
	 */
	IBookmark createNew();
	
	/**
	 * Apply the given bookmark using appropriate {@link IBookmarkPropertyApplicator}s
	 * and user preferences.
	 * 
	 * @param bookmark The bookmark to apply
	 */
	void apply(IBookmark bookmark);

	/**
	 * Launch an editor to collect user edits to the provided bookmark.
	 * 
	 * @param bookmark The bookmark to edit
	 */
	void edit(IBookmark bookmark);
	
	/**
	 * Delete the given bookmark from the current list
	 * 
	 * @param bookmark The bookmark to delete
	 */
	void delete(IBookmark bookmark);
	
	/**
	 * Delete the given bookmarks from the current list
	 * 
	 * @param bookmarks The bookmarks to delete
	 */
	void delete(IBookmark... bookmarks);
	
	/**
	 * Return the currently selected bookmark list
	 * 
	 * @return The currently selected bookmark list
	 */
	IBookmarkList getCurrentList();
	
	/**
	 * Play through the given bookmark list, starting at the given bookmark.
	 * <p/>
	 * If the given bookmark does not exist in the list, or is <code>null</code>, play 
	 * from the start of the given list.
	 * 
	 * @param list The bookmark list to play through
	 * @param bookmark The bookmark to start at
	 */
	void play(IBookmarkList list, IBookmark bookmark);
	
	/**
	 * Play through the current bookmark list, starting at the given bookmark.
	 * <p/>
	 * If the given bookmark does not exist in the current list, or is <code>null</code>,
	 * play from the start of the current list.
	 * 
	 * @param bookmark The bookmark to start at in the current list
	 */
	void play(IBookmark bookmark);
	
	/**
	 * @return Whether there are any actively playing lists
	 */
	boolean isPlaying();
	
	/**
	 * Stop any running playlists
	 */
	void stop();
	
	/**
	 * Set the UI view this controller links to
	 */
	void setView(BookmarksPart part);
}
