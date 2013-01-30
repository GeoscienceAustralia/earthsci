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
package au.gov.ga.earthsci.bookmark.part;

import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.io.BookmarkPropertyPersistentAdapter;
import au.gov.ga.earthsci.bookmark.model.Bookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.persistence.Persister;
import au.gov.ga.earthsci.core.util.XmlUtil;
import au.gov.ga.earthsci.worldwind.common.util.Validate;


/**
 * Transfer data for a {@link BookmarkTransfer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Exportable
public class BookmarkTransferData
{
	private static final Persister persister;
	static
	{
		persister = new Persister();
		persister.setIgnoreMissing(true);
		persister.setIgnoreNulls(true);
		
		persister.registerNamedExportable(BookmarkTransferData.class, "transferData"); //$NON-NLS-1$
		persister.registerNamedExportable(Bookmark.class, "bookmark"); //$NON-NLS-1$
		
		persister.registerAdapter(IBookmarkProperty.class, new BookmarkPropertyPersistentAdapter());
		persister.registerNamedExportable(IBookmarkProperty.class, "property"); //$NON-NLS-1$
		
	}
	
	/**
	 * Create a new transfer data object that can transfer the given bookmarks
	 * 
	 * @param bookmarks The bookmarks to transfer
	 * 
	 * @return The new transfer data object
	 */
	public static BookmarkTransferData fromBookmarks(IBookmark... bookmarks)
	{
		BookmarkTransferData result = new BookmarkTransferData();
		result.bookmarks = bookmarks;
		return result;
	}
	
	/**
	 * Load transfer data from an input stream
	 * 
	 * @param is The stream to load from
	 * 
	 * @return The transfer data
	 */
	public static BookmarkTransferData load(InputStream is) throws Exception
	{
		Document d = XmlUtil.openDocument(is);
		
		Element root = XmlUtil.getFirstChildElement(d.getDocumentElement());
		
		BookmarkTransferData result = (BookmarkTransferData) persister.load(root, null);
		
		return result;
	}

	/**
	 * Save the given transfer data object into the given output stream
	 * 
	 * @param data The transfer data to save
	 * @param os The output stream to save to
	 */
	public static void save(BookmarkTransferData data, OutputStream os) throws Exception
	{
		if (data == null)
		{
			return;
		}
		
		Validate.notNull(os, "An output stream is required"); //$NON-NLS-1$
		
		Document document = XmlUtil.createDocumentBuilder().newDocument();
		Element element = document.createElement("transfer"); //$NON-NLS-1$
		document.appendChild(element);
		
		persister.save(data, element, null);
		
		XmlUtil.saveDocumentToFormattedStream(document, os);
	}
	
	@Persistent
	private IBookmark[] bookmarks;
	
	private BookmarkTransferData() {}

	public IBookmark[] getBookmarks()
	{
		return bookmarks;
	}
}
