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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import au.gov.ga.earthsci.bookmark.Messages;
import au.gov.ga.earthsci.bookmark.io.BookmarksPersister;
import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.util.collection.ArrayListTreeMap;


/**
 * The default implementation of the {@link IBookmarks} interface.
 * <p/>
 * Uses a maps for fast lookup of lists by name/id.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Exportable
@Creatable
@Singleton
public class Bookmarks implements IBookmarks
{
	private static final String DEFAULT_LIST_ID = "au.gov.ga.earthsci.bookmark.list.default"; //$NON-NLS-1$
	
	private ArrayListTreeMap<String, IBookmarkList> nameToListMap = new ArrayListTreeMap<String, IBookmarkList>();
	private Map<String, IBookmarkList> idToListMap = new HashMap<String, IBookmarkList>();

	private IBookmarkList defaultList;
	
	@PostConstruct
	public void load()
	{
		BookmarksPersister.loadFromWorkspace(this);
	}
	
	@PreDestroy
	public void save()
	{
		BookmarksPersister.saveToWorkspace(this);
	}
	
	public Bookmarks()
	{
		initialiseDefaultList();
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

	@Persistent
	@Override
	public IBookmarkList[] getLists()
	{
		return nameToListMap.flatValues().toArray(new IBookmarkList[0]);
	}

	@Override
	public void setLists(IBookmarkList[] lists)
	{
		idToListMap.clear();
		nameToListMap.clear();
		defaultList = null;
		
		if (lists != null)
		{
			for (IBookmarkList list : lists)
			{
				addList(list);
			}
		}
		
		if (defaultList == null)
		{
			initialiseDefaultList();
		}
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
			IBookmarkList toRemove = idToListMap.get(list.getId());
			nameToListMap.removeSingle(toRemove.getName(), toRemove);
		}
		idToListMap.put(list.getId(), list);
		nameToListMap.putSingle(list.getName(), list);
		
		if (list.getId().equals(DEFAULT_LIST_ID))
		{
			defaultList = list;
		}
	}
	
	private void initialiseDefaultList()
	{
		BookmarkList defaultList = new BookmarkList();
		defaultList.setId(DEFAULT_LIST_ID);
		defaultList.setName(Messages.Bookmarks_DefaultListName); 
		
		addList(defaultList);
		this.defaultList = defaultList;
	}
	
}
