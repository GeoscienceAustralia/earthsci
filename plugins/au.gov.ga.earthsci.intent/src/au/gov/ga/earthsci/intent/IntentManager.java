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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Singleton;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.collection.ArrayListTreeMap;
import au.gov.ga.earthsci.common.collection.ListSortedMap;
import au.gov.ga.earthsci.intent.util.ContextInjectionFactoryThreadSafe;

/**
 * Injectable {@link Intent} manager, used for starting intents. Contains a
 * collection of the registered intent filters, and their associated handler.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class IntentManager implements IIntentManager
{
	private static IIntentManager instance;

	/**
	 * @return An instance of the intent manager
	 */
	public static IIntentManager getInstance()
	{
		return instance;
	}

	/**
	 * Set the singleton instance of the intent manager. Generally should not be
	 * called, but handy for inserting implementations for unit testing.
	 * 
	 * @param instance
	 */
	public static void setInstance(IIntentManager instance)
	{
		IntentManager.instance = instance;
	}

	private static final String INTENT_FILTERS_ID = "au.gov.ga.earthsci.intentFilters"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(IntentManager.class);

	//filters, sorted descending by priority
	private final ListSortedMap<Integer, IntentFilter> filters = new ArrayListTreeMap<Integer, IntentFilter>(
			new Comparator<Integer>()
			{
				@Override
				public int compare(Integer o1, Integer o2)
				{
					return -o1.compareTo(o2);
				}
			});

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

		IConfigurationElement[] config = RegistryFactory.getRegistry().getConfigurationElementsFor(INTENT_FILTERS_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				boolean isFilter = "filter".equals(element.getName()); //$NON-NLS-1$
				if (isFilter)
				{
					IntentFilter filter = new IntentFilter(element);
					filters.putSingle(filter.getPriority(), filter);
				}
			}
			catch (Exception e)
			{
				logger.error("Error processing intent filter", e); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void start(Intent intent, IIntentCallback callback, IEclipseContext context)
	{
		Class<? extends IIntentHandler> handlerClass = intent.getHandler();
		if (handlerClass == null)
		{
			IntentFilter filter = findFilter(intent);
			if (filter != null)
			{
				handlerClass = filter.getHandler();
			}
		}

		if (handlerClass != null)
		{
			IEclipseContext child = context.createChild();
			IIntentHandler handler = ContextInjectionFactoryThreadSafe.make(handlerClass, child);
			handler.handle(intent, callback);
		}
		else
		{
			logger.error("Could not find filter to handle intent: " + intent); //$NON-NLS-1$
		}
	}

	@Override
	public IntentFilter findFilter(Intent intent)
	{
		//add matching filters to a list, prioritising any that have a matching return type
		List<IntentFilter> matches = new ArrayList<IntentFilter>();
		boolean anyMatchExpectedReturnType = false;
		for (List<IntentFilter> list : filters.values())
		{
			for (IntentFilter filter : list)
			{
				if (filter.matches(intent))
				{
					if (filter.anyReturnTypesMatch(intent.getExpectedReturnType()))
					{
						if (!anyMatchExpectedReturnType)
						{
							//we've found the first filter that matches the intent's return type, so
							//clear any that we've previously added that don't
							matches.clear();
							anyMatchExpectedReturnType = true;
						}
						matches.add(filter);
					}
					else if (!anyMatchExpectedReturnType)
					{
						matches.add(filter);
					}
				}
			}
		}

		//if no matches, return null
		if (matches.isEmpty())
		{
			return null;
		}

		//if no content type matching, we don't need to find the closest, so just return the first one found
		if (intent.getContentType() == null)
		{
			return matches.get(0);
		}

		//find the filter in the list of matches that most closely matches the intent's content type
		int minDistance = Integer.MAX_VALUE;
		IntentFilter closest = null;
		for (IntentFilter filter : matches)
		{
			int distance =
					ContentTypeHelper.distanceToClosestMatching(intent.getContentType(), filter.getContentTypes());
			if (distance >= 0 && distance < minDistance)
			{
				minDistance = distance;
				closest = filter;
			}
		}
		return closest;
	}

	@Override
	public void addFilter(IntentFilter filter)
	{
		filters.putSingle(filter.getPriority(), filter);
	}

	@Override
	public void removeFilter(IntentFilter filter)
	{
		filters.removeSingle(filter.getPriority(), filter);
	}
}
