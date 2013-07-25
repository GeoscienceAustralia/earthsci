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
package au.gov.ga.earthsci.discovery.darwin;

import java.net.URL;

import au.gov.ga.earthsci.discovery.AbstractDiscoveryService;
import au.gov.ga.earthsci.discovery.IDiscoveryParameters;
import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * {@link IDiscoveryService} implementation for DARWIN.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DarwinDiscoveryService extends AbstractDiscoveryService<DarwinDiscoveryProvider>
{
	public DarwinDiscoveryService(DarwinDiscoveryProvider provider, String name, URL serviceURL)
	{
		super(provider, name, serviceURL);
	}

	@Override
	public DarwinDiscovery createDiscovery(IDiscoveryParameters parameters)
	{
		return new DarwinDiscovery(this, parameters);
	}
}
