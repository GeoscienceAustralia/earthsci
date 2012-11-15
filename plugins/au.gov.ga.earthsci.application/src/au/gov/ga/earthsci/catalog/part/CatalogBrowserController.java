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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.MultiListProperty;
import org.eclipse.e4.core.di.annotations.Creatable;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;
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
	
	@Override
	public boolean existsInLayerModel(URI layerURI)
	{
		return findLayerURI(currentLayerModel.getRootNode(), layerURI);
	}

	// TODO: This is grossly naive and inefficient
	private boolean findLayerURI(ILayerTreeNode node, URI uri)
	{
		if (node instanceof LayerNode)
		{
			return ((LayerNode)node).getLayerURI().equals(uri);
		}
		
		if (node instanceof FolderNode)
		{
			for (ITreeNode<ILayerTreeNode> child : ((FolderNode)node).getChildren())
			{
				if (findLayerURI(child.getValue(), uri))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void addToLayerModel(ICatalogTreeNode[] nodes)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeFromLayerModel(ICatalogTreeNode[] nodes)
	{
		// TODO Auto-generated method stub

	}
	
	@Inject
	public void setCurrentLayerModel(ITreeModel currentLayerModel)
	{
		this.currentLayerModel = currentLayerModel;
		
		System.out.println(Arrays.asList(currentLayerModel.getRootNode().getChildren()[0].getChildren()));
		
		currentLayerModel.getRootNode().addPropertyChangeListener("children", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				System.out.println("IM CHANGED!");
			}
		});
		
		IListProperty childrenProperty = new MultiListProperty(new IListProperty[] { BeanProperties.list("children") }); //$NON-NLS-1$
		final IObservableList observe = childrenProperty.observe(currentLayerModel.getRootNode());
		
		observe.addListChangeListener(new IListChangeListener()
		{
			@Override
			public void handleListChange(ListChangeEvent event)
			{
				System.out.println("I changed!");
			}
		});
		
	}

}
