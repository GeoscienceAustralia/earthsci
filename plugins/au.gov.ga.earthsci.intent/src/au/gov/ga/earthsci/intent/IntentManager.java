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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final String INTENT_FILTERS_ID = "au.gov.ga.earthsci.intent.filters"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(IntentManager.class);

	private final List<IntentFilter> filters = new ArrayList<IntentFilter>();

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
					filters.add(filter);
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
		try
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
			if (handlerClass == null)
			{
				throw new Exception("Could not find filter to handle intent: " + intent); //$NON-NLS-1$
			}
			start(intent, handlerClass, callback, context);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

	@Override
	public void start(Intent intent, IntentFilter filter, IIntentCallback callback, IEclipseContext context)
	{
		Class<? extends IIntentHandler> handlerClass = intent.getHandler();
		if (filter == null && handlerClass == null)
		{
			throw new NullPointerException("Intent filter is null"); //$NON-NLS-1$
		}
		try
		{
			if (handlerClass == null)
			{
				handlerClass = filter.getHandler();
				if (handlerClass == null)
				{
					throw new Exception("Filter does not contain a handler to handle intent: " + intent); //$NON-NLS-1$
				}
			}
			start(intent, handlerClass, callback, context);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

	@Override
	public void start(Intent intent, Class<? extends IIntentHandler> handlerClass, IIntentCallback callback,
			IEclipseContext context)
	{
		if (handlerClass == null)
		{
			throw new NullPointerException("Intent handler class is null"); //$NON-NLS-1$
		}
		try
		{
			IEclipseContext child = context.createChild();
			IIntentHandler handler = ContextInjectionFactoryThreadSafe.make(handlerClass, child);
			handler.handle(intent, callback);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

	@Override
	public IntentFilter findFilter(Intent intent)
	{
		List<IntentFilter> filters = findFilters(intent);
		if (!filters.isEmpty())
		{
			return filters.get(0);
		}
		return null;
	}

	@Override
	public List<IntentFilter> findFilters(Intent intent)
	{
		//add matching filters to a list, prioritising any that have a matching return type
		List<IntentFilter> matches = new ArrayList<IntentFilter>();
		boolean anyMatchExpectedReturnType = false;
		for (IntentFilter filter : filters)
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

		//if no matches, return null
		if (matches.isEmpty())
		{
			return matches;
		}

		//if the content type is defined or guessed, find the distances to the filter's content type
		Map<IntentFilter, Integer> contentTypeDistances = null;
		IContentType contentType = intent.getOrGuessContentType();
		if (contentType != null)
		{
			contentTypeDistances = new HashMap<IntentFilter, Integer>();
			for (IntentFilter filter : matches)
			{
				int distance = ContentTypeHelper.distanceToClosestMatching(contentType, filter.getContentTypes());
				if (distance < 0)
				{
					//content type doesn't match, put at the end
					distance = Integer.MAX_VALUE;
				}
				contentTypeDistances.put(filter, distance);
			}
		}
		final Map<IntentFilter, Integer> contentTypeDistancesFinal = contentTypeDistances;

		//sort matches by content type distance
		//if distances are the same (or no distances were calculated), sort by priority
		Collections.sort(matches, new Comparator<IntentFilter>()
		{
			@Override
			public int compare(IntentFilter o1, IntentFilter o2)
			{
				if (contentTypeDistancesFinal != null)
				{
					Integer d1 = contentTypeDistancesFinal.get(o1);
					Integer d2 = contentTypeDistancesFinal.get(o2);
					int compare = d1.compareTo(d2);
					if (compare != 0)
					{
						return compare;
					}
				}
				return -((Integer) o1.getPriority()).compareTo(o2.getPriority());
			}
		});

		return matches;
	}

	@Override
	public void addFilter(IntentFilter filter)
	{
		filters.add(filter);
	}

	@Override
	public void removeFilter(IntentFilter filter)
	{
		filters.remove(filter);
	}
}
