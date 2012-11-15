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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import au.gov.ga.earthsci.application.parts.info.InfoPart;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;

/**
 * Handles layer node information button selection.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InformationHandler
{
	@Inject
	private EPartService partService;

	@Execute
	public void execute(TreeViewer viewer)
	{
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		List<?> selectionList = selection.toList();
		if (!selectionList.isEmpty())
		{
			ILayerTreeNode[] nodes = selectionList.toArray(new ILayerTreeNode[selectionList.size()]);
			ILayerTreeNode node = nodes[0];

			MPart part = partService.showPart(InfoPart.PART_ID, PartState.VISIBLE);
			part.getContext().modify(InfoPart.INPUT_NAME, node);
			part.getContext().declareModifiable(InfoPart.INPUT_NAME);
		}
	}

	@CanExecute
	public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layer)
	{
		return layer != null;
	}
}
