/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.bookmark.io;

import static au.gov.ga.earthsci.core.util.XmlUtil.getChildElementByTagName;
import static au.gov.ga.earthsci.core.util.XmlUtil.getText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.databinding.observable.Realm;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.model.Bookmark;
import au.gov.ga.earthsci.bookmark.model.Bookmarks;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarks;
import au.gov.ga.earthsci.test.util.DummyRealm;

/**
 * Unit test for the {@link BookmarksPersister} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarksPersisterTest
{
	
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
		
	}
	
	@Test
	public void testSaveBookmarksToXMLNullBookmarks() throws Exception
	{
		IBookmarks bookmarks = null;
		Element parent = createDocument();
		
		BookmarksPersister.saveBookmarks(bookmarks, parent);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSaveBookmarksToXMLNullElement() throws Exception
	{
		IBookmarks bookmarks = new Bookmarks();
		Element parent = null;
		
		BookmarksPersister.saveBookmarks(bookmarks, parent);
	}
	
	@Test
	public void testSaveBookmarksToXMLEmptyBookmarks() throws Exception
	{
		IBookmarks bookmarks = new Bookmarks();
		Element parent = createDocument();
		
		BookmarksPersister.saveBookmarks(bookmarks, parent);
		
		assertEquals(1, parent.getChildNodes().getLength());
		
		// Expect single bookmarks element
		Element element = getChildElementByTagName(0, "bookmarks", parent);
		assertNotNull(element);
		assertEquals(1, element.getChildNodes().getLength());
		
		// With a single lists element
		element = getChildElementByTagName(0, "lists", element);
		assertNotNull(element);
		assertEquals(1, element.getChildNodes().getLength());
		
		// Containing a single (default) list
		element = getChildElementByTagName(0, "element", element);
		assertNotNull(element);
		element = getChildElementByTagName(0, "bookmarkList", element);
		assertNotNull(element);
		assertEquals(bookmarks.getDefaultList().getId(), element.getAttribute("id"));
		
		// With no bookmarks
		element = getChildElementByTagName(0, "bookmarks", element);
		assertNotNull(element);
		assertEquals(0, element.getChildNodes().getLength());
	}

	@Test
	public void testSaveBookmarksToXMLSingleBookmarkNoProperties() throws Exception
	{
		IBookmarks bookmarks = new Bookmarks();
		IBookmark bookmark = new Bookmark();
		bookmarks.getDefaultList().getBookmarks().add(bookmark);
		
		Element parent = createDocument();
		
		BookmarksPersister.saveBookmarks(bookmarks, parent);
		
		// Expect single bookmarks element
		Element element = getChildElementByTagName(0, "bookmarks", parent);
		assertNotNull(element);
		assertEquals(1, element.getChildNodes().getLength());
		
		// With a single lists element
		element = getChildElementByTagName(0, "lists", element);
		assertNotNull(element);
		assertEquals(1, element.getChildNodes().getLength());
		
		// Containing a single (default) list
		element = getChildElementByTagName(0, "element", element);
		assertNotNull(element);
		element = getChildElementByTagName(0, "bookmarkList", element);
		assertNotNull(element);
		assertEquals(bookmarks.getDefaultList().getId(), element.getAttribute("id"));
		
		// With a single bookmark
		element = getChildElementByTagName(0, "bookmarks", element);
		assertNotNull(element);
		assertEquals(1, element.getChildNodes().getLength());
		
		element = getChildElementByTagName(0, "element", element);
		element = getChildElementByTagName(0, "bookmark", element);
		
		assertEquals(bookmark.getId(), getText(element, "id"));
		assertEquals(bookmark.getName(), getText(element, "name"));
	}
	
	private Element createDocument() throws Exception
	{
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		return d.createElement("document");
	}
	
}
