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
package au.gov.ga.earthsci.core.url;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers all the {@link URLStreamHandlerService}s defined by the extension
 * points.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URLStreamHandlerServiceRegistrar
{
	private static final String HANDLER_ID = "au.gov.ga.earthsci.core.urlStreamHandlers"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(URLStreamHandlerServiceRegistrar.class);

	public static void register(BundleContext context)
	{
		//first sort the configuration elements by priority, descending
		IConfigurationElement[] config = RegistryFactory.getRegistry().getConfigurationElementsFor(HANDLER_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				Object o = element.createExecutableExtension("class"); //$NON-NLS-1$
				List<String> protocols = new ArrayList<String>();
				IConfigurationElement[] children = element.getChildren("protocol"); //$NON-NLS-1$
				for (IConfigurationElement child : children)
				{
					String protocol = child.getAttribute("name"); //$NON-NLS-1$
					protocols.add(protocol);
				}

				Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
				properties.put(URLConstants.URL_HANDLER_PROTOCOL, protocols.toArray(new String[protocols.size()]));
				context.registerService(URLStreamHandlerService.class.getName(), o, properties);
			}
			catch (CoreException e)
			{
				logger.error("Error registering URLStreamHandlerService", e); //$NON-NLS-1$
			}
		}
	}
}
