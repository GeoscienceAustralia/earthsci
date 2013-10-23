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

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.intent.dispatch.IDispatchHandler;
import au.gov.ga.earthsci.layer.ILayerTreeNode;
import au.gov.ga.earthsci.layer.LayerNode;
import au.gov.ga.earthsci.layer.worldwind.ITreeModel;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

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

	private static final Logger logger = LoggerFactory.getLogger(LayerTreeNodeDispatchHandler.class);

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
		if (object instanceof ILayerTreeNode)
		{
			node = (ILayerTreeNode) object;
		}
		else if (object instanceof Layer)
		{
			Layer layer = (Layer) object;
			LayerNode layerNode = new LayerNode();
			layerNode.setLayer(layer);
			layerNode.getLayer().setEnabled(true);
			URL url = (URL) layer.getValue(AVKeyMore.CONTEXT_URL);
			if (url == null)
			{
				AVList params = (AVList) layer.getValue(AVKeyMore.CONSTRUCTION_PARAMETERS);
				if (params != null)
				{
					url = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
				}
			}
			if (url != null)
			{
				try
				{
					layerNode.setURI(url.toURI());
				}
				catch (URISyntaxException e)
				{
					logger.error("Error converting layer URL to URI", e); //$NON-NLS-1$
				}
			}
			else
			{
				logger.warn("Dispatched Layer object doesn't contain a URL for persistence"); //$NON-NLS-1$
			}
			node = layerNode;
		}

		if (node == null)
		{
			return;
		}

		ILayerTreeNode existing = findMatchingExistingNode(node, model.getRootNode());
		if (existing != null)
		{
			node = existing;
		}
		else
		{
			model.getRootNode().addChild(node);
		}

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

	private ILayerTreeNode findMatchingExistingNode(ILayerTreeNode nodeToFind, ILayerTreeNode nodeToSearch)
	{
		if (nodesMatch(nodeToFind, nodeToSearch))
		{
			return nodeToSearch;
		}
		for (ILayerTreeNode child : nodeToSearch.getChildren())
		{
			ILayerTreeNode matching = findMatchingExistingNode(nodeToFind, child);
			if (matching != null)
			{
				return matching;
			}
		}
		return null;
	}

	private boolean nodesMatch(ILayerTreeNode node1, ILayerTreeNode node2)
	{
		if (node1 == null || node2 == null)
		{
			return node1 == node2;
		}
		if (!urisMatch(node1.getURI(), node2.getURI()))
		{
			return false;
		}
		if (node1.getChildCount() != node2.getChildCount())
		{
			return false;
		}
		for (int i = 0; i < node1.getChildCount(); i++)
		{
			if (!nodesMatch(node1.getChild(i), node2.getChild(i)))
			{
				return false;
			}
		}
		return true;
	}

	private boolean urisMatch(URI uri1, URI uri2)
	{
		if (uri1 == null || uri2 == null)
		{
			return uri1 == uri2;
		}
		return uri1.equals(uri2);
	}
}
