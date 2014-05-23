/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.layer.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.ui.dnd.LayerTransferData;

/**
 * {@link DragSourceListener} implementation for the draw order viewer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DrawOrderDragSourceListener implements DragSourceListener
{
	private final TreeViewer viewer;

	public DrawOrderDragSourceListener(TreeViewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public void dragStart(DragSourceEvent event)
	{
		event.doit = false;
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.isEmpty())
		{
			return;
		}
		List<?> selectionList = selection.toList();
		for (Object item : selectionList)
		{
			if (!(item instanceof DrawOrderModel.LayerDrawOrderModelElement))
			{
				return;
			}
		}
		event.doit = true;
	}

	@Override
	public void dragSetData(DragSourceEvent event)
	{
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		List<?> selectionList = selection.toList();
		List<ILayerTreeNode> nodeList = new ArrayList<ILayerTreeNode>();
		for (Object item : selectionList)
		{
			if (item instanceof DrawOrderModel.LayerDrawOrderModelElement)
			{
				nodeList.add(((DrawOrderModel.LayerDrawOrderModelElement) item).node);
			}
		}
		ILayerTreeNode[] nodes = nodeList.toArray(new ILayerTreeNode[nodeList.size()]);
		LayerTransferData data = LayerTransferData.fromNodes(nodes);
		event.data = data;
	}

	@Override
	public void dragFinished(DragSourceEvent event)
	{
	}
}
