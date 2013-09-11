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
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import au.gov.ga.earthsci.common.collection.ArrayListHashMap;
import au.gov.ga.earthsci.common.collection.ListMap;
import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.seeder.ISeeder;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Seeder for removing discovery services.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryRemoveSeeder implements ISeeder
{
	@Override
	public void seed(Element element, URL context)
	{
		ListMap<String, IDiscoveryService> services = new ArrayListHashMap<String, IDiscoveryService>();
		for (IDiscoveryService service : DiscoveryServiceManager.getServices())
		{
			services.putSingle(service.getServiceURL().toExternalForm(), service);
		}

		Element[] children = XmlUtil.getElements(element);
		for (Element child : children)
		{
			//remove any services named in attributes with "url" or "uri" in the attribute name
			NamedNodeMap attributes = child.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Attr attr = (Attr) attributes.item(i);
				String lowerName = attr.getName().toLowerCase();
				if (lowerName.contains("url") || lowerName.contains("uri")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					String url = attr.getValue();
					if (!Util.isBlank(url))
					{
						List<IDiscoveryService> remove = services.get(url);
						if (remove != null)
						{
							for (IDiscoveryService service : remove)
							{
								DiscoveryServiceManager.removeService(service);
							}
						}
					}
				}
			}
		}
	}
}
