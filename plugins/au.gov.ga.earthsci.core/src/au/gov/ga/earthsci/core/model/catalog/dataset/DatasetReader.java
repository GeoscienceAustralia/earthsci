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
package au.gov.ga.earthsci.core.model.catalog.dataset;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.util.XmlUtil;


/**
 * Helper class that reads legacy dataset.xml structures from a provided source.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetReader
{
	private static final String NAME_ATTRIBUTE = "@name"; //$NON-NLS-1$
	private static final String INFO_ATTRIBUTE = "@info"; //$NON-NLS-1$
	private static final String ICON_ATTRIBUTE = "@icon"; //$NON-NLS-1$
	private static final String URL_ATTRIBUTE = "@url"; //$NON-NLS-1$
	private static final String BASE_ATTRIBUTE = "@base"; //$NON-NLS-1$
	private static final String DEFAULT_ATTRIBUTE = "@default"; //$NON-NLS-1$
	private static final String ENABLED_ATTRIBUTE = "@enabled"; //$NON-NLS-1$
	
	private static final String DATASET_NODE_NAME = "Dataset"; //$NON-NLS-1$
	private static final String LINK_NODE_NAME = "Link"; //$NON-NLS-1$
	private static final String LAYER_NODE_NAME = "Layer"; //$NON-NLS-1$
	
	private static final String VALID_NODES_XPATH = DATASET_NODE_NAME + "|" + LINK_NODE_NAME + "|" + LAYER_NODE_NAME;  //$NON-NLS-1$//$NON-NLS-2$
	private static final String DATASET_LIST_XPATH = "//DatasetList"; //$NON-NLS-1$
	
	/**
	 * Read a dataset file from the given source and return the root node of the dataset
	 * tree defined by the provided source.
	 * <p/>
	 * Note that {@code link} nodes will not be expanded - they will be returned as {@link DatasetLinkCatalogTreeNode}s which can be lazy-expanded.
	 * 
	 * @param source The source to read the dataset structure from. See {@link XmlUtil#getElementFromSource(Object)} for supported sources.
	 * @param context The context URL to use when resolving relative paths. May be <code>null</code>.
	 * 
	 * @return The root node of the dataset tree structure defined in the given source.
	 * 
	 * @throws MalformedURLException
	 */
	public static ICatalogTreeNode read(Object source, URL context) throws MalformedURLException
	{
		//top level dataset (DatasetList) doesn't have a name, and is not shown in the tree
		ICatalogTreeNode root = new DatasetCatalogTreeNode(null, null, null, true);

		Element rootElement = XmlUtil.getElementFromSource(source);
		if (rootElement == null)
		{
			return root;
		}
		
		// Special case
		if (context == null && source instanceof URL)
		{
			context = (URL)source;
		}
		
		Element[] elements = XmlUtil.getElements(rootElement, DATASET_LIST_XPATH, null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				addChildren(element, root, context);
			}
		}
		
		return root;
	}

	private static void addChildren(Element element, ICatalogTreeNode parent, URL context) throws MalformedURLException
	{
		Element[] elements = XmlUtil.getElements(element, VALID_NODES_XPATH, null);
		if (elements == null)
		{
			return;
		}
		
		for (Element e : elements)
		{
			if (isDatasetNode(e))
			{
				ICatalogTreeNode dataset = addDataset(e, parent, context);
				addChildren(e, dataset, context);
			}
			else if (isLinkNode(e))
			{
				addLink(e, parent, context);
			}
			else if (isLayerNode(e))
			{
				addLayer(e, parent, context);
			}
		}
	}

	private static boolean isLayerNode(Element e)
	{
		return e.getNodeName().equalsIgnoreCase(LAYER_NODE_NAME);
	}

	private static boolean isLinkNode(Element e)
	{
		return e.getNodeName().equalsIgnoreCase(LINK_NODE_NAME);
	}

	private static boolean isDatasetNode(Element e)
	{
		return e.getNodeName().equalsIgnoreCase(DATASET_NODE_NAME);
	}

	private static ICatalogTreeNode addDataset(Element element, ICatalogTreeNode parent, URL context) throws MalformedURLException
	{
		String name = XmlUtil.getText(element, NAME_ATTRIBUTE);
		URL info = XmlUtil.getURL(element, INFO_ATTRIBUTE, context);
		URL icon = XmlUtil.getURL(element, ICON_ATTRIBUTE, context);
		boolean base = XmlUtil.getBoolean(element, BASE_ATTRIBUTE, false);
		
		ICatalogTreeNode dataset = new DatasetCatalogTreeNode(name, info, icon, base);
		parent.add(dataset);
		
		return dataset;
	}

	private static void addLink(Element element, ICatalogTreeNode parent, URL context) throws MalformedURLException
	{
		String name = XmlUtil.getText(element, NAME_ATTRIBUTE);
		URL info = XmlUtil.getURL(element, INFO_ATTRIBUTE, context);
		URL icon = XmlUtil.getURL(element, ICON_ATTRIBUTE, context);
		URL url = XmlUtil.getURL(element, URL_ATTRIBUTE, context);
		boolean base = XmlUtil.getBoolean(element, BASE_ATTRIBUTE, false);
		
		ICatalogTreeNode link = new DatasetLinkCatalogTreeNode(name, url, info, icon, base);
		parent.add(link);
	}

	private static void addLayer(Element element, ICatalogTreeNode parent, URL context) throws MalformedURLException
	{
		String name = XmlUtil.getText(element, NAME_ATTRIBUTE);
		URL info = XmlUtil.getURL(element, INFO_ATTRIBUTE, context);
		URL icon = XmlUtil.getURL(element, ICON_ATTRIBUTE, context);
		URL url = XmlUtil.getURL(element, URL_ATTRIBUTE, context);
		
		boolean base = XmlUtil.getBoolean(element, BASE_ATTRIBUTE, false);
		boolean def = XmlUtil.getBoolean(element, DEFAULT_ATTRIBUTE, false);
		boolean enabled = XmlUtil.getBoolean(element, ENABLED_ATTRIBUTE, true);
		
		DatasetLayerCatalogTreeNode layer = new DatasetLayerCatalogTreeNode(name, url, info, icon, base, def, enabled);
		parent.add(layer);
	}
}
