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
package au.gov.ga.earthsci.catalog;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.PersistenceException;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.persistence.Persister;
import au.gov.ga.earthsci.core.util.ConfigurationUtil;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Helper class used to persist the catalog model to an XML file.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogPersister
{
	private CatalogPersister()
	{
	}

	private static final Logger logger = LoggerFactory.getLogger(CatalogPersister.class);

	private static final String ROOT_NODE_NAME = "catalogModel"; //$NON-NLS-1$
	private static final String MODEL_ELEMENT_NAME = "model"; //$NON-NLS-1$
	private static final String CATALOG_NODE_ELEMENT_NAME = "catalog"; //$NON-NLS-1$

	private static final String DEFAULT_WORKSPACE_CATALOG_FILENAME = "catalogs.xml"; //$NON-NLS-1$

	private static final Persister persister;
	static
	{
		persister = new Persister();
		persister.setIgnoreMissing(true);
		persister.setIgnoreNulls(true);

		persister.registerNamedExportable(CatalogModelDTO.class, MODEL_ELEMENT_NAME);
		persister.registerNamedExportable(CatalogNodeDTO.class, CATALOG_NODE_ELEMENT_NAME);
	}

	/**
	 * Save the provided catalog model to the current workspace using the
	 * default name
	 * 
	 * @param model
	 *            The model to save
	 * 
	 * @throws IOException
	 *             If there is a problem writing to the output file
	 * @throws TransformerException
	 *             If there is a problem formatting the XML output
	 * @throws PersistenceException
	 *             If there is a problem persisting the model tree
	 */
	public static void saveToWorkspace(ICatalogModel model)
	{
		if (model == null)
		{
			return;
		}

		try
		{
			saveCatalogModel(model, ConfigurationUtil.getWorkspaceFile(DEFAULT_WORKSPACE_CATALOG_FILENAME));
		}
		catch (Exception e)
		{
			logger.error("Unable to save catalog model to workspace", e); //$NON-NLS-1$
		}
	}

	/**
	 * Save the provided catalog model to the provided file
	 * 
	 * @param model
	 *            The catalog model to save. If <code>null</code> this method
	 *            will have no effect.
	 * @param file
	 *            The file to save the model to. Cannot be <code>null</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             If the output file is <code>null</code>
	 * @throws IOException
	 *             If there is a problem writing to the output file
	 * @throws TransformerException
	 *             If there is a problem formatting the XML output
	 * @throws PersistenceException
	 *             If there is a problem persisting the model tree
	 */
	public static void saveCatalogModel(ICatalogModel model, File file) throws IOException, TransformerException,
			PersistenceException
	{
		if (model == null)
		{
			return;
		}
		Validate.notNull(file, "An output file is required"); //$NON-NLS-1$

		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(file);
			saveCatalogModel(model, os);
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
	 * Save the provided catalog model to the provided output stream
	 * 
	 * @param model
	 *            The catalog model to save. If <code>null</code> this method
	 *            will have no effect.
	 * @param os
	 *            The output stream to save to. Must be non-<code>null</code>
	 *            and writable.
	 * 
	 * @throws IllegalArgumentException
	 *             If the output stream is <code>null</code>
	 * @throws IOException
	 *             If there is a problem writing to the output stream
	 * @throws TransformerException
	 *             If there is a problem formatting the document tree
	 * @throws PersistenceException
	 *             If there is a problem persisting the model tree
	 */
	public static void saveCatalogModel(ICatalogModel model, OutputStream os) throws IOException, TransformerException,
			PersistenceException
	{
		if (model == null)
		{
			return;
		}
		Validate.notNull(os, "An output stream is required"); //$NON-NLS-1$

		DocumentBuilder documentBuilder = WWXML.createDocumentBuilder(false);
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(ROOT_NODE_NAME);
		document.appendChild(element);

		saveCatalogModel(model, element);

		XmlUtil.saveDocumentToFormattedStream(document, os);
	}

	/**
	 * Save the provided catalog model as XML children of the provided parent
	 * element
	 * 
	 * @param model
	 *            The model to save. If <code>null</code>, this method will have
	 *            no effect.
	 * @param parentElement
	 *            The parent XML element to save the model into
	 * 
	 * @throws IllegalArgumentException
	 *             If the parent element is <code>null</code>
	 * @throws PersistenceException
	 *             If there is a problem persisting the model tree
	 */
	public static void saveCatalogModel(ICatalogModel model, Element parentElement) throws PersistenceException
	{
		if (model == null)
		{
			return;
		}
		Validate.notNull(parentElement, "A parent element is required"); //$NON-NLS-1$

		persister.save(new CatalogModelDTO(model), parentElement, null);
	}

	/**
	 * Load the catalog model from the current workspace, if it is available, or
	 * return a new empty model.
	 * 
	 * @param result
	 *            The model to add the loaded catalog nodes to and return. If
	 *            null, a new model is created.
	 * @param context
	 *            An Eclipse context
	 * 
	 * @return The loaded catalog model
	 * 
	 * @throws IllegalArgumentException
	 *             If the provided source file is <code>null</code>
	 * @throws SAXException
	 *             If there is a problem parsing the XML document
	 * @throws IOException
	 *             If there is a problem reading from the source file
	 * @throws PersistenceException
	 *             If there is a problem recreating the model tree from the
	 *             persistence mechanism
	 */
	public static ICatalogModel loadFromWorkspace(ICatalogModel result, IEclipseContext context)
	{
		File workspaceFile = ConfigurationUtil.getWorkspaceFile(DEFAULT_WORKSPACE_CATALOG_FILENAME);
		if (!workspaceFile.exists())
		{
			logger.debug("No catalog model file found in workspace. Creating new model."); //$NON-NLS-1$
			return new CatalogModel();
		}
		try
		{
			return loadCatalogModel(workspaceFile, result, context, false);
		}
		catch (Exception e)
		{
			logger.debug("Unable to load catalog model from workspace", e); //$NON-NLS-1$
			return new CatalogModel();
		}

	}

	/**
	 * Load a catalog model from the provided source file
	 * 
	 * @param source
	 *            The file to load the catalog model from. Must be non-
	 *            <code>null</code>.
	 * @param result
	 *            The model to add the loaded catalog nodes to and return. If
	 *            null, a new model is created.
	 * @param context
	 *            An Eclipse context
	 * 
	 * @return The loaded catalog model
	 * 
	 * @throws IllegalArgumentException
	 *             If the provided source file is <code>null</code>
	 * @throws SAXException
	 *             If there is a problem parsing the XML document
	 * @throws IOException
	 *             If there is a problem reading from the source file
	 * @throws PersistenceException
	 *             If there is a problem recreating the model tree from the
	 *             persistence mechanism
	 */
	public static ICatalogModel loadCatalogModel(File source, ICatalogModel result, IEclipseContext context,
			boolean onlyAddUniqueUris)
			throws SAXException, IOException, PersistenceException
	{
		Validate.notNull(source, "An input file is required"); //$NON-NLS-1$

		FileInputStream is = null;
		try
		{
			is = new FileInputStream(source);
			return loadCatalogModel(is, result, context, onlyAddUniqueUris);
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
	 * Load a previously saved catalog model from the provided input stream.
	 * 
	 * @param is
	 *            The input stream to load from. Must be non-<code>null</code>.
	 * @param result
	 *            The model to add the loaded catalog nodes to and return. If
	 *            null, a new model is created.
	 * @param context
	 *            An Eclipse context
	 * 
	 * @return The loaded catalog model
	 * 
	 * @throws IllegalArgumentException
	 *             If the input stream is <code>null</code>
	 * @throws SAXException
	 *             If there is a problem parsing the XML
	 * @throws IOException
	 *             If there is a problem reading from the input stream
	 * @throws PersistenceException
	 *             If there is a problem recreating the model from the
	 *             persistence mechanism
	 */
	public static ICatalogModel loadCatalogModel(InputStream is, ICatalogModel result, IEclipseContext context,
			boolean onlyAddUniqueUris)
			throws SAXException, IOException, PersistenceException
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
		return loadCatalogModel(element, result, context, onlyAddUniqueUris);
	}

	/**
	 * Load a previously saved catalog model from the provided parent XML
	 * element.
	 * 
	 * @param parentElement
	 *            The parent element to load the catalog model from. Must be
	 *            non-<code>null</code>.
	 * @param result
	 *            The model to add the loaded catalog nodes to and return. If
	 *            null, a new model is created.
	 * @param context
	 *            An Eclipse context
	 * @param onlyAddUniqueUris
	 *            Only add catalogs if their URIs don't already exist in the
	 *            catalog
	 * 
	 * @return The loaded catalog model
	 * 
	 * @throws IllegalArgumentException
	 *             If the parent element is <code>null</code>
	 * @throws PersistenceException
	 *             If there is a problem recreating the model from the
	 *             persistence mechanism
	 */
	public static ICatalogModel loadCatalogModel(Element parentElement, ICatalogModel result, IEclipseContext context,
			boolean onlyAddUniqueUris)
			throws PersistenceException
	{
		Validate.notNull(parentElement, "A parent XML element is required"); //$NON-NLS-1$

		CatalogModelDTO dto = (CatalogModelDTO) persister.load(parentElement, null);

		if (result == null)
		{
			result = new CatalogModel();
		}

		for (int i = 0; i < dto.catalogs.length; i++)
		{
			URI uri = dto.catalogs[i].nodeURI;
			if (uri != null)
			{
				if (onlyAddUniqueUris && result.containsTopLevelCatalogURI(uri))
				{
					continue;
				}
				LoadingCatalogTreeNode loadingNode = new LoadingCatalogTreeNode(uri);
				loadingNode.setLabel(dto.catalogs[i].label);
				result.addTopLevelCatalog(loadingNode);
				IntentCatalogLoader.load(uri, loadingNode, context);
			}
		}

		return result;
	}

	/**
	 * A simple DTO that captures the state of a {@link ICatalogModel} required
	 * for persistence.
	 * <p/>
	 * Used to simplify the persisting / restoring of catalogs
	 */
	@Exportable
	private static class CatalogModelDTO
	{
		@Persistent
		private CatalogNodeDTO[] catalogs;

		public CatalogModelDTO(final ICatalogModel model)
		{
			List<ICatalogTreeNode> topLevelCatalogs = model.getTopLevelCatalogs();

			catalogs = new CatalogNodeDTO[topLevelCatalogs.size()];
			for (int i = 0; i < topLevelCatalogs.size(); i++)
			{
				catalogs[i] = new CatalogNodeDTO(topLevelCatalogs.get(i));
			}
		}

		@SuppressWarnings("unused")
		public CatalogModelDTO()
		{
			// For persistence mechanism only
		}
	}


	/**
	 * A simple DTO that captures the state of a {@link ICatalogTreeNode}
	 * required for persistence.
	 * <p/>
	 * Used to simplify the persisting / restoring of catalogs
	 */
	@Exportable
	private static class CatalogNodeDTO
	{
		@Persistent(attribute = true)
		private String label;

		@Persistent(attribute = true, name = "uri")
		private URI nodeURI;

		public CatalogNodeDTO(final ICatalogTreeNode node)
		{
			this.label = node.getLabel();
			this.nodeURI = node.getURI();
		}


		@SuppressWarnings("unused")
		public CatalogNodeDTO()
		{
			// For persistence mechanism only
		}
	}
}
