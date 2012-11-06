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
package au.gov.ga.earthsci.application.parts.layer;

import java.io.File;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;

import au.gov.ga.earthsci.application.parts.layer.LayerTransferData.TransferredLayer;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;

/**
 * {@link DropTargetListener} implementation for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeDropAdapter extends ViewerDropAdapter
{
	private final ITreeModel model;

	public LayerTreeDropAdapter(TreeViewer viewer, ITreeModel model)
	{
		super(viewer);
		this.model = model;
	}

	@Override
	public boolean performDrop(Object d)
	{
		if (d == null)
			return false;

		int index;
		ILayerTreeNode target = (ILayerTreeNode) getCurrentTarget();
		if (target == null)
		{
			target = model.getRootNode();
			index = target.getChildCount();
		}
		else
		{
			int location = getCurrentLocation();
			if (location == LOCATION_ON || location == LOCATION_NONE)
			{
				index = target.getChildCount();
			}
			else
			{
				index = location == LOCATION_BEFORE ? target.index() : target.index() + 1;
				target = target.getParent().getValue();
			}
		}

		if (LayerTransfer.getInstance().isSupportedType(getCurrentEvent().currentDataType))
		{
			LayerTransferData data = (LayerTransferData) d;

			//cannot drop a gadget onto itself or a child
			TransferredLayer[] toDrop = data.getLayers();
			if (getCurrentOperation() == DND.DROP_MOVE)
			{
				for (TransferredLayer drop : toDrop)
				{
					if (!validDropTarget(target, drop))
						return false;
				}
			}
			for (int i = toDrop.length - 1; i >= 0; i--)
			{
				TransferredLayer layer = toDrop[i];
				ILayerTreeNode node = layer.getNode();
				target.add(index, node);
				getViewer().add(target, node);
				getViewer().reveal(node);
			}
			return true;
		}
		else if (FileTransfer.getInstance().isSupportedType(getCurrentEvent().currentDataType))
		{
			String[] filenames = (String[]) d;
			for (String filename : filenames)
			{
				File file = new File(filename);
				if (file.isFile())
				{
					LayerNode node = new LayerNode();
					node.setName(file.getName());
					node.setEnabled(true);
					node.setLayerURI(file.toURI());
					target.add(index, node);
					getViewer().add(target, node);
					getViewer().reveal(node);
				}
			}
		}
		return false;
	}

	protected boolean validDropTarget(ILayerTreeNode target, TransferredLayer drop)
	{
		int[] dropPath = drop.getTreePath();
		int[] targetPath = target.indicesToRoot();
		return dropPath.length > 0
				&& (targetPath.length < dropPath.length || targetPath[dropPath.length - 1] != dropPath[dropPath.length - 1]);
	}

	@Override
	protected TreeViewer getViewer()
	{
		return (TreeViewer) super.getViewer();
	}

	@Override
	public boolean validateDrop(Object target, int op, TransferData type)
	{
		return LayerTransfer.getInstance().isSupportedType(type) || FileTransfer.getInstance().isSupportedType(type);
	}
}
