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
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.util.collection.ArrayListTreeMap;
import au.gov.ga.earthsci.util.collection.ListSortedMap;

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
	public void start(Intent intent, IIntentCaller caller, IEclipseContext context)
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
			IIntentHandler handler = ContextInjectionFactory.make(handlerClass, child);
			handler.handle(intent, caller);
		}
		else
		{
			logger.error("Could not find filter to handle intent: " + intent); //$NON-NLS-1$
		}
	}

	/**
	 * Find an intent filter that best matches the given intent, or null if none
	 * could be found.
	 * <p/>
	 * The best match is defined as follows:
	 * <ul>
	 * <li>If the intent defines an expected return type, any filters that
	 * define that return type are preferred over those that don't</li>
	 * <li>If the intent defines a content type, the filters that define a
	 * content type closer to the intent's content type are preferred</li>
	 * <li>Otherwise the first matching filter is returned</li>
	 * </ul>
	 * 
	 * @param intent
	 *            Intent to find a filter for
	 * @return Intent filter that matches the given intent
	 */
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
			return null;

		//if no content type matching, we don't need to find the closest, so just return the first one found
		if (intent.getContentType() == null)
			return matches.get(0);

		//find the filter in the list of matches that most closely matches the intent's content type
		int minDistance = Integer.MAX_VALUE;
		IntentFilter closest = null;
		for (IntentFilter filter : matches)
		{
			int distance =
					ContentTypeHelper.distanceToClosestMatching(intent.getContentType(),
							filter.getContentTypes());
			if (distance >= 0 && distance < minDistance)
			{
				minDistance = distance;
				closest = filter;
			}
		}
		return closest;
	}

	/**
	 * Add an intent filter.
	 * 
	 * @param filter
	 */
	public void addFilter(IntentFilter filter)
	{
		filters.putSingle(filter.getPriority(), filter);
	}

	/**
	 * Remove an intent filter.
	 * 
	 * @param filter
	 */
	public void removeFilter(IntentFilter filter)
	{
		filters.removeSingle(filter.getPriority(), filter);
	}
}
