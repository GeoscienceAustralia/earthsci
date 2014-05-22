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
package au.gov.ga.earthsci.layer.ui;

import gov.nasa.worldwind.layers.Layer;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.dispatch.IDispatchHandler;
import au.gov.ga.earthsci.layer.IPersistentLayer;
import au.gov.ga.earthsci.layer.LegacyLayerHelper;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.tree.LayerNode;
import au.gov.ga.earthsci.layer.worldwind.ITreeModel;

/**
 * An {@link IDispatchHandler} that receives {@link ILayerTreeNode} and/or
 * {@link Layer} objects and inserts them into the current layer model,
 * activating the {@link LayerTreePart}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeNodeDispatchHandler implements IDispatchHandler
{
	private static final String LAYER_PART_ID = "au.gov.ga.earthsci.application.layertree.part"; //$NON-NLS-1$

	@Inject
	private ITreeModel model;

	@Inject
	private EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Override
	public void handle(Object object, Intent intent)
	{
		ILayerTreeNode node = null;
		if (object instanceof ILayerTreeNode)
		{
			node = (ILayerTreeNode) object;
		}
		else if (object instanceof Layer)
		{
			Layer layer = (Layer) object;
			LayerNode layerNode = new LayerNode();
			IPersistentLayer persistentLayer = LegacyLayerHelper.wrap(layer);
			layerNode.setLayer(persistentLayer);
			layerNode.setEnabled(true);
			node = layerNode;
		}

		if (node == null)
		{
			return;
		}

		model.getRootNode().addChild(node);

		final ILayerTreeNode selection = node;
		shell.getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				MPart part = partService.showPart(LAYER_PART_ID, PartState.ACTIVATE);
				ESelectionService selectionService = part.getContext().get(ESelectionService.class);
				selectionService.setSelection(new ILayerTreeNode[] { selection });
			}
		});
	}
}
