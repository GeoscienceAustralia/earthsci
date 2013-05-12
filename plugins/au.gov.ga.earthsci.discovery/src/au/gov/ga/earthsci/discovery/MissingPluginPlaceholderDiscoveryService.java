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

import java.net.URL;

/**
 * {@link IDiscoveryService} implementation that can be used as a placeholder
 * for discovery services that have been saved but whose contributing plugin is
 * missing.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MissingPluginPlaceholderDiscoveryService implements IDiscoveryService
{
	private final URL serviceURL;
	private final IDiscoveryProvider provider;
	private final boolean wasEnabled;

	public MissingPluginPlaceholderDiscoveryService(String providerId, URL serviceURL, boolean wasEnabled)
	{
		this.serviceURL = serviceURL;
		this.provider = new MissingPluginPlaceholderDiscoveryProvider(providerId);
		this.wasEnabled = wasEnabled;
	}

	@Override
	public URL getServiceURL()
	{
		return serviceURL;
	}

	@Override
	public IDiscoveryProvider getProvider()
	{
		return provider;
	}

	@Override
	public boolean isEnabled()
	{
		return false;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
	}

	public boolean wasEnabled()
	{
		return wasEnabled;
	}

	@Override
	public IDiscovery createDiscovery(IDiscoveryParameters parameters)
	{
		return null;
	}

	private class MissingPluginPlaceholderDiscoveryProvider implements IDiscoveryProvider
	{
		private final String id;

		public MissingPluginPlaceholderDiscoveryProvider(String id)
		{
			this.id = id;
		}

		@Override
		public String getId()
		{
			return id;
		}

		@Override
		public String getName()
		{
			return "Discovery provider plugin missing";
		}

		@Override
		public IDiscoveryService createService(URL serviceURL)
		{
			return null;
		}
	}
}
