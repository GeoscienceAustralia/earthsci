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
package au.gov.ga.earthsci.catalog.part;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import au.gov.ga.earthsci.application.parts.layer.LayerTransfer;
import au.gov.ga.earthsci.application.parts.layer.LayerTransferData;
import au.gov.ga.earthsci.catalog.model.ICatalogTreeNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;

/**
 * {@link DragSourceListener} implementation for the catalog browser tree
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogTreeDragSourceListener implements DragSourceListener
{

	private final TreeViewer viewer;
	private final ICatalogBrowserController controller;

	public CatalogTreeDragSourceListener(TreeViewer viewer, ICatalogBrowserController controller)
	{
		this.viewer = viewer;
		this.controller = controller;
	}

	@Override
	public void dragStart(DragSourceEvent event)
	{
		if (viewer.getSelection().isEmpty())
		{
			event.doit = false;
		}
		else
		{
			event.doit = controller.areAllLayerNodes(getSelectedCatalogNodes());
		}
	}

	@Override
	public void dragSetData(DragSourceEvent event)
	{
		if (LayerTransfer.getInstance().isSupportedType(event.dataType))
		{
			doLayerTransfer(event);
			return;
		}
	}

	private void doLayerTransfer(DragSourceEvent event)
	{
		ICatalogTreeNode[] selectedCatalogNodes = getSelectedCatalogNodes();
		List<ILayerTreeNode> layerTreeNodes = new ArrayList<ILayerTreeNode>();
		for (ICatalogTreeNode node : selectedCatalogNodes)
		{
			if (node == null || !node.isLayerNode())
			{
				continue;
			}
			
			ILayerTreeNode layerTreeNode = controller.createLayerTreeNode(node);
			layerTreeNodes.add(layerTreeNode);
		}
		
		LayerTransferData transferData = LayerTransferData.fromNodes(layerTreeNodes.toArray(new ILayerTreeNode[layerTreeNodes.size()]));
		event.data = transferData;
	}

	private ICatalogTreeNode[] getSelectedCatalogNodes()
	{
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		List<?> selectionList = selection.toList();
		ICatalogTreeNode[] nodes = selectionList.toArray(new ICatalogTreeNode[selectionList.size()]);
		return nodes;
	}

	@Override
	public void dragFinished(DragSourceEvent event)
	{
		// Do nothing
	}

}
