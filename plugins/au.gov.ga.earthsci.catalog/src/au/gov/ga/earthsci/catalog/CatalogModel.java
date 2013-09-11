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
package au.gov.ga.earthsci.catalog;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;

/**
 * Default implementation of the {@link ICatalogModel} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class CatalogModel implements ICatalogModel
{
	private final RootNode root = new RootNode();

	@Inject
	private IEclipseContext context;

	@PostConstruct
	public void load()
	{
		CatalogPersister.loadFromWorkspace(this, context);
	}

	@PreDestroy
	public void save()
	{
		CatalogPersister.saveToWorkspace(this);
	}

	@Override
	public ICatalogTreeNode getRoot()
	{
		return root;
	}

	@Override
	public List<ICatalogTreeNode> getTopLevelCatalogs()
	{
		return root.getChildren();
	}

	public void setTopLevelCatalogs(ICatalogTreeNode[] nodes)
	{
		for (ICatalogTreeNode node : nodes)
		{
			root.addChild(node);
		}
	}

	@Override
	public void addTopLevelCatalog(final ICatalogTreeNode catalog)
	{
		if (catalog == null)
		{
			return;
		}

		root.addChild(catalog);
	}

	@Override
	public void addTopLevelCatalog(int index, ICatalogTreeNode catalog)
	{
		if (catalog == null)
		{
			return;
		}

		root.addChild(index, catalog);
	}

	@Override
	public boolean containsTopLevelCatalogURI(URI uri)
	{
		return root.containsChildURI(uri);
	}

	@Override
	public ICatalogTreeNode getTopLevelCatalogForURI(URI uri)
	{
		return root.getChildForURI(uri);
	}

	@Override
	public void removeTopLevelCatalogsForURI(URI uri)
	{
		List<ICatalogTreeNode> toRemove = new ArrayList<ICatalogTreeNode>();
		for (ICatalogTreeNode child : getTopLevelCatalogs())
		{
			URI childUri = child.getURI();
			if (uri == null)
			{
				if (childUri == null)
				{
					toRemove.add(child);
				}
			}
			else if (uri.equals(childUri))
			{
				toRemove.add(child);
			}
		}
		for (ICatalogTreeNode remove : toRemove)
		{
			root.removeChild(remove);
		}
	}

	private static class RootNode extends AbstractCatalogTreeNode
	{
		private final Map<URI, ICatalogTreeNode> uriMap = new HashMap<URI, ICatalogTreeNode>();

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
		public String getName()
		{
			return "ROOT"; //$NON-NLS-1$
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

		@Override
		public IContentType getLayerContentType()
		{
			return null;
		}

		@Override
		public URL getInformationURL()
		{
			return null;
		}

		@Override
		public String getInformationString()
		{
			return null;
		}

		@Override
		protected void fireChildrenPropertyChange(List<ICatalogTreeNode> oldChildren, List<ICatalogTreeNode> newChildren)
		{
			super.fireChildrenPropertyChange(oldChildren, newChildren);
			synchronized (uriMap)
			{
				uriMap.clear();
				for (ICatalogTreeNode child : newChildren)
				{
					uriMap.put(child.getURI(), child);
				}
			}
		}

		public boolean containsChildURI(URI uri)
		{
			return uriMap.containsKey(uri);
		}

		public ICatalogTreeNode getChildForURI(URI uri)
		{
			return uriMap.get(uri);
		}
	}
}
