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

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.util.ILabeled;
import au.gov.ga.earthsci.viewers.IControlProvider;

/**
 * A {@link IControlProvider} for the catalog browser tree
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
public class CatalogTreeControlProvider extends LabelProvider implements IControlProvider
{
	private static final Logger logger = LoggerFactory.getLogger(CatalogTreeControlProvider.class);
	
	private Color backgroundColor;
	
	public CatalogTreeControlProvider()
	{
		
	}
	
	@Override
	public Image getImage(Object element)
	{
		if (!(element instanceof ICatalogTreeNode))
		{
			return null;
		}
		Image icon = getProvider((ICatalogTreeNode)element).getIcon((ICatalogTreeNode)element);
		if (icon == null)
		{
			icon = DEFAULT_PROVIDER.getIcon((ICatalogTreeNode)element);
		}
		return icon;
	}
	
	
	@Override
	public String getText(Object element)
	{
		if (!(element instanceof ICatalogTreeNode))
		{
			if (element instanceof ILabeled)
			{
				return ((ILabeled)element).getLabelOrName();
			}
		}
		return getProvider((ICatalogTreeNode)element).getLabel((ICatalogTreeNode)element);
	}
	
	@Override
	public Control getControl(Composite parent, Object element, Item item, ControlEditor editor)
	{
		if (!(element instanceof ICatalogTreeNode))
		{
			return null;
		}
		
		ICatalogTreeNode node = (ICatalogTreeNode)element;
		ICatalogTreeNodeControlProvider provider = getProvider(node);
		if (provider == null)
		{
			return null;
		}
		
		return null;
	}

	@Override
	public boolean updateControl(Control control, Object element, Item item, ControlEditor editor)
	{
		return true;
	}

	@Override
	public Rectangle overrideBounds(Rectangle bounds, Control control, Object element, Item item)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void disposeControl(Control control, Object element, Item item)
	{
		// TODO Auto-generated method stub
		
	}

	public void setBackgroundColor(Color backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}
	
	@Override
	public void dispose()
	{
		controlProvidersLock.readLock().lock();
		try
		{
			for (ICatalogTreeNodeControlProvider provider : controlProviders)
			{
				provider.dispose();
			}
		}
		finally
		{
			controlProvidersLock.readLock().unlock();
		}
	}
	
	private static final Set<ICatalogTreeNodeControlProvider> controlProviders = new LinkedHashSet<ICatalogTreeNodeControlProvider>();
	private static final ReadWriteLock controlProvidersLock = new ReentrantReadWriteLock();
	
	public static final String CATALOG_NODE_CONTROL_PROVIDER_EXTENSION_POINT_ID = "au.gov.ga.earthsci.application.catalogNodeControlProvider"; //$NON-NLS-1$
	public static final String CONTROL_PROVIDER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	
	private static final ICatalogTreeNodeControlProvider DEFAULT_PROVIDER = new ICatalogTreeNodeControlProvider()
	{
		
		@Override
		public boolean supports(ICatalogTreeNode node)
		{
			return true;
		}
		
		@Override
		public String getLabel(ICatalogTreeNode node)
		{
			return node.getLabelOrName();
		}
		
		@Override
		public URL getInfoURL(ICatalogTreeNode node)
		{
			return null;
		}
		
		@Override
		public Image getIcon(ICatalogTreeNode node)
		{
			if (node.getParent() == null || node.getParent().getParent() == null)
			{
				return ImageRegistry.getInstance().get(ImageRegistry.ICON_REPOSITORY);
			}
			if (node.hasChildren())
			{
				return ImageRegistry.getInstance().get(ImageRegistry.ICON_FOLDER);
			}
			return ImageRegistry.getInstance().get(ImageRegistry.ICON_LAYER_NODE);
		}
		
		@Override
		public void dispose() {};
	};
	
	/**
	 * Load the registered providers from the extension registry
	 * 
	 * @param registry The registry to load from
	 */
	public static void loadProviders(final IExtensionRegistry registry)
	{
		logger.debug("Loading catalog node control providers from extension registry"); //$NON-NLS-1$
		
		IConfigurationElement[] config = registry.getConfigurationElementsFor(CATALOG_NODE_CONTROL_PROVIDER_EXTENSION_POINT_ID);
		try
		{
			for (IConfigurationElement e : config)
			{
				final Object o = e.createExecutableExtension(CONTROL_PROVIDER_CLASS_ATTRIBUTE); 
				if (o instanceof ICatalogTreeNodeControlProvider)
				{
					registerProvider((ICatalogTreeNodeControlProvider)o);
				}
			}
		}
		catch (CoreException e)
		{
			logger.error("Exception while loading providers", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Register the control provider with this class
	 * 
	 * @param provider The provider to register
	 */
	public static void registerProvider(final ICatalogTreeNodeControlProvider provider)
	{
		if (provider == null)
		{
			return;
		}
		
		controlProvidersLock.writeLock().lock();
		try
		{
			controlProviders.add(provider);
		}
		finally
		{
			controlProvidersLock.writeLock().unlock();
		}
		logger.debug("Registered catalog node control provider: {}", provider); //$NON-NLS-1$
	}
	
	/**
	 * Return the appropriate control provider to use for the given tree node
	 * 
	 * @param node The node for which a provider is required
	 * 
	 * @return The appropriate control provider
	 */
	public static ICatalogTreeNodeControlProvider getProvider(ICatalogTreeNode node)
	{
		controlProvidersLock.readLock().lock();
		try
		{
			for (ICatalogTreeNodeControlProvider provider : controlProviders)
			{
				if (provider.supports(node))
				{
					return provider;
				}
			}
			return DEFAULT_PROVIDER;
		}
		finally
		{
			controlProvidersLock.readLock().unlock();
		}
	}
}
