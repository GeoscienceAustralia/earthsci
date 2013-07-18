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
 * Basic abstract implementation of {@link IDiscoveryService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractDiscoveryService<P extends IDiscoveryProvider> implements IDiscoveryService
{
	protected final P provider;
	protected final String name;
	protected final URL serviceURL;
	protected boolean enabled = true;

	public AbstractDiscoveryService(P provider, String name, URL serviceURL)
	{
		this.provider = provider;
		this.name = name;
		this.serviceURL = serviceURL;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public URL getServiceURL()
	{
		return serviceURL;
	}

	@Override
	public P getProvider()
	{
		return provider;
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
