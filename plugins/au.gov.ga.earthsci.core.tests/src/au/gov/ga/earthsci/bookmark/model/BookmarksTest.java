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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.earthsci.test.util.DummyRealm;

/**
 * Unit tests for the {@link Bookmarks} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarksTest
{
	private Bookmarks classUnderTest;

	@BeforeClass
	public static void init()
	{
		if (Realm.getDefault() == null)
		{
			DummyRealm.init();
		}
	}
	
	@Before
	public void setup()
	{
		classUnderTest = new Bookmarks();
	}
	
	@Test
	public void testEmptyDefaultListExistsOnCreate()
	{
		assertNotNull(classUnderTest.getDefaultList());
		assertTrue(classUnderTest.getDefaultList().getBookmarks().isEmpty());
		assertTrue(classUnderTest.getLists().length == 1);
		
		assertEquals(classUnderTest.getDefaultList(), classUnderTest.getLists()[0]);
		assertEquals(classUnderTest.getDefaultList(), classUnderTest.getListById(classUnderTest.getDefaultList().getId()));
		assertEquals(classUnderTest.getDefaultList(), classUnderTest.getListByName(classUnderTest.getDefaultList().getName()));
	}
	
	@Test
	public void testAddListWithNull()
	{
		assertTrue(classUnderTest.getLists().length == 1);
		
		classUnderTest.addList(null);
		
		assertTrue(classUnderTest.getLists().length == 1);
	}
	
	@Test
	public void testAddListWithNonNullNonDuplicateIdNonDuplicateName()
	{
		BookmarkList newList = new BookmarkList("id", "name");
		
		assertTrue(classUnderTest.getLists().length == 1);
		
		classUnderTest.addList(newList);
		
		assertTrue(classUnderTest.getLists().length == 2);
		
		assertEquals(newList, classUnderTest.getListById(newList.getId()));
		assertEquals(newList, classUnderTest.getListByName(newList.getName()));
		assertTrue(Arrays.asList(classUnderTest.getLists()).contains(newList));
	}
	
	@Test
	public void testAddListWithNonNullNonDuplicateIdDuplicateName()
	{
		BookmarkList newList = new BookmarkList("id", "name");
		BookmarkList sameNameList = new BookmarkList("id2", "name");
		
		assertTrue(classUnderTest.getLists().length == 1);
		
		classUnderTest.addList(newList);
		classUnderTest.addList(sameNameList);
		
		assertTrue(classUnderTest.getLists().length == 3);
		
		assertEquals(newList, classUnderTest.getListById(newList.getId()));
		assertEquals(sameNameList, classUnderTest.getListById(sameNameList.getId()));
		
		assertEquals(newList, classUnderTest.getListByName(newList.getName()));
		
		assertTrue(Arrays.asList(classUnderTest.getLists()).contains(newList));
		assertTrue(Arrays.asList(classUnderTest.getLists()).contains(sameNameList));
	}
	
	@Test
	public void testAddListWithNonNullDuplicateId()
	{
		BookmarkList newList = new BookmarkList("id", "name");
		BookmarkList sameIdList = new BookmarkList("id", "name2");
		
		assertTrue(classUnderTest.getLists().length == 1);
		
		classUnderTest.addList(newList);
		classUnderTest.addList(sameIdList);
		
		assertTrue(classUnderTest.getLists().length == 2);
		
		assertEquals(sameIdList, classUnderTest.getListById(sameIdList.getId()));
		assertEquals(sameIdList, classUnderTest.getListById(newList.getId()));
		
		assertEquals(null, classUnderTest.getListByName(newList.getName()));
		assertEquals(sameIdList, classUnderTest.getListByName(sameIdList.getName()));
		
		assertFalse(Arrays.asList(classUnderTest.getLists()).contains(newList));
		assertTrue(Arrays.asList(classUnderTest.getLists()).contains(sameIdList));
	}
	
	@Test
	public void testAddListWithDefaultListDuplicateId()
	{
		IBookmarkList originalDefaultList = classUnderTest.getDefaultList();
		BookmarkList newDefaultList = new BookmarkList(originalDefaultList.getId(), "new default");
		
		assertTrue(classUnderTest.getLists().length == 1);
		
		classUnderTest.addList(newDefaultList);
		
		assertTrue(classUnderTest.getLists().length == 1);
		
		assertEquals(newDefaultList, classUnderTest.getDefaultList());
		
		assertEquals(newDefaultList, classUnderTest.getListById(newDefaultList.getId()));
		assertEquals(newDefaultList, classUnderTest.getListById(originalDefaultList.getId()));
		
		assertEquals(null, classUnderTest.getListByName(originalDefaultList.getName()));
		assertEquals(newDefaultList, classUnderTest.getListByName(newDefaultList.getName()));
		
		assertFalse(Arrays.asList(classUnderTest.getLists()).contains(originalDefaultList));
		assertTrue(Arrays.asList(classUnderTest.getLists()).contains(newDefaultList));
	}
	
	@Test
	public void testRemoveListWithNull()
	{
		IBookmarkList[] lists = new IBookmarkList[] {new BookmarkList("id0", "name0"),
													 new BookmarkList("id1", "name1")};
		
		classUnderTest.setLists(lists);
		
		assertTrue(classUnderTest.getLists().length == 3);
		
		assertFalse(classUnderTest.removeList(null));
		
		assertTrue(classUnderTest.getLists().length == 3);
	}
	
	@Test
	public void testRemoveListWithIncluded()
	{
		IBookmarkList[] lists = new IBookmarkList[] {new BookmarkList("id0", "name0"),
													 new BookmarkList("id1", "name1")};
		
		classUnderTest.setLists(lists);
		
		assertTrue(classUnderTest.getLists().length == 3);
		
		assertTrue(classUnderTest.removeList(lists[0]));
		
		assertTrue(classUnderTest.getLists().length == 2);
	}
	
	@Test
	public void testRemoveListWithNonIncluded()
	{
		IBookmarkList[] lists = new IBookmarkList[] {new BookmarkList("id0", "name0"),
													 new BookmarkList("id1", "name1")};
		
		classUnderTest.setLists(lists);
		
		assertTrue(classUnderTest.getLists().length == 3);
		
		assertFalse(classUnderTest.removeList(new BookmarkList("id2", "name2")));
		
		assertTrue(classUnderTest.getLists().length == 3);
	}
	
	@Test
	public void testRemoveListWithDefault()
	{
		IBookmarkList[] lists = new IBookmarkList[] {new BookmarkList("id0", "name0"),
													 new BookmarkList("id1", "name1")};
		
		classUnderTest.setLists(lists);
		
		assertTrue(classUnderTest.getLists().length == 3);
		
		assertFalse(classUnderTest.removeList(classUnderTest.getDefaultList()));
		
		assertTrue(classUnderTest.getLists().length == 3);
	}
	
	@Test
	public void testGetListByIdWithNull()
	{
		assertNull(classUnderTest.getListById(null));
	}
	
	@Test
	public void testGetListByIdWithExistingId()
	{
		assertNotNull(classUnderTest.getListById(classUnderTest.getDefaultList().getId()));
	}
	
	@Test
	public void testGetListByIdWithNonExistingId()
	{
		assertNull(classUnderTest.getListById("dummy"));
	}
	
	@Test
	public void testSetListsWithNull()
	{
		IBookmarkList originalDefaultList = classUnderTest.getDefaultList();
		
		classUnderTest.setLists(null);
		
		assertTrue(classUnderTest.getLists().length == 1);
		
		assertNotNull(classUnderTest.getDefaultList());
		assertTrue(originalDefaultList != classUnderTest.getDefaultList());
		
		assertFalse(Arrays.asList(classUnderTest.getLists()).contains(originalDefaultList));
		assertTrue(Arrays.asList(classUnderTest.getLists()).contains(classUnderTest.getDefaultList()));
	}

	@Test
	public void testSetListsWithEmpty()
	{
		IBookmarkList originalDefaultList = classUnderTest.getDefaultList();
		
		classUnderTest.setLists(new IBookmarkList[0]);
		
		assertTrue(classUnderTest.getLists().length == 1);
		
		assertNotNull(classUnderTest.getDefaultList());
		assertTrue(originalDefaultList != classUnderTest.getDefaultList());
		
		assertFalse(Arrays.asList(classUnderTest.getLists()).contains(originalDefaultList));
		assertTrue(Arrays.asList(classUnderTest.getLists()).contains(classUnderTest.getDefaultList()));
	}
	
	@Test
	public void testSetListsWithNonEmptyNoDefault()
	{
		IBookmarkList[] newLists = new IBookmarkList[] {new BookmarkList("id0", "name0"), new BookmarkList("id1", "name1"), new BookmarkList("id2", "name2")};
		
		IBookmarkList originalDefaultList = classUnderTest.getDefaultList();
		
		classUnderTest.setLists(newLists);
		
		assertTrue(classUnderTest.getLists().length == 4);
		
		assertNotNull(classUnderTest.getDefaultList());
		assertTrue(originalDefaultList != classUnderTest.getDefaultList());
		
		List<IBookmarkList> lists = Arrays.asList(classUnderTest.getLists());
		assertFalse(lists.contains(originalDefaultList));
		assertTrue(lists.contains(newLists[0]));
		assertTrue(lists.contains(newLists[1]));
		assertTrue(lists.contains(newLists[2]));
	}
}
