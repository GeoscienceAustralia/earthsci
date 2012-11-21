/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.injectable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for reading extensions that extend the injectable extension
 * point, and injects them into a provided {@link IEclipseContext}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public final class Injector
{
	private static final String INJECTOR_ID = "au.gov.ga.earthsci.injectable"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(Injector.class);

	private Injector()
	{
	}

	public static void injectIntoContext(IEclipseContext context)
	{
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(INJECTOR_ID);
		try
		{
			for (IConfigurationElement element : config)
			{
				Object object = element.createExecutableExtension("class"); //$NON-NLS-1$
				ContextInjectionFactory.inject(object, context);
			}
		}
		catch (Exception e)
		{
			logger.error("Error processing injectable", e); //$NON-NLS-1$
		}
	}
}
