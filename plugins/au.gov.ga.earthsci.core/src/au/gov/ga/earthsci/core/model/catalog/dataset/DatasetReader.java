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


/**
 * Helper class that reads {@link IDataset}s from XML files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetReader
{
	/*public static IDataset read(Object source, URL context) throws MalformedURLException
	{
		//top level dataset (DatasetList) doesn't have a name, and is not shown in the tree
		IDataset root = new Dataset(null, null, null, true);

		Element elem = XMLUtil.getElementFromSource(source);
		if (elem != null)
		{
			Element[] elements = XMLUtil.getElements(elem, "//DatasetList", null);
			if (elements != null)
			{
				for (Element element : elements)
				{
					addRelevant(element, root, context);
				}
			}
		}

		return root;
	}

	private static void addRelevant(Element element, IDataset parent, URL context) throws MalformedURLException
	{
		Element[] elements = XMLUtil.getElements(element, "Dataset|Link|Layer", null);
		if (elements != null)
		{
			for (Element e : elements)
			{
				if (e.getNodeName().equals("Dataset"))
				{
					IDataset dataset = addDataset(e, parent, context);
					addRelevant(e, dataset, context);
				}
				else if (e.getNodeName().equals("Link"))
				{
					addLink(e, parent, context);
				}
				else if (e.getNodeName().equals("Layer"))
				{
					addLayer(e, parent, context);
				}
			}
		}
	}

	private static IDataset addDataset(Element element, ICatalogTreeNode parent, URL context) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL info = XMLUtil.getURL(element, "@info", context);
		URL icon = XMLUtil.getURL(element, "@icon", context);
		
		//boolean base = XMLUtil.getBoolean(element, "@base", false);
		
		ICatalogTreeNode dataset = new DatasetCatalogTreeNode(name, info, icon);
		parent.add(dataset);
		return dataset;
	}

	private static void addLink(Element element, ICatalogTreeNode parent, URL context) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL info = XMLUtil.getURL(element, "@info", context);
		URL icon = XMLUtil.getURL(element, "@icon", context);
		URL url = XMLUtil.getURL(element, "@url", context);
		
		//boolean base = XMLUtil.getBoolean(element, "@base", false);
		
		ICatalogTreeNode dataset = new LazyDataset(name, url, info, icon, base);
		parent.addChild(dataset);
	}

	private static void addLayer(Element element, ICatalogTreeNode parent, URL context) throws MalformedURLException
	{
		String name = XMLUtil.getText(element, "@name");
		URL info = XMLUtil.getURL(element, "@info", context);
		URL icon = XMLUtil.getURL(element, "@icon", context);
		URL url = XMLUtil.getURL(element, "@url", context);
		boolean base = XMLUtil.getBoolean(element, "@base", false);
		boolean def = XMLUtil.getBoolean(element, "@default", false);
		boolean enabled = XMLUtil.getBoolean(element, "@enabled", true);
		ILayerDefinition layer = new LayerDefinition(name, url, info, icon, base, def, enabled);
		parent.addChild(layer);
	}*/
}
