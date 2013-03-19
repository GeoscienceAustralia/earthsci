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
package au.gov.ga.earthsci.intent.dispatch;

import java.util.Collection;
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
 * Central mechanism for handling unexpected domain objects. This is inherently
 * tied to the intent system in that, if an intent handler produces a result
 * that the intent caller didn't expect, the caller can (optionally, perhaps
 * after prompting the user) pass the result to the {@link Dispatcher} to
 * gracefully handle the result.
 * <p/>
 * For example, if the layer model loads a file, but an object other than a
 * layer is returned (eg a catalog node), it can pass the object to the
 * {@link Dispatcher} to attempt to handle the object gracefully.
 * <p/>
 * UI components and plugins can define {@link DispatchHandler}s via an
 * extension point which handle instances of specific classes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class Dispatcher
{
	private static Dispatcher instance;

	public static Dispatcher getInstance()
	{
		return instance;
	}

	private static final String DISPATCH_FILTER_ID = "au.gov.ga.earthsci.dispatchFilters"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

	//filters, sorted descending by priority
	private final ListSortedMap<Integer, DispatchFilter> filters = new ArrayListTreeMap<Integer, DispatchFilter>(
			new Comparator<Integer>()
			{
				@Override
				public int compare(Integer o1, Integer o2)
				{
					return -o1.compareTo(o2);
				}
			});

	/**
	 * Constructor, should not be called directly. Instead it should be
	 * injected, or accessed via the static singleton method.
	 */
	public Dispatcher()
	{
		if (instance != null)
		{
			throw new IllegalStateException(Dispatcher.class.getSimpleName() + " should not be instantiated"); //$NON-NLS-1$
		}
		instance = this;

		IConfigurationElement[] config = RegistryFactory.getRegistry().getConfigurationElementsFor(DISPATCH_FILTER_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				boolean isFilter = "filter".equals(element.getName()); //$NON-NLS-1$
				if (isFilter)
				{
					DispatchFilter filter = new DispatchFilter(element);
					filters.putSingle(filter.getPriority(), filter);
				}
			}
			catch (Exception e)
			{
				logger.error("Error processing dispatch filter", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Dispatch the given object to a dispatch handler if a matching filter can
	 * be found for the object's class. The dispatch handler is instantiated
	 * using the {@link ContextInjectionFactory}, which injects the handler
	 * using the provided context.
	 * 
	 * @param object
	 *            Object to dispatch
	 * @param context
	 *            Context to use for injection into the handler
	 */
	public void dispatch(Object object, IEclipseContext context)
	{
		DispatchFilter filter = findFilter(object);
		Class<? extends DispatchHandler> handlerClass = filter == null ? null : filter.getHandler();
		if (handlerClass == null)
		{
			logger.error("Could not find dispatch handler for object: " + object); //$NON-NLS-1$
			return;
		}
		IEclipseContext child = context.createChild();
		DispatchHandler handler = ContextInjectionFactory.make(handlerClass, child);
		handler.handle(object);
	}

	/**
	 * Find the best dispatch filter for the given object's class.
	 * 
	 * @param object
	 *            Object to find a filter for
	 * @return Dispatch filter for the object's class, or null if no matching
	 *         filter could be found
	 */
	public DispatchFilter findFilter(Object object)
	{
		int minDistance = Integer.MAX_VALUE;
		DispatchFilter closest = null;
		for (List<DispatchFilter> list : filters.values())
		{
			for (DispatchFilter filter : list)
			{
				int distance = distanceToClosestType(object.getClass(), filter.getTypes());
				if (distance >= 0 && distance < minDistance)
				{
					minDistance = distance;
					closest = filter;
				}
			}
		}
		return closest;
	}

	protected static int distanceToClosestType(Class<?> subclass, Collection<Class<?>> superclasses)
	{
		int minDistance = Integer.MAX_VALUE;
		for (Class<?> superclass : superclasses)
		{
			int distance = classHierarchyDistance(subclass, superclass);
			if (distance >= 0 && distance < minDistance)
			{
				minDistance = distance;
			}
		}
		return minDistance == Integer.MAX_VALUE ? -1 : minDistance;
	}

	protected static int classHierarchyDistance(Class<?> subclass, Class<?> superclass)
	{
		if (subclass == null || superclass == null)
			return -1;
		if (!superclass.isAssignableFrom(subclass))
			return -1;
		return classHierarchyDistance(subclass, superclass, 0);
	}

	private static int classHierarchyDistance(Class<?> subclass, Class<?> superclass, int position)
	{
		if (subclass.equals(superclass))
			return position;
		position++;
		Class<?>[] supers = subclass.getInterfaces();
		if (subclass.getSuperclass() != null)
		{
			Class<?>[] newSupers = new Class<?>[supers.length + 1];
			System.arraycopy(supers, 0, newSupers, 1, supers.length);
			supers = newSupers;
			supers[0] = subclass.getSuperclass();
		}
		int minDistance = Integer.MAX_VALUE;
		for (Class<?> s : supers)
		{
			int distance = classHierarchyDistance(s, superclass, position);
			if (distance >= 0 && distance < minDistance)
			{
				minDistance = distance;
			}
		}
		return minDistance == Integer.MAX_VALUE ? -1 : minDistance;
	}
}
