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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.e4.core.di.annotations.Creatable;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
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

	private CatalogBrowserPart part;
	
	@Override
	public void setCatalogBrowserPart(CatalogBrowserPart part)
	{
		this.part = part;
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
			// Shortcut exit from the loop
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
		// TODO Auto-generated method stub

	}

	@Override
	public void removeFromLayerModel(ITreeNode<ICatalogTreeNode>... nodes)
	{
		// TODO Auto-generated method stub

	}
	
	@Inject
	public void setCurrentLayerModel(ITreeModel currentLayerModel)
	{
		this.currentLayerModel = currentLayerModel;
		this.layers = new Bag<URI>();
		
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
						redecorateRequired = collectLayerURIs(layers, this, node) || redecorateRequired;
					}
					else
					{
						redecorateRequired = removeLayerURIs(layers, node) || redecorateRequired;
					}
				}
				if (redecorateRequired)
				{
					triggerRedecorate();
				}
			}
		};
		
		collectLayerURIs(layers, recursiveChildListener, currentLayerModel.getRootNode());
	}

	/**
	 * Walk the tree from the provided node in a depth-first fashion and collect layer URIs into the provided bag.
	 * <p/>
	 * Additionally, attach the provided list change listener to the child lists of each node so
	 * future changes to the child state can be detected.
	 * 
	 * @return true If any new layer URIs were added to the bag
	 */
	private boolean collectLayerURIs(final Bag<URI> layers, final IListChangeListener listener, ILayerTreeNode node)
	{
		boolean changesFound = false;
		if (node instanceof LayerNode)
		{
			int newCount = layers.add(((LayerNode)node).getLayerURI());
			changesFound = (newCount == 1) || changesFound;
		}
		
		IObservableList observer = BeanProperties.list("children").observe(node); //$NON-NLS-1$
		observer.addListChangeListener(listener);
		
		for (ITreeNode<ILayerTreeNode> child : node.getChildren())
		{
			changesFound = collectLayerURIs(layers, listener, (ILayerTreeNode)child) || changesFound;
		}
		return changesFound;
	}
	
	/**
	 * Walk the tree from the provided node in a depth-first fashion and remove layer URIs from the provided bag.
	 * 
	 * @return true If any new layer URIs were added to the bag
	 */
	private boolean removeLayerURIs(final Bag<URI> layers, ILayerTreeNode node)
	{
		boolean changesFound = false;
		if (node instanceof LayerNode)
		{
			int newCount = layers.remove(((LayerNode)node).getLayerURI());
			changesFound = (newCount == 0) || changesFound;
		}
		
		for (ITreeNode<ILayerTreeNode> child : node.getChildren())
		{
			changesFound = removeLayerURIs(layers, (ILayerTreeNode)child) || changesFound;
		}
		return changesFound;
	}
	
	private void triggerRedecorate()
	{
		part.getTreeViewer().refresh(true);
	}
}
