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
package au.gov.ga.earthsci.catalog.dataset;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.common.util.UTF8URLEncoder;
import au.gov.ga.earthsci.common.util.XmlUtil;


/**
 * Helper class that reads legacy dataset.xml structures from a provided source.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
final public class DatasetReader
{
	private DatasetReader()
	{
	}

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

	private static final String VALID_NODES_XPATH = DATASET_NODE_NAME + "|" + LINK_NODE_NAME + "|" + LAYER_NODE_NAME; //$NON-NLS-1$//$NON-NLS-2$
	private static final String DATASET_LIST_XPATH = "//DatasetList"; //$NON-NLS-1$

	/**
	 * Read a dataset file from the given source and return the root node of the
	 * dataset tree defined by the provided source.
	 * <p/>
	 * Note that {@code link} nodes will not be expanded - they will be returned
	 * as {@link DatasetLinkCatalogTreeNode}s which can be lazy-expanded.
	 * 
	 * @param source
	 *            The source to read the dataset structure from. See
	 *            {@link XmlUtil#getElementFromSource(Object)} for supported
	 *            sources.
	 * @param context
	 *            The context URL to use when resolving relative paths. May be
	 *            <code>null</code>.
	 * 
	 * @return The root node of the dataset tree structure defined in the given
	 *         source.
	 * 
	 * @throws MalformedURLException
	 */
	public static ICatalogTreeNode read(final Object source, final URL context) throws MalformedURLException,
			URISyntaxException
	{

		final Element rootElement = XmlUtil.getElementFromSource(source);
		if (rootElement == null)
		{
			return new DatasetCatalogTreeNode(null, getRootNodeName(source, context), null, null, true, true);
		}

		// Special case
		URL theContext = context;
		if (context == null && source instanceof URL)
		{
			theContext = (URL) source;
		}

		final ICatalogTreeNode root =
				new DatasetCatalogTreeNode(theContext.toURI(), getRootNodeName(source, context), null, null, true, true);

		final Element[] elements = XmlUtil.getElements(rootElement, DATASET_LIST_XPATH, null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				addChildren(element, root, theContext);
			}
		}

		return root;
	}

	private static String getRootNodeName(Object source, URL context)
	{
		if (source instanceof File)
		{
			return ((File) source).getAbsolutePath();
		}
		if (source instanceof URI)
		{
			return UTF8URLEncoder.decode(((URI) source).toASCIIString());
		}
		if (source instanceof URL)
		{
			return UTF8URLEncoder.decode(((URL) source).toExternalForm());
		}
		if (context != null)
		{
			return UTF8URLEncoder.decode(context.toExternalForm());
		}
		return Messages.DatasetReader_DefaultRootNodeName;
	}

	private static void addChildren(final Element element, final ICatalogTreeNode parent, final URL context)
			throws MalformedURLException, URISyntaxException
	{
		final Element[] elements = XmlUtil.getElements(element, VALID_NODES_XPATH, null);
		if (elements == null)
		{
			return;
		}

		for (Element e : elements)
		{
			if (isDatasetNode(e))
			{
				final ICatalogTreeNode dataset = addDataset(e, parent, context);
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

	private static boolean isLayerNode(final Element element)
	{
		return element.getNodeName().equalsIgnoreCase(LAYER_NODE_NAME);
	}

	private static boolean isLinkNode(final Element element)
	{
		return element.getNodeName().equalsIgnoreCase(LINK_NODE_NAME);
	}

	private static boolean isDatasetNode(final Element element)
	{
		return element.getNodeName().equalsIgnoreCase(DATASET_NODE_NAME);
	}

	private static ICatalogTreeNode addDataset(final Element element, final ICatalogTreeNode parent, final URL context)
			throws MalformedURLException, URISyntaxException
	{
		final String name = XmlUtil.getText(element, NAME_ATTRIBUTE);
		final URL info = XmlUtil.getURL(element, INFO_ATTRIBUTE, context);
		final URL icon = XmlUtil.getURL(element, ICON_ATTRIBUTE, context);
		final boolean base = XmlUtil.getBoolean(element, BASE_ATTRIBUTE, false);

		URI nodeURI = buildChildURI(parent.getURI(), UTF8URLEncoder.encode(name));

		final ICatalogTreeNode dataset = new DatasetCatalogTreeNode(nodeURI, name, info, icon, base);
		parent.addChild(dataset);

		return dataset;
	}

	private static void addLink(final Element element, final ICatalogTreeNode parent, final URL context)
			throws MalformedURLException, URISyntaxException
	{
		final String name = XmlUtil.getText(element, NAME_ATTRIBUTE);
		final URL info = XmlUtil.getURL(element, INFO_ATTRIBUTE, context);
		final URL icon = XmlUtil.getURL(element, ICON_ATTRIBUTE, context);
		final URL url = XmlUtil.getURL(element, URL_ATTRIBUTE, context);
		final boolean base = XmlUtil.getBoolean(element, BASE_ATTRIBUTE, false);

		URI nodeURI = buildChildURI(parent.getURI(), url.toExternalForm());

		final ICatalogTreeNode link = new DatasetLinkCatalogTreeNode(nodeURI, name, url, info, icon, base);
		parent.addChild(link);
	}

	private static void addLayer(final Element element, final ICatalogTreeNode parent, final URL context)
			throws MalformedURLException, URISyntaxException
	{
		final String name = XmlUtil.getText(element, NAME_ATTRIBUTE);
		final URL info = XmlUtil.getURL(element, INFO_ATTRIBUTE, context);
		final URL icon = XmlUtil.getURL(element, ICON_ATTRIBUTE, context);
		final URL url = XmlUtil.getURL(element, URL_ATTRIBUTE, context);

		final boolean base = XmlUtil.getBoolean(element, BASE_ATTRIBUTE, false);
		final boolean def = XmlUtil.getBoolean(element, DEFAULT_ATTRIBUTE, false);
		final boolean enabled = XmlUtil.getBoolean(element, ENABLED_ATTRIBUTE, true);

		URI nodeURI = buildChildURI(parent.getURI(), url.toExternalForm());

		final DatasetLayerCatalogTreeNode layer =
				new DatasetLayerCatalogTreeNode(nodeURI, name, url, info, icon, base, def, enabled);
		parent.addChild(layer);
	}

	private static URI buildChildURI(URI parentURI, String childPath) throws URISyntaxException
	{
		return new URI(parentURI.toASCIIString() + "/" + childPath); //$NON-NLS-1$
	}
}
