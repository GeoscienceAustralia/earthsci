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

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;
import au.gov.ga.earthsci.intent.dispatch.IDispatchHandler;

/**
 * An {@link IDispatchHandler} that receives {@link ILayerTreeNode} and/or
 * {@link Layer} objects and inserts them into the current layer model,
 * activating the {@link LayerTreePart}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
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
	public void handle(Object object)
	{
		ILayerTreeNode node = null;
		if (object instanceof Layer)
		{
			LayerNode layerNode = new LayerNode();
			layerNode.setLayer((Layer) object);
			layerNode.getLayer().setEnabled(true);
			node = layerNode;
		}

		if (object instanceof ILayerTreeNode)
		{
			node = (ILayerTreeNode) object;
		}

		if (node == null)
		{
			return;
		}

		model.getRootNode().addChild(node);

		shell.getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				partService.showPart(LAYER_PART_ID, PartState.ACTIVATE);
			}
		});
	}

}
