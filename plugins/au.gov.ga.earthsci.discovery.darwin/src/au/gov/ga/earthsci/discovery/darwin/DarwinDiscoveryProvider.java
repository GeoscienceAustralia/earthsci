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
import java.util.Map;

import au.gov.ga.earthsci.discovery.IDiscoveryProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryResultHandler;
import au.gov.ga.earthsci.discovery.IDiscoveryServiceProperty;

/**
 * {@link IDiscoveryProvider} implementation for DARWIN.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DarwinDiscoveryProvider implements IDiscoveryProvider
{
	@Override
	public String getId()
	{
		return "darwin"; //$NON-NLS-1$
	}

	@Override
	public String getName()
	{
		return "GA Search";
	}

	@Override
	public URL getIconURL()
	{
		return Icons.GA;
	}

	@Override
	public DarwinDiscoveryService createService(String name, URL url,
			Map<IDiscoveryServiceProperty<?>, Object> propertyValues)
	{
		return new DarwinDiscoveryService(this, name, url);
	}

	@Override
	public IDiscoveryServiceProperty<?>[] getProperties()
	{
		return null;
	}

	@Override
	public IDiscoveryResultHandler getHandler()
	{
		return new DarwinDiscoveryResultHandler();
	}
}
