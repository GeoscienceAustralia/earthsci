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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;

/**
 * The default {@link IBookmarkList} implementation
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Exportable
public class BookmarkList implements IBookmarkList
{
	@Persistent(attribute = true)
	private String id;
	
	@Persistent
	private String name;
	
	@Persistent
	private List<IBookmark> bookmarks = new ArrayList<IBookmark>();

	/**
	 * Create a new unnamed bookmark list with a unique randome ID.
	 */
	public BookmarkList()
	{
		this.id = UUID.randomUUID().toString();
	}
	
	/**
	 * Create a new bookmark list with the given name and a unique random ID.
	 */
	public BookmarkList(String name)
	{
		this();
		this.name = name;
	}
	
	/**
	 * Create a new bookmark list with the given name and ID.
	 */
	public BookmarkList(String id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	@Override
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public List<IBookmark> getBookmarks()
	{
		return bookmarks;
	}

	@Override
	public void setBookmarks(List<IBookmark> bookmarks)
	{
		this.bookmarks = bookmarks;
	}
	
}
