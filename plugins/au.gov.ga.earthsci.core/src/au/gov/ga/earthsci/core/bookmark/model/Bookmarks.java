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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.earthsci.core.bookmark.Messages;
import au.gov.ga.earthsci.core.util.collection.ArrayListTreeMap;


/**
 * The default implementation of the {@link IBookmarks} interface.
 * <p/>
 * Uses a maps for fast lookup of lists by name/id.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Bookmarks implements IBookmarks
{
	private static final String DEFAULT_LIST_ID = "au.gov.ga.earthsci.core.bookmark.list.default"; //$NON-NLS-1$
	
	private ArrayListTreeMap<String, IBookmarkList> nameToListMap = new ArrayListTreeMap<String, IBookmarkList>();
	private Map<String, IBookmarkList> idToListMap = new HashMap<String, IBookmarkList>();

	private IBookmarkList defaultList;
	
	public Bookmarks()
	{
		BookmarkList defaultList = new BookmarkList();
		defaultList.setId(DEFAULT_LIST_ID);
		defaultList.setName("--" + Messages.Bookmarks_DefaultListName + "--"); //$NON-NLS-1$ //$NON-NLS-2$
		
		addList(defaultList);
		this.defaultList = defaultList;
	}
	
	@Override
	public IBookmarkList getListByName(String name)
	{
		ArrayList<IBookmarkList> lists = nameToListMap.get(name);
		if (lists != null && lists.size() > 0)
		{
			return lists.get(0);
		}
		return null;
	}

	@Override
	public IBookmarkList getListById(String id)
	{
		return idToListMap.get(id);
	}

	@Override
	public IBookmarkList getDefaultList()
	{
		return defaultList;
	}

	@Override
	public IBookmarkList[] getLists()
	{
		return nameToListMap.flatValues().toArray(new IBookmarkList[0]);
	}

	@Override
	public void addList(IBookmarkList list)
	{
		if (list == null)
		{
			return;
		}
		
		if (idToListMap.containsKey(list.getId()))
		{
			nameToListMap.removeSingle(list.getId(), list);
		}
		idToListMap.put(list.getId(), list);
		nameToListMap.putSingle(list.getName(), list);
	}
	
}
