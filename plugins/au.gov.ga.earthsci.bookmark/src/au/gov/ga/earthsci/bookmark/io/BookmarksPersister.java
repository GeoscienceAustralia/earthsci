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
package au.gov.ga.earthsci.bookmark.io;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.gov.ga.earthsci.bookmark.model.Bookmark;
import au.gov.ga.earthsci.bookmark.model.BookmarkList;
import au.gov.ga.earthsci.bookmark.model.Bookmarks;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.model.IBookmarks;
import au.gov.ga.earthsci.common.persistence.PersistenceException;
import au.gov.ga.earthsci.common.persistence.Persister;
import au.gov.ga.earthsci.common.util.ConfigurationUtil;
import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Helper class to persist the bookmarks model to an XML file
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarksPersister
{
	private BookmarksPersister()
	{
	}

	private static final Logger logger = LoggerFactory.getLogger(BookmarksPersister.class);

	private static final String DEFAULT_WORKSPACE_BOOKMARKS_FILENAME = "bookmarks.xml"; //$NON-NLS-1$
	private static final String ROOT_NODE_NAME = "document"; //$NON-NLS-1$

	private static final Persister persister;
	static
	{
		persister = new Persister();
		persister.setIgnoreMissing(true);
		persister.setIgnoreNulls(true);

		persister.registerNamedExportable(Bookmark.class, "bookmark"); //$NON-NLS-1$
		persister.registerNamedExportable(Bookmarks.class, "bookmarks"); //$NON-NLS-1$
		persister.registerNamedExportable(BookmarkList.class, "bookmarkList"); //$NON-NLS-1$

		persister.registerAdapter(IBookmarkProperty.class, new BookmarkPropertyPersistentAdapter());
		persister.registerNamedExportable(IBookmarkProperty.class, "property"); //$NON-NLS-1$
	}

	/**
	 * Save the provided bookmarks to the current workspace using the configured
	 * file name
	 * 
	 * @param bookmarks
	 *            The bookmarks to save. If <code>null</code> this method will
	 *            have no effect.
	 */
	public static void saveToWorkspace(IBookmarks bookmarks)
	{
		if (bookmarks == null)
		{
			return;
		}

		try
		{
			saveBookmarks(bookmarks, ConfigurationUtil.getWorkspaceFile(DEFAULT_WORKSPACE_BOOKMARKS_FILENAME));
		}
		catch (Exception e)
		{
			logger.error("Unable to save bookmarks to workspace", e); //$NON-NLS-1$
		}
	}

	/**
	 * Save the provided bookmarks to the provided file
	 * 
	 * @param bookmarks
	 *            The bookmarks to save. If <code>null</code> this method will
	 *            have no effect.
	 * @param file
	 *            The file to save to. Cannot be <code>null</code>.
	 * 
	 * @throws IOException
	 *             if there is a problem writing to the provided file
	 * @throws TransformerException
	 *             If there is problem formatting the output XML
	 * @throws PersistenceException
	 *             If there is a problem persisting the bookmarks
	 */
	public static void saveBookmarks(IBookmarks bookmarks, File file) throws IOException, TransformerException,
			PersistenceException
	{
		if (bookmarks == null)
		{
			return;
		}
		Validate.notNull(file, "An output file is required"); //$NON-NLS-1$

		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(file);
			saveBookmarks(bookmarks, os);
		}
		finally
		{
			if (os != null)
			{
				os.close();
			}
		}
	}

	/**
	 * Save the provided bookmarks to the provided output stream.
	 * 
	 * @param bookmarks
	 *            The bookmarks to save. If <code>null</code> this method will
	 *            have no effect.
	 * @param os
	 *            The output stream to save to. Must be non-<code>null</code>
	 *            and writable.
	 * 
	 * @throws IOException
	 *             If there is a problem writing to the output stream
	 * @throws TransformerException
	 *             If there is problem formatting the output XML
	 * @throws PersistenceException
	 *             If there is a problem persisting the bookmarks
	 */
	public static void saveBookmarks(IBookmarks bookmarks, OutputStream os) throws IOException, TransformerException,
			PersistenceException
	{
		if (bookmarks == null)
		{
			return;
		}
		Validate.notNull(os, "An output stream is required"); //$NON-NLS-1$

		DocumentBuilder documentBuilder = WWXML.createDocumentBuilder(false);
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(ROOT_NODE_NAME);
		document.appendChild(element);

		saveBookmarks(bookmarks, element);

		XmlUtil.saveDocumentToFormattedStream(document, os);
	}

	/**
	 * Save the provided bookmarks as a child of the provided parent element
	 * 
	 * @param bookmarks
	 *            The bookmarks to save. If <code>null</code> this method has no
	 *            effect.
	 * @param parent
	 *            The parent element under which to save the XML output
	 * 
	 * @throws PersistenceException
	 *             If there is a problem persisting the bookmarks
	 */
	public static void saveBookmarks(IBookmarks bookmarks, Element parent) throws PersistenceException
	{
		if (bookmarks == null)
		{
			return;
		}
		Validate.notNull(parent, "A parent element is required"); //$NON-NLS-1$

		persister.save(bookmarks, parent, null);
	}

	/**
	 * Load any persisted bookmarks in the current workspace using the
	 * configured bookmarks file name.
	 * <p/>
	 * If a target instance is provided it will be populated with the loaded
	 * bookmarks data. Otherwise a new instance will be created and returned.
	 * 
	 * @param target
	 *            The target instance to populate with bookmark data. If
	 *            <code>null</code> a new instance will be created.
	 * 
	 * @return the loaded bookmarks data
	 */
	public static IBookmarks loadFromWorkspace(IBookmarks target)
	{
		File workspaceFile = ConfigurationUtil.getWorkspaceFile(DEFAULT_WORKSPACE_BOOKMARKS_FILENAME);
		if (!workspaceFile.exists())
		{
			logger.debug("No bookmarks file found in workspace. Creating new model."); //$NON-NLS-1$
			if (target == null)
			{
				return new Bookmarks();
			}
			return target;
		}
		try
		{
			return loadBookmarks(workspaceFile, target);
		}
		catch (Exception e)
		{
			logger.error("Unable to load bookmarks from workspace", e); //$NON-NLS-1$
		}
		return target;
	}

	/**
	 * Load persisted bookmarks from the given file.
	 * <p/>
	 * If a target instance is provided it will be populated with the loaded
	 * bookmarks data. Otherwise a new instance will be created and returned.
	 * 
	 * @param file
	 *            The file to load bookmarks from. Must be non-<code>null</code>
	 *            .
	 * @param target
	 *            The target instance to populate with bookmark data. If
	 *            <code>null</code> a new instance will be created.
	 * 
	 * @return the loaded bookmarks data
	 * 
	 * @throws PersistenceException
	 *             If there is a problem un-persisting from the XML
	 * @throws SAXException
	 *             If there is a problem parsing the XML
	 * @throws IOException
	 *             If there is a problem reading from the file
	 */
	public static IBookmarks loadBookmarks(File file, IBookmarks target) throws IOException, SAXException,
			PersistenceException
	{
		Validate.notNull(file, "An input file is required"); //$NON-NLS-1$

		FileInputStream is = null;
		try
		{
			is = new FileInputStream(file);
			return loadBookmarks(is, target);
		}
		finally
		{
			if (is != null)
			{
				is.close();
			}
		}
	}

	/**
	 * Load persisted bookmarks from the given input stream.
	 * <p/>
	 * If a target instance is provided it will be populated with the loaded
	 * bookmarks data. Otherwise a new instance will be created and returned.
	 * 
	 * @param is
	 *            The input stream to load bookmarks from. Must be non-
	 *            <code>null</code> and readable.
	 * @param target
	 *            The target instance to populate with bookmark data. If
	 *            <code>null</code> a new instance will be created.
	 * 
	 * @return the loaded bookmarks data
	 * 
	 * @throws PersistenceException
	 *             If there is a problem un-persisting from the XML
	 * @throws SAXException
	 *             If there is a problem parsing the XML
	 * @throws IOException
	 *             If there is a problem reading from the stream
	 */
	public static IBookmarks loadBookmarks(InputStream is, IBookmarks target) throws PersistenceException,
			SAXException, IOException
	{
		Validate.notNull(is, "An input stream is required"); //$NON-NLS-1$

		Document document = WWXML.createDocumentBuilder(false).parse(is);
		Element parent = document.getDocumentElement();
		if (!ROOT_NODE_NAME.equals(parent.getNodeName()))
		{
			throw new PersistenceException(
					"Provided document is not a valid catalog model document. Expected root node " + ROOT_NODE_NAME + " but found " + parent.getNodeName()); //$NON-NLS-1$//$NON-NLS-2$
		}
		Element element = XmlUtil.getFirstChildElement(parent);
		return loadBookmarks(element, target);
	}

	/**
	 * Load persisted bookmarks from the given input stream.
	 * <p/>
	 * If a target instance is provided it will be populated with the loaded
	 * bookmarks data. Otherwise a new instance will be created and returned.
	 * 
	 * @param element
	 *            The root XML element to load from
	 * @param target
	 *            The target instance to populate with bookmark data. If
	 *            <code>null</code> a new instance will be created.
	 * 
	 * @return the loaded bookmarks data
	 * 
	 * @throws PersistenceException
	 *             If there is a problem un-persisting from the XML
	 * @throws SAXException
	 *             If there is a problem parsing the XML
	 * @throws IOException
	 *             If there is a problem reading from the stream
	 */
	public static IBookmarks loadBookmarks(Element element, IBookmarks target) throws PersistenceException
	{
		IBookmarks loaded = (IBookmarks) persister.load(element, null);

		if (target == null)
		{
			return loaded;
		}

		target.setLists(loaded.getLists());
		return target;
	}
}
