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
package au.gov.ga.earthsci.catalog.ui;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;

/**
 * Registry that provides {@link ICatalogTreeLabelProvider}s for initializing
 * the UI for catalog tree nodes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogTreeLabelProviderRegistry
{
	@Inject
	public void initialiseExtensions(IExtensionRegistry registry)
	{
		loadProviders(registry);
	}

	private static final Logger logger = LoggerFactory.getLogger(CatalogTreeLabelProviderRegistry.class);

	private static final Set<ICatalogTreeLabelProvider> labelProviders = new LinkedHashSet<ICatalogTreeLabelProvider>();
	private static final ReadWriteLock labelProvidersLock = new ReentrantReadWriteLock();

	public static final String CATALOG_NODE_CONTROL_PROVIDER_EXTENSION_POINT_ID =
			"au.gov.ga.earthsci.catalog.ui.catalogTreeLabelProvider"; //$NON-NLS-1$
	public static final String CONTROL_PROVIDER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	/**
	 * The default provider - can be used to obtain default values for a given
	 * node
	 */
	private static final ICatalogTreeLabelProvider DEFAULT_PROVIDER = new ICatalogTreeLabelProvider()
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
		public URL getIconURL(ICatalogTreeNode node)
		{
			URL result = null;
			if (node.getParent() == null || node.getParent().getParent() == null)
			{
				result = ImageRegistry.getInstance().getURL(ImageRegistry.ICON_REPOSITORY);
			}
			else if (node.isLayerNode())
			{
				result = ImageRegistry.getInstance().getURL(ImageRegistry.ICON_FILE);
			}
			else
			{
				result = ImageRegistry.getInstance().getURL(ImageRegistry.ICON_FOLDER);
			}
			return result;
		}

		@Override
		public void dispose()
		{
		};
	};

	/**
	 * @return The default label provider to use if no others are available
	 */
	public static ICatalogTreeLabelProvider getDefaultProvider()
	{
		return DEFAULT_PROVIDER;
	}

	/**
	 * Load the registered providers from the extension registry
	 * 
	 * @param registry
	 *            The registry to load from
	 */
	public static void loadProviders(final IExtensionRegistry registry)
	{
		logger.debug("Registering catalog tree label providers"); //$NON-NLS-1$

		IConfigurationElement[] config =
				registry.getConfigurationElementsFor(CATALOG_NODE_CONTROL_PROVIDER_EXTENSION_POINT_ID);
		try
		{
			for (IConfigurationElement e : config)
			{
				final Object o = e.createExecutableExtension(CONTROL_PROVIDER_CLASS_ATTRIBUTE);
				if (o instanceof ICatalogTreeLabelProvider)
				{
					registerProvider((ICatalogTreeLabelProvider) o);
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
	 * @param provider
	 *            The provider to register
	 */
	public static void registerProvider(final ICatalogTreeLabelProvider provider)
	{
		if (provider == null)
		{
			return;
		}

		labelProvidersLock.writeLock().lock();
		try
		{
			labelProviders.add(provider);
		}
		finally
		{
			labelProvidersLock.writeLock().unlock();
		}
		logger.debug("Registered catalog node label provider: {}", provider); //$NON-NLS-1$
	}

	/**
	 * Return the appropriate control provider to use for the given tree node
	 * 
	 * @param node
	 *            The node for which a provider is required
	 * 
	 * @return The appropriate control provider
	 */
	public static ICatalogTreeLabelProvider getProvider(ICatalogTreeNode node)
	{
		labelProvidersLock.readLock().lock();
		try
		{
			for (ICatalogTreeLabelProvider provider : labelProviders)
			{
				if (provider.supports(node))
				{
					return provider;
				}
			}
			return getDefaultProvider();
		}
		finally
		{
			labelProvidersLock.readLock().unlock();
		}
	}

	/**
	 * Dispose of all the resources used by the control providers.
	 */
	public static void dispose()
	{
		labelProvidersLock.readLock().lock();
		try
		{
			for (ICatalogTreeLabelProvider provider : labelProviders)
			{
				provider.dispose();
			}
		}
		finally
		{
			labelProvidersLock.readLock().unlock();
		}
	}
}
