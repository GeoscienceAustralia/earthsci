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
package au.gov.ga.earthsci.catalog;

import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.core.model.ModelStatus;
import au.gov.ga.earthsci.layer.tree.FolderNode;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.tree.LayerNode;

/**
 * Helper class used for creating layers from catalog tree nodes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CatalogLayerHelper
{
	/**
	 * Insert the given catalog node (and it's subtree) into the child list of
	 * the given parent layer tree node
	 * 
	 * @param parent
	 *            The parent layer tree node to insert into
	 * @param node
	 *            The catalog node to insert
	 * @param context
	 *            Eclipse context
	 */
	public static void insertIntoLayerModel(ILayerTreeNode parent, ICatalogTreeNode node, IEclipseContext context)
	{
		if (node.isLayerNode())
		{
			LayerNode layer = createLayerNode(node);
			parent.addChild(layer);
			try
			{
				node.loadLayer(layer, context);
			}
			catch (Exception e)
			{
				layer.setStatus(ModelStatus.error(Messages.CatalogLayerHelper_LoadLayerError, e));
			}
		}
		else
		{
			FolderNode folder = createFolderNode(node);
			parent.addChild(folder);
			for (ICatalogTreeNode child : node.getChildren())
			{
				insertIntoLayerModel(folder, child, context);
			}
		}
	}

	/**
	 * Create and return an {@link ILayerTreeNode} that is the equivalent of the
	 * given {@link ICatalogTreeNode}.
	 * 
	 * @param catalogTreeNode
	 *            The catalog tree node to transform
	 * 
	 * @return A new {@link ILayerTreeNode} that is the equivalent of the given
	 *         {@link ICatalogTreeNode}
	 */
	public static ILayerTreeNode createLayerTreeNode(ICatalogTreeNode catalogTreeNode)
	{
		if (catalogTreeNode.isLayerNode())
		{
			return createLayerNode(catalogTreeNode);
		}
		return createFolderNode(catalogTreeNode);
	}

	public static LayerNode createLayerNode(ICatalogTreeNode catalogTreeNode)
	{
		LayerNode layer = new LayerNode();
		layer.setCatalogURI(catalogTreeNode.getURI());
		layer.setName(catalogTreeNode.getName());
		layer.setLabel(catalogTreeNode.getLabel());
		layer.setEnabled(true);
		layer.setIconURL(catalogTreeNode.getIconURL());
		layer.setNodeInformationURL(catalogTreeNode.getInformationURL());
		return layer;
	}

	public static FolderNode createFolderNode(ICatalogTreeNode catalogTreeNode)
	{
		FolderNode folder = new FolderNode();
		folder.setCatalogURI(catalogTreeNode.getURI());
		folder.setName(catalogTreeNode.getName());
		folder.setLabel(catalogTreeNode.getLabel());
		folder.setExpanded(true);
		folder.setIconURL(catalogTreeNode.getIconURL());
		folder.setNodeInformationURL(catalogTreeNode.getInformationURL());
		return folder;
	}
}
