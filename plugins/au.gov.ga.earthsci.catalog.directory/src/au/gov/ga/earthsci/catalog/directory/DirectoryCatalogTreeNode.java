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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.catalog.AbstractCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ErrorCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.LoadingCatalogTreeNode;
import au.gov.ga.earthsci.core.tree.ILazyTreeNodeCallback;
import au.gov.ga.earthsci.core.tree.lazy.AsynchronousLazyTreeNodeHelper;
import au.gov.ga.earthsci.core.tree.lazy.IAsynchronousLazyTreeNode;
import au.gov.ga.earthsci.core.url.SystemIconURLStreamHandlerService;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
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
	private final IEclipseContext context;
	private final boolean root;
	private final AsynchronousLazyTreeNodeHelper<ICatalogTreeNode> helper =
			new AsynchronousLazyTreeNodeHelper<ICatalogTreeNode>(this);
	private boolean iconURLLoaded = false;
	private URL iconURL;

	public DirectoryCatalogTreeNode(URI directoryURI, IEclipseContext context)
	{
		this(directoryURI, false, context);
	}

	public DirectoryCatalogTreeNode(URI directoryURI, boolean root, IEclipseContext context)
	{
		super(directoryURI);
		this.root = root;
		this.context = context;
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
		if (!iconURLLoaded)
		{
			try
			{
				iconURL = SystemIconURLStreamHandlerService.createURL(getURI());
			}
			catch (MalformedURLException e)
			{
			}
			iconURLLoaded = true;
		}
		return iconURL;
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
					addChild(new DirectoryCatalogTreeNode(directory.toURI(), context));
				}
			}
			for (final File file : files)
			{
				if (file.isFile())
				{
					Intent intent = new Intent();
					intent.setExpectedReturnType(Layer.class);
					intent.setURI(file.toURI());
					IntentManager.getInstance().start(intent, null, false, new AbstractIntentCallback()
					{
						@Override
						public boolean filters(List<IntentFilter> filters, Intent intent)
						{
							if (!filters.isEmpty())
							{
								//some intent filter knows how to open this, so add it as a child
								addChild(new FileCatalogTreeNode(file.toURI()));
							}

							//don't actually run the intent, just want to know if there's any filters
							return false;
						}

						@Override
						public void error(Exception e, Intent intent)
						{
						}

						@Override
						public void completed(Object result, Intent intent)
						{
						}
					}, context);
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
