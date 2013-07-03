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
package au.gov.ga.earthsci.layer.ui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

import au.gov.ga.earthsci.application.Activator;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.layer.ui.dnd.LayerTransfer;
import au.gov.ga.earthsci.layer.ui.dnd.LayerTransferData;

/**
 * {@link DragSourceListener} implementation for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeDragSourceListener implements DragSourceListener
{
	private final TreeViewer viewer;

	public LayerTreeDragSourceListener(TreeViewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public void dragFinished(DragSourceEvent event)
	{
		if (!event.doit)
		{
			return;
		}
		//if the gadget was moved, remove it from the source viewer
		if (event.detail == DND.DROP_MOVE)
		{
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			for (Iterator<?> it = selection.iterator(); it.hasNext();)
			{
				ILayerTreeNode node = (ILayerTreeNode) it.next();
				node.removeFromParent();
			}
			viewer.refresh();
		}
	}

	@Override
	public void dragSetData(DragSourceEvent event)
	{
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		List<?> selectionList = selection.toList();
		ILayerTreeNode[] nodes = selectionList.toArray(new ILayerTreeNode[selectionList.size()]);
		LayerTransferData data = LayerTransferData.fromNodes(nodes);
		if (LayerTransfer.getInstance().isSupportedType(event.dataType))
		{
			event.data = data;
		}
		else if (PluginTransfer.getInstance().isSupportedType(event.dataType))
		{
			byte[] bytes = LayerTransfer.getInstance().toByteArray(data);
			event.data = new PluginTransferData(Activator.getBundleName(), bytes);
		}
	}

	@Override
	public void dragStart(DragSourceEvent event)
	{
		event.doit = !viewer.getSelection().isEmpty();
	}
}
