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
package au.gov.ga.earthsci.core.model.catalog;

import java.net.URI;
import java.util.Arrays;

import au.gov.ga.earthsci.core.tree.ITreeNode;


/**
 * Default implementation of the {@link ICatalogModel} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogModel implements ICatalogModel
{

	private final ICatalogTreeNode root = new RootNode();
	
	@Override
	public ICatalogTreeNode getRoot()
	{
		return root;
	}

	@Override
	public ICatalogTreeNode[] getTopLevelCatalogs()
	{
		ITreeNode<ICatalogTreeNode>[] children = root.getChildren();
		return Arrays.asList(children).toArray(new ICatalogTreeNode[children.length]);
	}

	public void setTopLevelCatalogs(ICatalogTreeNode[] nodes)
	{
		for (ICatalogTreeNode node : nodes)
		{
			root.add(node);
		}
	}
	
	@Override
	public void addTopLevelCatalog(final ICatalogTreeNode catalog)
	{
		if (catalog == null)
		{
			return;
		}
		
		root.add(catalog);
	}
	
	@Override
	public void addTopLevelCatalog(int index, ICatalogTreeNode catalog)
	{
		if (catalog == null)
		{
			return;
		}
		
		root.add(index, catalog);
	}
	
	private static class RootNode extends AbstractCatalogTreeNode
	{
		public RootNode()
		{
			super(null);
		}

		@Override
		public boolean isRemoveable()
		{
			return false;
		}

		@Override
		public boolean isReloadable()
		{
			return true;
		}

		@Override
		public String getName()
		{
			return "ROOT"; //$NON-NLS-1$
		}
		
		@Override
		public boolean isLoaded()
		{
			return true;
		}
		
		@Override
		public boolean isLayerNode()
		{
			return false;
		}
		
		@Override
		public URI getLayerURI() 
		{
			return null;
		}
	}
	
}
