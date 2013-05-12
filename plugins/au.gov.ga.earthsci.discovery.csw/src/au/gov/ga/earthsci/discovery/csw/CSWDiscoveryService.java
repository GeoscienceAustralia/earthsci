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
package au.gov.ga.earthsci.discovery.csw;

import java.net.URL;

import au.gov.ga.earthsci.discovery.IDiscovery;
import au.gov.ga.earthsci.discovery.IDiscoveryParameters;
import au.gov.ga.earthsci.discovery.IDiscoveryProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class CSWDiscoveryService implements IDiscoveryService
{
	private final URL serviceURL;
	private final IDiscoveryProvider provider;
	private boolean enabled;

	public CSWDiscoveryService(URL serviceURL, IDiscoveryProvider provider)
	{
		this.serviceURL = serviceURL;
		this.provider = provider;
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
	public IDiscovery createDiscovery(IDiscoveryParameters parameters)
	{
		return new CSWDiscovery(parameters, this);
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
