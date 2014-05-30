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
package au.gov.ga.earthsci.logging;

import org.eclipse.core.internal.runtime.DevClassPathHelper;
import org.eclipse.osgi.internal.loader.EquinoxClassLoader;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.slf4j.LoggerFactory;

/**
 * When running in development mode (with osgi.dev property set, or with -dev
 * command line), the <code>dev.properties</code> file in the
 * <code>workspace/.metadata/.plugins/org.eclipse.pde.core/&lt;product name&gt;</code>
 * directory is used to extend the classpath to include required libraries (see
 * {@link DevClassLoadingHook}). This causes the
 * org.slf4j.impl.StaticLoggerBinder.class to be found twice, causing the <a
 * href="http://www.slf4j.org/codes.html#multiple_bindings">multiple
 * bindings</a> error to be printed in the console.
 * <p/>
 * This class applies a fix to the classloader of the {@link LoggerFactory}
 * class that removes any duplicate jars in the classpath.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DevelopmentModeMultipleBindingsFix
{
	public static void apply()
	{
		if (DevClassPathHelper.inDevelopmentMode())
		{
			ClassLoader loggerFactoryClassLoader = LoggerFactory.class.getClassLoader();
			if (loggerFactoryClassLoader instanceof EquinoxClassLoader)
			{
				EquinoxClassLoader ecl = (EquinoxClassLoader) loggerFactoryClassLoader;
				ClasspathManager manager = ecl.getClasspathManager();
				ClasspathEntry[] entries = manager.getHostClasspathEntries();
				ClasspathEntry projectDir = null;
				for (ClasspathEntry entry : entries)
				{
					if (entry.getBundleFile().getBaseFile().isDirectory())
					{
						projectDir = entry;
						break;
					}
				}
				for (int i = 0; i < entries.length; i++)
				{
					ClasspathEntry entry = entries[i];
					for (int j = 0; j < i; j++)
					{
						if (entry.getBundleFile().getBaseFile().equals(entries[j].getBundleFile().getBaseFile()))
						{
							entries[i] = projectDir;
							break;
						}
					}
				}
			}
		}
	}
}
