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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.intent.resolver.ContentTypeResolverManager;
import au.gov.ga.earthsci.intent.util.ContextInjectionFactoryThreadSafe;

/**
 * Injectable {@link Intent} manager, used for starting intents. Contains a
 * collection of the registered intent filters, and their associated handler.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IntentManager implements IIntentManager
{
	private static final String INTENT_FILTERS_ID = "au.gov.ga.earthsci.intent.filters"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(IntentManager.class);
	private static IIntentManager instance = new IntentManager();

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

	private final List<IntentFilter> filters = new ArrayList<IntentFilter>();

	private final LinkedBlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<Runnable>();
	private ExecutorService executor;

	private IntentManager()
	{
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
	public void beginExecution()
	{
		synchronized (executorQueue)
		{
			if (executor == null)
			{
				executor = Executors.newFixedThreadPool(5, new ThreadFactory()
				{
					private int count = 0;

					@Override
					public Thread newThread(Runnable r)
					{
						Thread thread = new Thread(r);
						thread.setName("Intent thread " + (++count)); //$NON-NLS-1$
						return thread;
					}
				});
				for (Runnable runnable : executorQueue)
				{
					executor.execute(runnable);
				}
				executorQueue.clear();
			}
		}
	}

	@Override
	public void start(Intent intent, IIntentCallback callback, IEclipseContext context)
	{
		start(intent, null, true, callback, context);
	}

	@Override
	public void start(final Intent intent, final IIntentFilterSelectionPolicy selectionPolicy,
			final boolean showProgress, final IIntentCallback callback, final IEclipseContext context)
	{
		synchronized (executorQueue)
		{
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					//TODO add progress monitor if show progress is true

					try
					{
						IntentFilter filter = null;
						Class<? extends IIntentHandler> handlerClass = intent.getHandler();
						if (handlerClass == null)
						{
							//if intent has no content type, try to determine it
							if (intent.getContentType() == null && intent.isDetermineContentType())
							{
								IContentType contentType = determineContentType(intent, showProgress, context);
								intent.setContentType(contentType);
							}

							//search through all registered filters for those that can handle the intent
							List<IntentFilter> filters = findFilters(intent);
							if (selectionPolicy != null)
							{
								//remove any filters that the selection filter disallows
								Iterator<IntentFilter> iterator = filters.iterator();
								while (iterator.hasNext())
								{
									if (!selectionPolicy.allowed(intent, iterator.next()))
									{
										iterator.remove();
									}
								}
							}
							if (!callback.filters(filters, intent))
							{
								return;
							}
							if (filters.isEmpty())
							{
								throw new Exception("Could not find filter to handle intent: " + intent); //$NON-NLS-1$
							}

							//select the filter to use to handle the intent
							filter = selectFilter(filters, intent, context);
							if (filter == null)
							{
								callback.aborted(intent);
								return;
							}
							handlerClass = filter.getHandler();
							if (handlerClass == null)
							{
								throw new Exception("Selected intent filter has no handler registered"); //$NON-NLS-1$
							}
						}

						//create the handler, and notify the callback
						IEclipseContext activeLeaf = context.getActiveLeaf();
						IEclipseContext child = activeLeaf.createChild();
						IIntentHandler handler = ContextInjectionFactoryThreadSafe.make(handlerClass, child);
						if (!callback.starting(filter, handler, intent))
						{
							return;
						}

						//handle the intent
						handler.handle(intent, callback);
					}
					catch (Exception e)
					{
						callback.error(e, intent);
					}
				}
			};

			if (executor == null)
			{
				executorQueue.add(runnable);
			}
			else
			{
				executor.execute(runnable);
			}
		}

	}

	protected IContentType determineContentType(final Intent intent, boolean showProgress, IEclipseContext context)
			throws IOException
	{
		URL url = null;
		try
		{
			url = intent.getURL();
		}
		catch (MalformedURLException e)
		{
		}
		if (url == null)
		{
			return null;
		}

		InputStream is = new LazyURLInputStream(url);
		try
		{
			return ContentTypeResolverManager.resolveContentType(url, intent);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	protected IntentFilter selectFilter(final List<IntentFilter> filters, final Intent intent, IEclipseContext context)
	{
		if (filters == null || filters.isEmpty())
		{
			return null;
		}
		if (filters.size() == 1)
		{
			return filters.get(0);
		}

		//show a dialog allowing the user to select which intent filter to use
		final IntentSelectionDialog.Factory dialogFactory = new IntentSelectionDialog.Factory();
		ContextInjectionFactoryThreadSafe.inject(dialogFactory, context);
		final AtomicInteger index = new AtomicInteger(0);
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				final IntentSelectionDialog dialog = dialogFactory.create(intent, filters);
				if (dialog.open() == Dialog.CANCEL)
				{
					index.set(-1);
					return;
				}
				index.set(dialog.getSelectedIndex());
			}
		});

		if (index.get() < 0)
		{
			return null;
		}
		return filters.get(index.get());
	}

	/**
	 * Find the intent filters that match the given intent. Returns an empty
	 * list if none could be found.
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
	 * @return Intent filters that match the given intent
	 */
	protected List<IntentFilter> findFilters(Intent intent)
	{
		//TODO is matching expected return type more important than content type distance?
		//right now, matched filter list is ordered by content type distance first

		//add matching filters to a list, prioritising any that have a matching return type
		List<IntentFilter> matches = new ArrayList<IntentFilter>();
		int matchExpectedReturnTypeIndex = 0;
		for (IntentFilter filter : filters)
		{
			if (filter.matches(intent))
			{
				if (filter.anyReturnTypesMatch(intent.getExpectedReturnType()))
				{
					matches.add(matchExpectedReturnTypeIndex++, filter);
				}
				else
				{
					matches.add(filter);
				}
			}
		}
		removeFiltersWithSuperclassHandlers(matches);
		removeNonPromptFiltersIfPromptFilterExists(matches);

		//if no matches or only 1, return list
		if (matches.isEmpty() || matches.size() == 1)
		{
			return matches;
		}

		//if the content type is defined, find the distances to the filter's content type
		Map<IntentFilter, Integer> contentTypeDistances = null;
		IContentType contentType = intent.getContentType();
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

	private void removeFiltersWithSuperclassHandlers(List<IntentFilter> filters)
	{
		Iterator<IntentFilter> iterator = filters.iterator();
		while (iterator.hasNext())
		{
			IntentFilter filter = iterator.next();
			if (hasFilterWithSubclassHandler(filters, filter))
			{
				iterator.remove();
			}
		}
	}

	private void removeNonPromptFiltersIfPromptFilterExists(List<IntentFilter> filters)
	{
		boolean anyPromptFilters = false, anyNonPromptFilters = false;
		for (IntentFilter filter : filters)
		{
			anyPromptFilters |= filter.isPrompt();
			anyNonPromptFilters |= !filter.isPrompt();
		}
		if (anyPromptFilters && anyNonPromptFilters)
		{
			Iterator<IntentFilter> iterator = filters.iterator();
			while (iterator.hasNext())
			{
				IntentFilter filter = iterator.next();
				if (!filter.isPrompt())
				{
					iterator.remove();
				}
			}
		}
	}

	private boolean hasFilterWithSubclassHandler(List<IntentFilter> filters, IntentFilter filter)
	{
		Class<? extends IIntentHandler> handler = filter.getHandler();
		if (handler != null)
		{
			for (IntentFilter f : filters)
			{
				if (f == filter)
				{
					//skip itself
					continue;
				}
				Class<? extends IIntentHandler> otherHandler = f.getHandler();
				if (otherHandler == null)
				{
					continue;
				}
				if (handler.isAssignableFrom(otherHandler))
				{
					//found a handler that is a subclass of the filter's handler in question
					return true;
				}
			}
		}
		return false;
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
