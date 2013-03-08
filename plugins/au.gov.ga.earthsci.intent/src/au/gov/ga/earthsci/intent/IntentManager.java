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
package au.gov.ga.earthsci.intent;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injectable {@link Intent} manager, used for starting intents. Contains a
 * collection of the registered intent filters, and their associated handler.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class IntentManager
{
	private static IntentManager instance;

	public static IntentManager getInstance()
	{
		return instance;
	}

	private static final String INTENT_ID = "au.gov.ga.earthsci.intentFilters"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(IntentManager.class);

	private final Set<IntentFilter> filters = new HashSet<IntentFilter>();

	/**
	 * Intent manager constructor, should not be called directly. Instead the
	 * manager should be injected, or accessed via the static singleton method.
	 */
	public IntentManager()
	{
		if (instance != null)
		{
			throw new IllegalStateException(IntentManager.class.getSimpleName() + " should not be instantiated"); //$NON-NLS-1$
		}
		instance = this;

		IConfigurationElement[] config = RegistryFactory.getRegistry().getConfigurationElementsFor(INTENT_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				boolean isFilter = "filter".equals(element.getName()); //$NON-NLS-1$
				if (isFilter)
				{
					IntentFilter filter = new IntentFilter(element);
					filters.add(filter);
				}
			}
			catch (Exception e)
			{
				logger.error("Error processing intent filter", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Start the given Intent.
	 * 
	 * @param intent
	 *            Intent to start
	 * @param caller
	 *            Caller of the intent that is notified of intent completion
	 * @param context
	 *            Eclipse context in which to run the intent
	 */
	public void start(Intent intent, IntentCaller caller, IEclipseContext context)
	{
		IntentFilter filter = findFilter(intent);
		if (filter != null)
		{
			IEclipseContext child = context.createChild();
			IntentHandler handler = ContextInjectionFactory.make(filter.getHandler(), child);
			handler.handle(intent, caller);
		}
		else
		{
			logger.error("Could not find filter to handle intent: " + intent); //$NON-NLS-1$
		}
	}

	/**
	 * Find an intent filter that matches the given intent, or null if none
	 * could be found.
	 * 
	 * @param intent
	 *            Intent to find a filter for
	 * @return Intent filter that matches the given intent
	 */
	public IntentFilter findFilter(Intent intent)
	{
		for (IntentFilter filter : filters)
		{
			if (filter.matches(intent))
				return filter;
		}
		return null;
	}

	/**
	 * Add an intent filter.
	 * 
	 * @param filter
	 */
	public void addFilter(IntentFilter filter)
	{
		filters.add(filter);
	}

	/**
	 * Remove an intent filter.
	 * 
	 * @param filter
	 */
	public void removeFilter(IntentFilter filter)
	{
		filters.remove(filter);
	}
}
