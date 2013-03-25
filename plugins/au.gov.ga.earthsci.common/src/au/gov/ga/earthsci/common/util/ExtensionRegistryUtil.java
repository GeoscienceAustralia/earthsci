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

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with {@link IExtensionRegistry}s
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ExtensionRegistryUtil
{

	private static final Logger logger = LoggerFactory.getLogger(ExtensionRegistryUtil.class);
	
	@Inject
	public static void init(IExtensionRegistry registry, IEclipseContext context)
	{
		logger.debug("Initialising extension registry util"); //$NON-NLS-1$
		ExtensionRegistryUtil.registry = registry;
		ExtensionRegistryUtil.context = context;
	}
	
	private static IExtensionRegistry registry;
	private static IEclipseContext context;
	
	/**
	 * Instantiate classes registered against the given extension point ID using the named attribute.
	 * Once instantiated, the callback will be invoked with the newly created object.
	 *  
	 * @param extensionPointId The ID of the extension point to load from
	 * @param classAttribute The name of the attribute defining the target class
	 * @param clazz The type of object that is expected (used for validation)
	 * @param callback The callback to execute once the object has been instantiated
	 */
	public static <T> void createFromExtension(String extensionPointId, 
										   String classAttribute, 
										   Class<? extends T> clazz, 
										   Callback<T> callback) throws CoreException
	{
		IConfigurationElement[] config = registry.getConfigurationElementsFor(extensionPointId);
		for (IConfigurationElement e : config)
		{
			final Object o = e.createExecutableExtension(classAttribute);
			if (clazz == null || !clazz.isInstance(o))
			{
				continue;
			}
			T t = clazz.cast(o);
			ContextInjectionFactory.inject(t, context);
			if (callback != null)
			{
				callback.run(t, e, context);
			}
		}
	}
	
	
	public static interface Callback<T>
	{
		void run(T t, IConfigurationElement element, IEclipseContext context);
	}
}
