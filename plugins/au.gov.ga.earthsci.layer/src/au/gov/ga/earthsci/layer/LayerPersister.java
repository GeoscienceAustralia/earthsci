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
package au.gov.ga.earthsci.layer;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.gov.ga.earthsci.common.persistence.PersistenceException;
import au.gov.ga.earthsci.common.persistence.Persister;
import au.gov.ga.earthsci.common.util.XmlUtil;

/**
 * Helper class used to save the layer tree hierarchy to an XML file, using the
 * {@link Persister}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerPersister
{
	private static Persister persister;

	static
	{
		persister = new Persister();
		persister.setIgnoreMissing(true);
		persister.setIgnoreNulls(true);
		persister.registerNamedExportable(FolderNode.class, "Folder"); //$NON-NLS-1$
		persister.registerNamedExportable(LayerNode.class, "Layer"); //$NON-NLS-1$
	}

	/**
	 * Save the given root node (and children) of the layer tree to a file.
	 * 
	 * @param rootNode
	 *            Layer tree root node to save
	 * @param file
	 *            File to save to
	 * @throws IOException
	 * @throws TransformerException
	 * @throws PersistenceException
	 */
	public static void saveLayers(ILayerTreeNode rootNode, File file) throws IOException, TransformerException,
			PersistenceException
	{
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(file);
			saveLayers(rootNode, os);
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
	 * Save the given root node (and children) of the layer tree to an
	 * OutputStream.
	 * 
	 * @param rootNode
	 *            Layer tree root node to save
	 * @param os
	 *            OutputStream to save to
	 * @throws TransformerException
	 * @throws IOException
	 * @throws PersistenceException
	 */
	public static void saveLayers(ILayerTreeNode rootNode, OutputStream os) throws TransformerException, IOException,
			PersistenceException
	{
		DocumentBuilder documentBuilder = WWXML.createDocumentBuilder(false);
		Document document = documentBuilder.newDocument();
		Element element = document.createElement("Layers"); //$NON-NLS-1$
		document.appendChild(element);
		saveLayers(rootNode, element);
		XmlUtil.saveDocumentToFormattedStream(document, os);
	}

	/**
	 * Save the given layers (and children) to an OutputStream.
	 * 
	 * @param array
	 *            Array of layers to save
	 * @param os
	 *            OutputStream to save to
	 * @throws TransformerException
	 * @throws IOException
	 * @throws PersistenceException
	 */
	public static void saveLayerArray(ILayerTreeNode[] array, OutputStream os) throws TransformerException,
			IOException, PersistenceException
	{
		DocumentBuilder documentBuilder = WWXML.createDocumentBuilder(false);
		Document document = documentBuilder.newDocument();
		Element element = document.createElement("Layers"); //$NON-NLS-1$
		document.appendChild(element);
		for (ILayerTreeNode n : array)
		{
			saveLayers(n, element);
		}
		XmlUtil.saveDocumentToFormattedStream(document, os);
	}

	/**
	 * Save the given root node (and children) of the layer tree as a child of
	 * the given XML element.
	 * 
	 * @param rootNode
	 *            Layer tree root node to save
	 * @param element
	 *            Parent element to save under
	 * @throws PersistenceException
	 */
	public static void saveLayers(ILayerTreeNode rootNode, Element element) throws PersistenceException
	{
		persister.save(rootNode, element, null);
	}

	/**
	 * Load the root node (and children) of a layer tree from a file.
	 * 
	 * @param file
	 *            File to load from
	 * @return Root node of the layer tree loaded from file
	 * @throws SAXException
	 * @throws IOException
	 * @throws PersistenceException
	 */
	public static ILayerTreeNode loadLayers(File file) throws SAXException, IOException, PersistenceException
	{
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(file);
			return loadLayers(is);
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
	 * Load the root node (and children) of a layer tree from an InputStream.
	 * 
	 * @param is
	 *            InputStream to load from
	 * @return Root node of the layer tree loaded from the InputStream
	 * @throws SAXException
	 * @throws IOException
	 * @throws PersistenceException
	 */
	public static ILayerTreeNode loadLayers(InputStream is) throws SAXException, IOException, PersistenceException
	{
		Document document = WWXML.createDocumentBuilder(false).parse(is);
		Element parent = document.getDocumentElement();
		Element element = XmlUtil.getFirstChildElement(parent);
		return loadLayers(element);
	}

	/**
	 * Load an array of layers (and children) from an InputStream.
	 * 
	 * @param is
	 *            InputStream to load from
	 * @return Array of layer nodes loaded from the InputStream
	 * @throws SAXException
	 * @throws IOException
	 * @throws PersistenceException
	 */
	public static ILayerTreeNode[] loadLayerArray(InputStream is) throws SAXException, IOException,
			PersistenceException
	{
		Document document = WWXML.createDocumentBuilder(false).parse(is);
		Element parent = document.getDocumentElement();
		Element[] elements = XmlUtil.getElements(parent);
		List<ILayerTreeNode> layers = new ArrayList<ILayerTreeNode>(elements.length);
		for (Element element : elements)
		{
			layers.add(loadLayers(element));
		}
		return layers.toArray(new ILayerTreeNode[layers.size()]);
	}

	/**
	 * Load the root node (and children) of a layer tree from the given XML
	 * element.
	 * 
	 * @param element
	 *            Element to load the layer tree from
	 * @return Root node of the layer tree loaded from the XML element
	 * @throws PersistenceException
	 */
	public static ILayerTreeNode loadLayers(Element element) throws PersistenceException
	{
		return (ILayerTreeNode) persister.load(element, null);
	}
}
