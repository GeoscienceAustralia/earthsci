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
package au.gov.ga.earthsci.application.parts.layer.handlers;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;

import au.gov.ga.earthsci.application.parts.layer.LayerTransfer;
import au.gov.ga.earthsci.application.parts.layer.LayerTransferData;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;

/**
 * Handles copy commands for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CopyHandler
{
	@Execute
	public void execute(TreeViewer viewer, Clipboard clipboard)
	{
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		List<?> selectionList = selection.toList();
		ILayerTreeNode[] nodes = selectionList.toArray(new ILayerTreeNode[selectionList.size()]);
		LayerTransferData data = LayerTransferData.fromNodes(nodes);
		clipboard.setContents(new Object[] { data }, new Transfer[] { LayerTransfer.getInstance() });
	}
}
