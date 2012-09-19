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
package au.gov.ga.earthsci.core.model.layer;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;

/**
 * Helper class that stores a list of the default NASA layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DefaultLayers
{
	public static FolderNode getDefaultLayers()
	{
		FolderNode root = new FolderNode();
		root.setName("NASA");
		LayerList defaultLayers = createLayerList();
		for (Layer layer : defaultLayers)
		{
			LayerNode node = new LayerNode(layer);
			root.add(node);
		}
		return root;
	}

	public static ICatalogTreeNode getDefaultCatalog()
	{
		return null;
	}

	protected static LayerList createLayerList()
	{
		Element element = Configuration.getElement("./LayerList"); //$NON-NLS-1$
		return createLayersFromElement(element);
	}

	protected static LayerList createLayersFromElement(Element element)
	{
		Object o = BasicFactory.create(AVKey.LAYER_FACTORY, element);

		if (o instanceof LayerList)
			return (LayerList) o;

		if (o instanceof Layer)
			return new LayerList(new Layer[] { (Layer) o });

		if (o instanceof LayerList[])
		{
			LayerList[] lists = (LayerList[]) o;
			if (lists.length > 0)
				return LayerList.collapseLists((LayerList[]) o);
		}

		return null;
	}

	protected static class LayerTreeLayerFactory extends BasicLayerFactory
	{
		@Override
		protected Layer createFromLayerDocument(Element domElement, AVList params)
		{
			Layer layer = super.createFromLayerDocument(domElement, params);
			return new LayerNode(layer);
		}
	}
}
