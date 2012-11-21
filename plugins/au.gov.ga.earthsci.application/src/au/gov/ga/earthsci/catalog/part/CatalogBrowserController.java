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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;

import au.gov.ga.earthsci.application.util.UserActionPreference;
import au.gov.ga.earthsci.catalog.part.preferences.ICatalogBrowserPreferences;
import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.util.Bag;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;

/**
 * The default implementation of the {@link ICatalogBrowserController} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class CatalogBrowserController implements ICatalogBrowserController
{

	private ITreeModel currentLayerModel;
	
	private Bag<URI> layers;
	private Map<URI, FolderNode> folders;

	private CatalogBrowserPart part;
	
	@Inject
	private ICatalogBrowserPreferences preferences;
	
	@Override
	public void setCatalogBrowserPart(CatalogBrowserPart part)
	{
		this.part = part;
	}
	
	@Inject
	public void setCurrentLayerModel(ITreeModel currentLayerModel)
	{
		this.currentLayerModel = currentLayerModel;
		this.layers = new Bag<URI>();
		this.folders = new HashMap<URI, FolderNode>();
		
		final IListChangeListener recursiveChildListener = new IListChangeListener()
		{
			@Override
			public void handleListChange(ListChangeEvent event)
			{
				boolean redecorateRequired = false;
				for (ListDiffEntry diff : event.diff.getDifferences())
				{
					ILayerTreeNode node = (ILayerTreeNode)diff.getElement();
					if (diff.isAddition())
					{
						redecorateRequired = collectLayerURIs(this, node) || redecorateRequired;
					}
					else
					{
						redecorateRequired = removeLayerURIs(node) || redecorateRequired;
					}
				}
				if (redecorateRequired)
				{
					triggerRedecorate();
				}
			}
		};
		
		collectLayerURIs(recursiveChildListener, currentLayerModel.getRootNode());
	}
	
	@Override
	public boolean existsInLayerModel(URI layerURI)
	{
		return layers.contains(layerURI);
	}
	
	@Override
	public boolean allExistInLayerModel(ITreeNode<ICatalogTreeNode>... nodes)
	{
		if (nodes == null || nodes.length == 0)
		{
			return true;
		}
		boolean allExistInModel = true;
		for (ITreeNode<ICatalogTreeNode> n : nodes)
		{
			if (n != null && n.getValue().isLayerNode())
			{
				allExistInModel = allExistInModel && existsInLayerModel(n.getValue().getLayerURI());
			}
			if (n.getChildCount() > 0)
			{
				allExistInModel = allExistInModel && allExistInLayerModel(n.getChildren());
			}
			if (!allExistInModel)
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean anyExistInLayerModel(ITreeNode<ICatalogTreeNode>... nodes)
	{
		if (nodes == null || nodes.length == 0)
		{
			return false;
		}
		boolean anyExistInModel = false;
		for (ITreeNode<ICatalogTreeNode> n : nodes)
		{
			if (n != null && n.getValue() != null && n.getValue().isLayerNode())
			{
				anyExistInModel = anyExistInModel || existsInLayerModel(n.getValue().getLayerURI());
			}
			if (n.getChildCount() > 0)
			{
				anyExistInModel = anyExistInModel || anyExistInLayerModel(n.getChildren());
			}
			if (anyExistInModel)
			{
				return true;
			}
		}
		return anyExistInModel;
	}
	
	@Override
	public boolean areAllLayerNodes(ITreeNode<ICatalogTreeNode>... nodes)
	{
		for (ITreeNode<ICatalogTreeNode> node : nodes)
		{
			if (node != null && !node.getValue().isLayerNode())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void addToLayerModel(ITreeNode<ICatalogTreeNode>... nodes)
	{
		boolean fullNodePathRequiredOnAdd = isFullNodePathRequiredOnAdd();
		for (ITreeNode<ICatalogTreeNode> node : nodes)
		{
			ILayerTreeNode parent = fullNodePathRequiredOnAdd ? createNodePath(node) : currentLayerModel.getRootNode();
			insertIntoLayerModel(parent, node);
		}

	}

	/**
	 * Initialises (if required) the node path in the current layer model into which the given node
	 * and its children should be inserted. The parent node into which the given catalog node should be inserted
	 * is returned.
	 * <p/>
	 * If the node path does not yet exist, it will be created.
	 * <p/>
	 * If the node path does exist, it will be reused.
	 * 
	 * @param node The catalog being inserted into the layer model
	 * 
	 * @return The node into which the given catalog node (and its children) should be inserted. This may be an existing node, or a new node
	 * as required.
	 */
	private ILayerTreeNode createNodePath(ITreeNode<ICatalogTreeNode> node)
	{
		if (node.isRoot())
		{
			return currentLayerModel.getRootNode();
		}
		
		FolderNode folder = folders.get(node.getValue().getURI());
		if (folder != null)
		{
			return folder;
		}
		
		ILayerTreeNode parent = createNodePath(node.getParent());
		
		if (!node.getValue().isLayerNode())
		{
			folder = createFolderNode(node.getValue());
			parent.add(folder);
			return folder;
		}
		
		return parent;
	}

	/**
	 * Insert the given catalog node (and it's subtree) into the child list of the given parent layer tree node
	 * 
	 * @param parent The parent layer tree node to insert into
	 * @param node The catalog node to  insert
	 */
	private void insertIntoLayerModel(ILayerTreeNode parent, ITreeNode<ICatalogTreeNode> node)
	{
		ICatalogTreeNode catalogTreeNode = node.getValue();
		
		if (catalogTreeNode.isLayerNode())
		{
			LayerNode layer = createLayerNode(catalogTreeNode);
			parent.add(layer);
		}
		else
		{
			FolderNode folder = createFolderNode(catalogTreeNode);
			parent.add(folder);
			for (ITreeNode<ICatalogTreeNode> child : catalogTreeNode.getChildren())
			{
				insertIntoLayerModel(folder, child);
			}
		}
	}

	private LayerNode createLayerNode(ICatalogTreeNode catalogTreeNode)
	{
		LayerNode layer = new LayerNode();
		layer.setUri(catalogTreeNode.getLayerURI());
		layer.setLabel(catalogTreeNode.getLabelOrName());
		layer.setEnabled(true);
		return layer;
	}

	private FolderNode createFolderNode(ICatalogTreeNode catalogTreeNode)
	{
		FolderNode folder = new FolderNode();
		folder.setName(catalogTreeNode.getName());
		folder.setLabel(catalogTreeNode.getLabel());
		folder.setUri(catalogTreeNode.getURI());
		folder.setExpanded(true);
		return folder;
	}

	private boolean isFullNodePathRequiredOnAdd()
	{
		if (preferences.getAddNodeStructureMode() != UserActionPreference.ASK)
		{
			return preferences.getAddNodeStructureMode() == UserActionPreference.ALWAYS;
		}
		
		MessageDialogWithToggle message = MessageDialogWithToggle.openYesNoQuestion(null, Messages.CatalogBrowserController_AddNodePathDialogTitle, 
																						  Messages.CatalogBrowserController_AddNodePathDialogMessage, 
																						  Messages.CatalogBrowserController_AddNodePathDialogDontAsk, 
																						  false, null, null);

		UserActionPreference preference = (message.getReturnCode() == IDialogConstants.YES_ID) ? UserActionPreference.ALWAYS : UserActionPreference.NEVER;
		preferences.setAddNodeStructureMode(message.getToggleState() ? preference : UserActionPreference.ASK);
		
		return preference == UserActionPreference.ALWAYS;
	}
	
	@Override
	public void removeFromLayerModel(ITreeNode<ICatalogTreeNode>... nodes)
	{
		// TODO Auto-generated method stub

	}
	
	/**
	 * Walk the tree from the provided node in a depth-first fashion and collect layer URIs into the provided bag.
	 * <p/>
	 * Additionally, attach the provided list change listener to the child lists of each node so
	 * future changes to the child state can be detected.
	 * 
	 * @return true If any new layer URIs were added to the bag
	 */
	private boolean collectLayerURIs(final IListChangeListener listener, ILayerTreeNode node)
	{
		boolean changesFound = false;
		if (node instanceof LayerNode)
		{
			int newCount = layers.add(node.getUri());
			changesFound = (newCount == 1) || changesFound;
		}
		if (node instanceof FolderNode)
		{
			folders.put(node.getUri(), (FolderNode)node);
		}
		
		IObservableList observer = BeanProperties.list("children").observe(node); //$NON-NLS-1$
		observer.addListChangeListener(listener);
		
		for (ITreeNode<ILayerTreeNode> child : node.getChildren())
		{
			changesFound = collectLayerURIs(listener, (ILayerTreeNode)child) || changesFound;
		}
		return changesFound;
	}
	
	/**
	 * Walk the tree from the provided node in a depth-first fashion and remove layer URIs from the provided bag.
	 * 
	 * @return true If any new layer URIs were added to the bag
	 */
	private boolean removeLayerURIs(ILayerTreeNode node)
	{
		boolean changesFound = false;
		if (node instanceof LayerNode)
		{
			int newCount = layers.remove(node.getUri());
			changesFound = (newCount == 0) || changesFound;
		}
		if (node instanceof FolderNode)
		{
			folders.remove(node.getUri());
		}
		for (ITreeNode<ILayerTreeNode> child : node.getChildren())
		{
			changesFound = removeLayerURIs((ILayerTreeNode)child) || changesFound;
		}
		return changesFound;
	}
	
	private void triggerRedecorate()
	{
		part.getTreeViewer().refresh(true);
	}
}
