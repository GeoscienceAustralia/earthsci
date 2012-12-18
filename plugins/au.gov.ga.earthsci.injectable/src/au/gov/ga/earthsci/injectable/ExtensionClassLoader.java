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
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.osgi.framework.Bundle;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 *
 */
public class ExtensionClassLoader
{
	public static Class<?> getClass(IConfigurationElement element, String propertyName) throws ClassNotFoundException
	{
		String className = element.getAttribute(propertyName);
		IContributor contributor = element.getContributor();
		if (contributor instanceof RegistryContributor)
		{
			String stringId = ((RegistryContributor) contributor).getId();
			long id = Long.parseLong(stringId);
			Bundle bundle = Activator.getContext().getBundle(id);
			return bundle.loadClass(className);
		}
		throw new ClassNotFoundException(className);
	}
}
