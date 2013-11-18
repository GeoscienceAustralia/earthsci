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
package au.gov.ga.earthsci.common.util;

import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.osgi.framework.Bundle;

import au.gov.ga.earthsci.common.Activator;

/**
 * Helper class for accessing classes and resources defined in extention point
 * definitions.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExtensionPointHelper
{
	/**
	 * Get an integer value from the extension point configuration element under
	 * the given property name.
	 * 
	 * @param element
	 *            Extension point configuration element
	 * @param propertyName
	 *            Property name in the element that defines the integer value
	 * @param defaultValue
	 *            Value to return if the string attribute cannot be parsed as an
	 *            int
	 * @return Integer value of the configuration element's attribute for the
	 *         given property name
	 */
	public static int getIntegerForProperty(IConfigurationElement element, String propertyName, int defaultValue)
	{
		try
		{
			return Integer.parseInt(element.getAttribute(propertyName));
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}

	/**
	 * Get a boolean value from the extension point configuration element under
	 * the given property name.
	 * 
	 * @param element
	 *            Extension point configuration element
	 * @param propertyName
	 *            Property name in the element that defines the boolean value
	 * @param defaultValue
	 *            Value to return if the attribute doesn't exist
	 * @return Boolean value of the configuration element's attribute for the
	 *         given property name
	 */
	public static boolean getBooleanForProperty(IConfigurationElement element, String propertyName, boolean defaultValue)
	{
		String s = element.getAttribute(propertyName);
		if (s != null)
		{
			return Boolean.parseBoolean(s);
		}
		return defaultValue;
	}

	/**
	 * Load the class defined in the extension point configuration element under
	 * the given property name.
	 * 
	 * @param element
	 *            Extension point configuration element
	 * @param propertyName
	 *            Property name in element that defines the class name
	 * @return Class defined in the configuration element property
	 * @throws ClassNotFoundException
	 *             If the class could not be found
	 */
	public static Class<?> getClassForProperty(IConfigurationElement element, String propertyName)
			throws ClassNotFoundException
	{
		String className = element.getAttribute(propertyName);
		return getClassForName(element, className);
	}

	/**
	 * Load the named class defined in the given extension point configuration
	 * element.
	 * 
	 * @param element
	 *            Extension point configuration element
	 * @param className
	 *            Fully qualified class name, passed to
	 *            {@link Bundle#loadClass(String)}
	 * @return Class for the given name
	 * @throws ClassNotFoundException
	 *             If the class could not be found
	 */
	public static Class<?> getClassForName(IConfigurationElement element, String className)
			throws ClassNotFoundException
	{
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

	/**
	 * Get a URL pointing to the resource defined in the extension point
	 * configuration element under the given property name.
	 * 
	 * @param element
	 *            Extension point configuration element
	 * @param propertyName
	 *            Property name in element that defines the resource name
	 * @return URI pointing at the resource, or null if the resource could not
	 *         be found
	 */
	public static URL getResourceURLForProperty(IConfigurationElement element, String propertyName)
	{
		String resourceName = element.getAttribute(propertyName);
		return getResourceURLForName(element, resourceName);
	}

	/**
	 * Get a URL pointing to the named resource defined in the given extension
	 * point configuration element.
	 * 
	 * @param element
	 *            Extension point configuration element
	 * @param resourceName
	 *            Resource name, relative to the defining plugin (passed to
	 *            {@link Bundle#getResource(String)})
	 * @return URI pointing at the resource, or null if the resource could not
	 *         be found
	 */
	public static URL getResourceURLForName(IConfigurationElement element, String resourceName)
	{
		if (resourceName == null)
		{
			return null;
		}
		IContributor contributor = element.getContributor();
		if (contributor instanceof RegistryContributor)
		{
			String stringId = ((RegistryContributor) contributor).getId();
			long id = Long.parseLong(stringId);
			Bundle bundle = Activator.getContext().getBundle(id);
			return bundle.getResource(resourceName);
		}
		return null;
	}
}
