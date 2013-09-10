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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.persistence.PersistenceException;
import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.seeder.ISeeder;

/**
 * Seeder for discovery services.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoverySeeder implements ISeeder
{
	private static final Logger logger = LoggerFactory.getLogger(DiscoverySeeder.class);

	@Override
	public void seed(Element element, URL context)
	{
		Set<String> serviceUrls = new HashSet<String>();
		for (IDiscoveryService service : DiscoveryServiceManager.getServices())
		{
			serviceUrls.add(service.getServiceURL().toExternalForm());
		}

		Element[] children = XmlUtil.getElements(element);
		for (Element child : children)
		{
			try
			{
				List<IDiscoveryService> services = DiscoveryServiceManager.loadServices(child);
				for (IDiscoveryService service : services)
				{
					if (!serviceUrls.contains(service.getServiceURL().toExternalForm()))
					{
						DiscoveryServiceManager.addService(service);
					}
				}
			}
			catch (PersistenceException e)
			{
				logger.error("Error unpersisting discovery services from seed file", e); //$NON-NLS-1$
			}
		}
	}
}
