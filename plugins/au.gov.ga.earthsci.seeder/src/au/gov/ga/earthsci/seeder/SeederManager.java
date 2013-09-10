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
package au.gov.ga.earthsci.seeder;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ga.earthsci.injectable.ExtensionPointHelper;

/**
 * Manages a collection of seeders, defined by the
 * {@value #SEEDERS_EXTENSION_POINT_ID} extension point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SeederManager
{
	public static final String SEEDERS_EXTENSION_POINT_ID = "au.gov.ga.earthsci.seeders"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(SeederManager.class);

	@Inject
	private static IEclipseContext context;

	private static final Map<String, Class<ISeeder>> seeders = new HashMap<String, Class<ISeeder>>();
	private static final Map<Class<ISeeder>, ISeeder> seederCache = new HashMap<Class<ISeeder>, ISeeder>();

	static
	{
		IConfigurationElement[] config =
				RegistryFactory.getRegistry().getConfigurationElementsFor(SEEDERS_EXTENSION_POINT_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				String elementName = element.getAttribute("element"); //$NON-NLS-1$
				if (seeders.containsKey(elementName))
				{
					throw new Exception("Element name '" + elementName + "' already exists in the seeder registry"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				@SuppressWarnings("unchecked")
				Class<ISeeder> seederClass =
						(Class<ISeeder>) ExtensionPointHelper.getClassForProperty(element, "class"); //$NON-NLS-1$
				seeders.put(elementName, seederClass);
			}
			catch (Exception e)
			{
				logger.error("Error processing intent filter", e); //$NON-NLS-1$
			}
		}
	}

	public static void addSeeder(String elementName, Class<ISeeder> seederClass)
	{
		seeders.put(elementName, seederClass);
	}

	public static void removeSeeder(String elementName)
	{
		seeders.remove(elementName);
	}

	public static void seed(Document document, URL context)
	{
		Element documentElement = document.getDocumentElement();
		NodeList children = documentElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			if (!(node instanceof Element))
			{
				continue;
			}
			Element child = (Element) node;
			Class<ISeeder> seederClass = seeders.get(child.getNodeName());
			if (seederClass != null)
			{
				ISeeder seeder = seederCache.get(seederClass);
				if (seeder == null)
				{
					seeder = ContextInjectionFactory.make(seederClass, SeederManager.context);
					seederCache.put(seederClass, seeder);
				}
				seeder.seed(child, context);
			}
		}
	}
}
