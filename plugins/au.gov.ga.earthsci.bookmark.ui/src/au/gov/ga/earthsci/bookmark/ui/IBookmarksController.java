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
package au.gov.ga.earthsci.bookmark.ui;

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
	 * Move the provided bookmarks to a new index in the current list.
	 * <p/>
	 * In the case where the provided bookmarks are not sequential, they will be
	 * inserted at the target index such that they are sequential and maintain
	 * the same relative order.
	 * <p/>
	 * If a given bookmark is not part of the current list it will be ignored
	 * for the purposes of this operation.
	 * <p/>
	 * Equivalent to
	 * {@code moveBookmarks(getCurrentList(), bookmarks, getCurrentList(), targetIndex);}
	 * 
	 * @param bookmarks
	 *            The bookmarks to move
	 * @param targetIndex
	 *            The target index AS IT IS BEFORE THE MOVE
	 */
	void moveBookmarks(IBookmark[] bookmarks, int targetIndex);

	/**
	 * Move the provided bookmarks between the source and target lists
	 * <p/>
	 * Bookmarks will be removed from the source list.
	 * <p/>
	 * In the case where the provided bookmarks are not sequential, they will be
	 * inserted at the target index such that they are sequential and maintain
	 * the same relative order.
	 * 
	 * @param sourceList
	 *            The source list
	 * @param bookmarks
	 *            The bookmarks to move
	 * @param targetIndex
	 *            The target index AS IT IS BEFORE THE MOVE
	 * @param targetList
	 *            The target list
	 */
	void moveBookmarks(IBookmarkList sourceList, IBookmark[] bookmarks, IBookmarkList targetList, int targetIndex);

	/**
	 * Copy the selected bookmarks and paste them into the current list at the
	 * target index
	 * <p/>
	 * In the case where the provided bookmarks are not sequential, they will be
	 * inserted at the target index such that they are sequential and maintain
	 * the same relative order.
	 * 
	 * @param bookmarks
	 *            The bookmarks to copy
	 * @param targetIndex
	 *            The target index AS IT IS BEFORE THE COPY
	 */
	void copyBookmarks(IBookmark[] bookmarks, int targetIndex);

	/**
	 * Copy the selected bookmarks and paste them into the target list at the
	 * target index
	 * <p/>
	 * In the case where the provided bookmarks are not sequential, they will be
	 * inserted at the target index such that they are sequential and maintain
	 * the same relative order.
	 * 
	 * @param sourceList
	 *            The source list
	 * @param bookmarks
	 *            The bookmarks to copy
	 * @param targetList
	 *            The targetList
	 * @param targetIndex
	 *            The target index AS IT IS BEFORE THE COPY
	 */
	void copyBookmarks(IBookmarkList sourceList, IBookmark[] bookmarks, IBookmarkList targetList, int targetIndex);

	/**
	 * Create a new bookmark and append it to the given bookmark list.
	 * 
	 * @param list
	 *            The list append the new bookmark to
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
	 * Apply the given bookmark using appropriate
	 * {@link IBookmarkPropertyApplicator}s and user preferences.
	 * 
	 * @param bookmark
	 *            The bookmark to apply
	 */
	void apply(IBookmark bookmark);

	/**
	 * Launch an editor to collect user edits to the provided bookmark.
	 * 
	 * @param bookmark
	 *            The bookmark to edit
	 */
	void edit(IBookmark bookmark);

	/**
	 * Delete the given bookmark from the current list
	 * 
	 * @param bookmark
	 *            The bookmark to delete
	 */
	void delete(IBookmark bookmark);

	/**
	 * Delete the given bookmarks from the current list
	 * 
	 * @param bookmarks
	 *            The bookmarks to delete
	 */
	void delete(IBookmark... bookmarks);

	/**
	 * Return the currently selected bookmark list
	 * 
	 * @return The currently selected bookmark list
	 */
	IBookmarkList getCurrentList();

	/**
	 * Set the currently selected bookmark list
	 */
	void setCurrentList(IBookmarkList list);

	/**
	 * Play through the given bookmark list, starting at the given bookmark.
	 * <p/>
	 * If the given bookmark does not exist in the list, or is <code>null</code>
	 * , play from the start of the given list.
	 * 
	 * @param list
	 *            The bookmark list to play through
	 * @param bookmark
	 *            The bookmark to start at
	 */
	void play(IBookmarkList list, IBookmark bookmark);

	/**
	 * Play through the current bookmark list, starting at the given bookmark.
	 * <p/>
	 * If the given bookmark does not exist in the current list, or is
	 * <code>null</code>, play from the start of the current list.
	 * 
	 * @param bookmark
	 *            The bookmark to start at in the current list
	 */
	void play(IBookmark bookmark);

	/**
	 * Create a new bookmark list and add it to the current bookmarks model
	 * <p/>
	 * Implementations may prompt the user to provide a name for the list.
	 * 
	 * @return The newly created list
	 */
	IBookmarkList createNewBookmarkList();

	/**
	 * Prompt the user to rename the provided bookmark list
	 * 
	 * @param list
	 *            The list to rename
	 */
	void renameBookmarkList(IBookmarkList list);

	/**
	 * Delete the bookmark list from the current bookmarks model.
	 * <p/>
	 * Implementations may prompt the user to confirm deletion
	 * <p/>
	 * If the list being deleted is the current list, will change current list
	 * to default list. Will not delete the model's default list.
	 * 
	 * @param list
	 *            The list to delete
	 * 
	 * @return <code>true</code> if the list was deleted; <code>false</code>
	 *         otherwise
	 */
	boolean deleteBookmarkList(IBookmarkList list);

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
