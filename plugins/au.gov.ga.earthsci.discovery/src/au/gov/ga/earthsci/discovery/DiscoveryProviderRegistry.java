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
package au.gov.ga.earthsci.discovery;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central {@link IDiscoveryProvider} registry. Reads the list of discovery
 * providers using the extension registry mechanism.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class DiscoveryProviderRegistry
{
	public static final String DISCOVERY_PROVIDERS_ID = "au.gov.ga.earthsci.discovery.providers"; //$NON-NLS-1$

	private static final Map<String, IDiscoveryProvider> providers = new HashMap<String, IDiscoveryProvider>();
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryProviderRegistry.class);

	/**
	 * Register the given {@link IDiscoveryProvider}.
	 * 
	 * @param provider
	 *            Provider to register
	 */
	public static void registerProvider(IDiscoveryProvider provider)
	{
		providers.put(provider.getId(), provider);
	}

	/**
	 * Unregister the given {@link IDiscoveryProvider}.
	 * 
	 * @param provider
	 *            Provider to unregister
	 */
	public static void unregisterProvider(IDiscoveryProvider provider)
	{
		providers.remove(provider.getId());
	}

	/**
	 * Get the provider implementation with the given id.
	 * 
	 * @param id
	 *            ID to get the provider for
	 * @return Discovery provider with the given id, or null if no provider with
	 *         that id could be found.
	 * @see IDiscoveryProvider#getId()
	 */
	public static IDiscoveryProvider getProviderForId(String id)
	{
		return providers.get(id);
	}

	@Inject
	public static void registerExtensions()
	{
		IConfigurationElement[] config =
				RegistryFactory.getRegistry().getConfigurationElementsFor(DISCOVERY_PROVIDERS_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				IDiscoveryProvider provider = (IDiscoveryProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
				registerProvider(provider);
			}
			catch (CoreException e)
			{
				logger.error("Error creating discovery provider", e); //$NON-NLS-1$
			}
		}
	}
}
