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
package au.gov.ga.earthsci.catalog.directory;

import gov.nasa.worldwind.layers.Layer;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;

import au.gov.ga.earthsci.catalog.AbstractCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ErrorCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.LoadingCatalogTreeNode;
import au.gov.ga.earthsci.core.tree.ILazyTreeNodeCallback;
import au.gov.ga.earthsci.core.tree.lazy.AsynchronousLazyTreeNodeHelper;
import au.gov.ga.earthsci.core.tree.lazy.IAsynchronousLazyTreeNode;
import au.gov.ga.earthsci.core.url.SystemIconURLStreamHandlerService;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentFilter;
import au.gov.ga.earthsci.intent.IntentManager;

/**
 * Catalog tree node that represents a directory in a local file system.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DirectoryCatalogTreeNode extends AbstractCatalogTreeNode implements
		IAsynchronousLazyTreeNode<ICatalogTreeNode>
{
	private final boolean root;
	private final AsynchronousLazyTreeNodeHelper<ICatalogTreeNode> helper =
			new AsynchronousLazyTreeNodeHelper<ICatalogTreeNode>(this);

	public DirectoryCatalogTreeNode(URI directoryURI)
	{
		this(directoryURI, false);
	}

	public DirectoryCatalogTreeNode(URI directoryURI, boolean root)
	{
		super(directoryURI);
		this.root = root;
	}

	@Override
	public boolean isRemoveable()
	{
		return root;
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
	public String getName()
	{
		File file = new File(getURI());
		if (root)
		{
			return file.getAbsolutePath();
		}
		return file.getName();
	}

	@Override
	public URL getInformationURL()
	{
		return null;
	}

	@Override
	public String getInformationString()
	{
		// TODO
		return null;
	}

	@Override
	public URL getIconURL()
	{
		return SystemIconURLStreamHandlerService.createURL(new File(getURI()));
	}

	@Override
	public void load(ILazyTreeNodeCallback callback)
	{
		helper.load(getName(), callback);
	}

	@Override
	public boolean isLoaded()
	{
		return helper.isLoaded();
	}

	@Override
	public List<ICatalogTreeNode> getDisplayChildren()
	{
		return helper.getDisplayChildren();
	}

	@Override
	public IStatus doLoad(IProgressMonitor monitor)
	{
		File parent = new File(getURI());
		if (parent.exists() && parent.isDirectory())
		{
			File[] files = parent.listFiles();
			for (File directory : files)
			{
				if (directory.isDirectory())
				{
					addChild(new DirectoryCatalogTreeNode(directory.toURI()));
				}
			}
			for (File file : files)
			{
				if (file.isFile())
				{
					Intent intent = new Intent();
					IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(file.getName());
					intent.setContentType(contentType);
					intent.setExpectedReturnType(Layer.class);
					intent.setURI(file.toURI());
					IntentFilter filter = IntentManager.getInstance().findFilter(intent);
					if (filter != null)
					{
						//some intent filter knows how to open this, so add it as a child
						addChild(new FileCatalogTreeNode(file.toURI()));
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public ICatalogTreeNode getLoadingNode()
	{
		return new LoadingCatalogTreeNode();
	}

	@Override
	public ICatalogTreeNode getErrorNode(Throwable error)
	{
		return new ErrorCatalogTreeNode(error);
	}
}
