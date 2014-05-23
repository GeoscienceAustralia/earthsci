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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.TreeItem;

import au.gov.ga.earthsci.layer.tree.ILayerNode;
import au.gov.ga.earthsci.layer.ui.dnd.LayerTransferData;
import au.gov.ga.earthsci.layer.ui.dnd.LayerTransferData.TransferredLayer;
import au.gov.ga.earthsci.layer.ui.dnd.LocalLayerTransfer;

/**
 * {@link DropTargetListener} for the draw order viewer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DrawOrderDropAdapter extends ViewerDropAdapter
{
	public DrawOrderDropAdapter(Viewer viewer)
	{
		super(viewer);
	}

	@Override
	public boolean performDrop(Object data)
	{
		if (data instanceof LayerTransferData)
		{
			LayerTransferData layerTransferData = (LayerTransferData) data;
			TransferredLayer[] layers = layerTransferData.getLayers();

			Integer newDrawOrderValue = null;
			Object target = getCurrentTarget();
			if (target instanceof DrawOrderModel.DrawOrderDrawOrderModelElement)
			{
				newDrawOrderValue = ((DrawOrderModel.DrawOrderDrawOrderModelElement) target).drawOrder;
			}
			else if (target instanceof DrawOrderModel.LayerDrawOrderModelElement)
			{
				newDrawOrderValue = ((DrawOrderModel.LayerDrawOrderModelElement) target).getParent().drawOrder;
			}

			if (newDrawOrderValue != null)
			{
				for (TransferredLayer layer : layers)
				{
					if (layer.getNode() instanceof ILayerNode)
					{
						ILayerNode layerNode = (ILayerNode) layer.getNode();
						if (layerNode.getDrawOrder() != newDrawOrderValue)
						{
							layerNode.setDrawOrder(newDrawOrderValue);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType)
	{
		return LocalLayerTransfer.getInstance().isSupportedType(transferType)
				&& (target instanceof DrawOrderModel.DrawOrderDrawOrderModelElement || target instanceof DrawOrderModel.LayerDrawOrderModelElement);
	}

	@Override
	protected Object determineTarget(DropTargetEvent event)
	{
		//modify the event target item so that it is always a draw order element, and not a layer
		if (event.item instanceof TreeItem && event.item.getData() instanceof DrawOrderModel.LayerDrawOrderModelElement)
		{
			TreeItem treeItem = (TreeItem) event.item;
			TreeItem parentItem = treeItem.getParentItem();
			if (parentItem != null)
			{
				event.item = parentItem;
				event.x += parentItem.getBounds().x - treeItem.getBounds().x;
				event.y += parentItem.getBounds().y - treeItem.getBounds().y;
			}
		}
		return super.determineTarget(event);
	}

	@Override
	protected int determineLocation(DropTargetEvent event)
	{
		int location = super.determineLocation(event);
		if (location == LOCATION_NONE)
		{
			return location;
		}
		return LOCATION_ON;
	}
}
