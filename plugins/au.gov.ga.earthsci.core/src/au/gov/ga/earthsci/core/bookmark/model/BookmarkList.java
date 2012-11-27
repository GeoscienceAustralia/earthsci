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
 * The default {@link IBookmarkList} implementation
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkList implements IBookmarkList
{
	private String name;
	private List<IBookmark> bookmarks;
	
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
