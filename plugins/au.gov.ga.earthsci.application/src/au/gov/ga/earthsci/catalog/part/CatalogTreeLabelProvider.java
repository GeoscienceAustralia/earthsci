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

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
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
public class CatalogTreeLabelProvider extends LabelProvider implements ILabelDecorator
{
	private static final Logger logger = LoggerFactory.getLogger(CatalogTreeLabelProvider.class);
	
	private final org.eclipse.jface.resource.ImageRegistry decoratedImages = new org.eclipse.jface.resource.ImageRegistry();
	
	private ICatalogBrowserController controller;
	
	public CatalogTreeLabelProvider()
	{
		
	}
	
	@Override
	public Image getImage(Object element)
	{
		if (!(element instanceof ICatalogTreeNode))
		{
			return null;
		}
		ICatalogTreeNode node = (ICatalogTreeNode)element;
		
		Image icon = getProvider(node).getIcon(node);
		if (icon == null)
		{
			icon = DEFAULT_PROVIDER.getIcon(node);
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
		ICatalogTreeNode node = (ICatalogTreeNode)element;
		return getProvider(node).getLabel(node);
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
		decoratedImages.dispose();
	}
	
	@Inject
	public void setController(ICatalogBrowserController controller)
	{
		this.controller = controller;
	}
	
	@Override
	public Image decorateImage(Image image, Object element)
	{
		System.out.println("Decorating!");
		if (!(element instanceof ICatalogTreeNode) || !((ICatalogTreeNode)element).isLayerNode())
		{
			return null;
		}
		
		ICatalogTreeNode node = (ICatalogTreeNode)element;
		
		if (!controller.existsInLayerModel(node.getLayerURI()))
		{
			return null;
		}
		
		return getDecoratedIcon(image);
	}

	@Override
	public String decorateText(String text, Object element)
	{
		if (!(element instanceof ICatalogTreeNode) || !((ICatalogTreeNode)element).isLayerNode())
		{
			return null;
		}
		
		ICatalogTreeNode node = (ICatalogTreeNode)element;
		
		if (!controller.existsInLayerModel(node.getLayerURI()))
		{
			return null;
		}
		System.out.println(node.getLayerURI());
		return text + "*"; //$NON-NLS-1$
	}
	
	private Image getDecoratedIcon(Image base)
	{
		String key = base.hashCode() + ""; //$NON-NLS-1$
		
		if (base.isDisposed())
		{
			decoratedImages.remove(key);
			return null;
		}
		
		Image decorated = decoratedImages.get(key);
		if (decorated != null)
		{
			return decorated;
		}
				
		decorated = new DecorationOverlayIcon(base, ImageRegistry.getInstance().getDescriptor(ImageRegistry.DECORATION_INCLUDED), IDecoration.BOTTOM_RIGHT).createImage();
		decoratedImages.put(key, decorated);
		return decorated;
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
