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
import java.util.Map;

import au.gov.ga.earthsci.discovery.IDiscoveryProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryService;
import au.gov.ga.earthsci.discovery.IDiscoveryServiceProperty;

/**
 * {@link IDiscoveryProvider} implementation for CSW discoveries.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CSWDiscoveryProvider implements IDiscoveryProvider
{
	private final CSWFormatProperty formatProperty = new CSWFormatProperty();
	private final IDiscoveryServiceProperty<?>[] properties = new IDiscoveryServiceProperty[] { formatProperty };

	@Override
	public String getId()
	{
		return "csw"; //$NON-NLS-1$
	}

	@Override
	public String getName()
	{
		return "CSW"; //$NON-NLS-1$
	}

	@Override
	public URL getIconURL()
	{
		return Icons.MAP_SERVER;
	}

	@Override
	public IDiscoveryService createService(String name, URL url,
			Map<IDiscoveryServiceProperty<?>, Object> propertyValues)
	{
		CSWFormat format = (CSWFormat) propertyValues.get(formatProperty);
		if (format == null)
		{
			format = CSWFormat.GEONETWORK2;
		}
		return new CSWDiscoveryService(format, name, url, this);
	}

	@Override
	public IDiscoveryServiceProperty<?>[] getProperties()
	{
		return properties;
	}
}
